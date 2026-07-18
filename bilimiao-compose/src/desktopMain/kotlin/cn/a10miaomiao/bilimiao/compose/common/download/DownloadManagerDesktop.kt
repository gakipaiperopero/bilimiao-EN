package cn.a10miaomiao.bilimiao.compose.common.download

import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryAndPathInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadMediaFileInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.CurrentDownloadInfo
import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class DownloadManagerDesktop : DownloadManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _downloadListVersion = MutableStateFlow(0)
    private val _curDownload = MutableStateFlow<CurrentDownloadInfo?>(null)

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private var _downloadList = mutableListOf<BiliDownloadEntryAndPathInfo>()
    private var waitDownloadQueue = mutableListOf<BiliDownloadEntryAndPathInfo>()
    private var currentTaskId = 0L
    private var idCounter = 1L
    private var currentDownloadJob: Job? = null

    init {
        loadDownloadList()
    }

    override val downloadListVersion: StateFlow<Int> = _downloadListVersion
    override val curDownload: StateFlow<CurrentDownloadInfo?> = _curDownload

    override val downloadList: List<BiliDownloadEntryAndPathInfo>
        get() = _downloadList

    override fun getDownloadPath(): String {
        val home = System.getProperty("user.home")
        val downloadDir = File(home, "Downloads/bilimiao")
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        return downloadDir.absolutePath
    }

    override fun readDownloadDirectory(dirPath: String): List<BiliDownloadEntryAndPathInfo> {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.listFiles()
            .filter { it.isDirectory }
            .map { File(it, "entry.json") }
            .filter { it.exists() }
            .map { entryJsonFile ->
                val entry = MiaoJson.fromJson<BiliDownloadEntryInfo>(entryJsonFile.readText())
                BiliDownloadEntryAndPathInfo(
                    entry = entry,
                    entryDirPath = entryJsonFile.parent,
                    pageDirPath = entryJsonFile.parentFile.parent,
                )
            }
    }

    override fun createDownload(biliEntry: BiliDownloadEntryInfo) {
        val entryDir = getDownloadFileDir(biliEntry)
        val entryJsonFile = File(entryDir, "entry.json")
        entryJsonFile.writeText(MiaoJson.toJson(biliEntry))
        val info = BiliDownloadEntryAndPathInfo(
            entry = biliEntry,
            pageDirPath = entryDir.parent,
            entryDirPath = entryDir.absolutePath,
        )
        val index = _downloadList.indexOfFirst {
            if (biliEntry.avid != null) biliEntry.avid == it.entry.avid
            else biliEntry.season_id == it.entry.season_id
        }
        _downloadList.add(index + 1, info)
        _downloadListVersion.value++
        if (_curDownload.value == null) {
            startDownload(info)
        } else {
            waitDownloadQueue.add(info)
        }
    }

    override fun startDownload(entryDirPath: String) {
        val info = _downloadList.find { it.entryDirPath == entryDirPath }
        if (info != null) startDownload(info)
    }

    private fun startDownload(info: BiliDownloadEntryAndPathInfo) {
        currentDownloadJob?.cancel()
        currentDownloadJob = scope.launch {
            val entry = info.entry
            val entryDir = File(info.entryDirPath)
            val danmakuXMLFile = File(entryDir, "danmaku.xml")
            val parentId = entry.season_id ?: entry.avid?.toString() ?: ""
            val id = entry.page_data?.cid ?: entry.source?.cid ?: 0L
            currentTaskId = ++idCounter
            val currentDownloadInfo = CurrentDownloadInfo(
                taskId = currentTaskId,
                parentDirPath = entryDir.parent,
                parentId = parentId,
                id = id,
                name = entry.name,
                url = "",
                header = mapOf(),
                size = entry.total_bytes,
                progress = entry.downloaded_bytes,
                length = entry.total_time_milli,
            )

            if (!danmakuXMLFile.exists()) {
                try {
                    _curDownload.value = currentDownloadInfo.copy(
                        status = CurrentDownloadInfo.STATUS_GET_DANMAKU,
                    )
                    val cid = entry.page_data?.cid ?: entry.source?.cid ?: 0L
                    val res = BiliApiService.playerAPI.getDanmakuList(cid.toString()).awaitCall()
                    val body = res.body
                    if (body != null) {
                        val xmlBytes = CompressionTools.decompressXML(body.bytes())
                        danmakuXMLFile.writeBytes(xmlBytes)
                    }
                } catch (e: Exception) {
                    _curDownload.value = currentDownloadInfo.copy(
                        status = CurrentDownloadInfo.STATUS_FAIL_DANMAKU,
                    )
                    return@launch
                }
            }

            downloadVideo(currentDownloadInfo, info)
        }
    }

    private suspend fun downloadVideo(
        currentDownloadInfo: CurrentDownloadInfo,
        info: BiliDownloadEntryAndPathInfo,
    ) {
        if (currentDownloadInfo.taskId != currentTaskId) return
        val entry = info.entry
        val entryDir = File(info.entryDirPath)
        val videoDir = File(entryDir, entry.type_tag)
        if (!videoDir.exists()) videoDir.mkdir()

        try {
            _curDownload.value = currentDownloadInfo.copy(
                status = CurrentDownloadInfo.STATUS_GET_PLAYURL,
            )

            val mediaFileInfo = fetchPlayUrl(entry)
            val httpHeader = mediaFileInfo.httpHeader()
            val mediaJsonFile = File(videoDir, "index.json")
            mediaJsonFile.writeText(MiaoJson.toJson(mediaFileInfo))

            if (currentDownloadInfo.taskId != currentTaskId) return

            when (mediaFileInfo) {
                is BiliDownloadMediaFileInfo.Type2 -> {
                    val videoInfo = currentDownloadInfo.copy(
                        url = mediaFileInfo.video[0].base_url,
                        header = httpHeader,
                        size = entry.total_bytes,
                        length = mediaFileInfo.duration,
                    )
                    _curDownload.value = videoInfo
                    downloadFile(videoInfo, File(videoDir, "video.m4s"))

                    val audio = mediaFileInfo.audio
                    if (audio != null && audio.isNotEmpty()) {
                        val audioInfo = CurrentDownloadInfo(
                            taskId = currentDownloadInfo.taskId,
                            parentDirPath = currentDownloadInfo.parentDirPath,
                            parentId = currentDownloadInfo.parentId,
                            id = currentDownloadInfo.id,
                            name = entry.name,
                            url = audio[0].base_url,
                            header = httpHeader,
                            size = audio[0].size,
                            length = mediaFileInfo.duration,
                        )
                        downloadFile(audioInfo, File(videoDir, "audio.m4s"))
                    }

                    entry.page_data?.let {
                        entry.page_data = it.copy(
                            height = mediaFileInfo.video[0].height,
                            width = mediaFileInfo.video[0].width,
                        )
                    }
                    entry.ep?.let {
                        entry.ep = it.copy(
                            height = mediaFileInfo.video[0].height,
                            width = mediaFileInfo.video[0].width,
                        )
                    }
                    updateEntryJson(info.entryDirPath, entry)

                    if (_curDownload.value?.status != CurrentDownloadInfo.STATUS_PAUSE) {
                        muxDashToMkv(videoDir)
                    }
                    completeDownload(info)
                }
                is BiliDownloadMediaFileInfo.Type1 -> {
                    val segment = mediaFileInfo.segment_list[0]
                    val segInfo = currentDownloadInfo.copy(
                        url = segment.url,
                        header = httpHeader,
                        size = segment.bytes,
                        length = segment.duration,
                    )
                    _curDownload.value = segInfo
                    val videoFile = File(videoDir, "0." + mediaFileInfo.format)
                    downloadFile(segInfo, videoFile)

                    completeDownload(info)
                }
                else -> {
                    _curDownload.value = currentDownloadInfo.copy(
                        status = CurrentDownloadInfo.STATUS_FAIL_PLAYURL,
                    )
                }
            }
        } catch (e: Exception) {
            _curDownload.value = currentDownloadInfo.copy(
                status = CurrentDownloadInfo.STATUS_FAIL_PLAYURL,
            )
        }
    }

    private suspend fun fetchPlayUrl(entry: BiliDownloadEntryInfo): BiliDownloadMediaFileInfo {
        val page = entry.page_data
        if (page != null) {
            return fetchVideoPlayUrl(entry, page)
        }
        val ep = entry.ep
        val source = entry.source
        if (ep != null && source != null) {
            return fetchBangumiPlayUrl(entry, source, ep)
        }
        return BiliDownloadMediaFileInfo.None("missing page or ep info")
    }

    private suspend fun fetchVideoPlayUrl(
        entry: BiliDownloadEntryInfo,
        pageData: BiliDownloadEntryInfo.PageInfo,
    ): BiliDownloadMediaFileInfo {
        val res = BiliApiService.playerAPI.getVideoPalyUrl(
            entry.avid!!.toString(),
            pageData.cid.toString(),
            entry.prefered_video_quality,
            if (entry.media_type == 1) 1 else 4048,
        )
        val dash = res.dash
        if (dash != null) {
            val videoDash = dash.video.firstOrNull { it.id == res.quality }
                ?: dash.video.first()
            val videoFile = BiliDownloadMediaFileInfo.Type2File(
                id = videoDash.id,
                base_url = videoDash.base_url,
                backup_url = videoDash.backup_url,
                bandwidth = videoDash.bandwidth,
                codecid = videoDash.codecid,
                size = 0,
                md5 = "",
                no_rexcode = false,
                frame_rate = videoDash.frame_rate,
                width = videoDash.width,
                height = videoDash.height,
                dash_drm_type = 0,
            )
            val audioFileList = dash.audio?.firstOrNull()?.let { audioDash ->
                listOf(
                    BiliDownloadMediaFileInfo.Type2File(
                        id = audioDash.id,
                        base_url = audioDash.base_url,
                        backup_url = audioDash.backup_url,
                        bandwidth = audioDash.bandwidth,
                        codecid = audioDash.codecid,
                        size = 0,
                        md5 = "",
                        no_rexcode = false,
                        frame_rate = audioDash.frame_rate,
                        width = audioDash.width,
                        height = audioDash.height,
                        dash_drm_type = 0,
                    )
                )
            } ?: emptyList()
            return BiliDownloadMediaFileInfo.Type2(
                duration = dash.duration,
                video = listOf(videoFile),
                audio = audioFileList,
                user_agent = PlayerAPI().DEFAULT_USER_AGENT,
                referer = PlayerAPI().DEFAULT_REFERER,
            )
        } else {
            val durl = res.durl ?: return BiliDownloadMediaFileInfo.None("no video URL")
            val segmentList = durl.map { item ->
                BiliDownloadMediaFileInfo.Type1Segment(
                    backup_urls = listOf(),
                    bytes = item.size,
                    duration = item.length,
                    md5 = "",
                    meta_url = "",
                    order = item.order,
                    url = item.url,
                )
            }
            val description = res.support_formats.find { res.quality == it.quality }
                ?.new_description ?: "清晰 480P"
            return BiliDownloadMediaFileInfo.Type1(
                from = pageData.from,
                quality = entry.prefered_video_quality,
                type_tag = entry.type_tag,
                description = description,
                player_codec_config_list = listOf(
                    BiliDownloadMediaFileInfo.Type1PlayerCodecConfig("IJK_PLAYER", false),
                    BiliDownloadMediaFileInfo.Type1PlayerCodecConfig("ANDROID_PLAYER", false),
                ),
                segment_list = segmentList,
                parse_timestamp_milli = 0,
                available_period_milli = 0,
                is_downloaded = false,
                is_resolved = true,
                time_length = 0,
                marlin_token = "",
                video_codec_id = 0,
                video_project = true,
                format = res.format,
                player_error = 0,
                need_vip = false,
                need_login = false,
                intact = false,
                user_agent = PlayerAPI().DEFAULT_USER_AGENT,
                referer = PlayerAPI().DEFAULT_REFERER,
            )
        }
    }

    private suspend fun fetchBangumiPlayUrl(
        entry: BiliDownloadEntryInfo,
        source: BiliDownloadEntryInfo.SourceInfo,
        ep: BiliDownloadEntryInfo.EpInfo,
    ): BiliDownloadMediaFileInfo {
        val res = BiliApiService.playerAPI.getBangumiUrl(
            ep.episode_id.toString(),
            source.cid.toString(),
            entry.prefered_video_quality,
            if (entry.media_type == 1) 1 else 4048,
        )
        val dash = res.dash
        if (dash != null) {
            val videoDash = dash.video.firstOrNull { it.id == res.quality }
                ?: dash.video.first()
            val videoFile = BiliDownloadMediaFileInfo.Type2File(
                id = videoDash.id,
                base_url = videoDash.base_url,
                backup_url = videoDash.backup_url,
                bandwidth = videoDash.bandwidth,
                codecid = videoDash.codecid,
                size = 0,
                md5 = "",
                no_rexcode = false,
                frame_rate = videoDash.frame_rate,
                width = videoDash.width,
                height = videoDash.height,
                dash_drm_type = 0,
            )
            val audioFileList = dash.audio?.firstOrNull()?.let { audioDash ->
                listOf(
                    BiliDownloadMediaFileInfo.Type2File(
                        id = audioDash.id,
                        base_url = audioDash.base_url,
                        backup_url = audioDash.backup_url,
                        bandwidth = audioDash.bandwidth,
                        codecid = audioDash.codecid,
                        size = 0,
                        md5 = "",
                        no_rexcode = false,
                        frame_rate = audioDash.frame_rate,
                        width = audioDash.width,
                        height = audioDash.height,
                        dash_drm_type = 0,
                    )
                )
            } ?: emptyList()
            return BiliDownloadMediaFileInfo.Type2(
                duration = dash.duration,
                video = listOf(videoFile),
                audio = audioFileList,
                user_agent = PlayerAPI().DEFAULT_USER_AGENT,
                referer = PlayerAPI().DEFAULT_REFERER,
            )
        } else {
            val durl = res.durl ?: return BiliDownloadMediaFileInfo.None("no video URL")
            val segmentList = durl.map { item ->
                BiliDownloadMediaFileInfo.Type1Segment(
                    backup_urls = listOf(),
                    bytes = item.size,
                    duration = item.length,
                    md5 = "",
                    meta_url = "",
                    order = item.order,
                    url = item.url,
                )
            }
            val description = res.support_formats.find { res.quality == it.quality }
                ?.new_description ?: "清晰 480P"
            return BiliDownloadMediaFileInfo.Type1(
                from = ep.from,
                quality = entry.prefered_video_quality,
                type_tag = entry.type_tag,
                description = description,
                player_codec_config_list = listOf(
                    BiliDownloadMediaFileInfo.Type1PlayerCodecConfig("IJK_PLAYER", false),
                    BiliDownloadMediaFileInfo.Type1PlayerCodecConfig("ANDROID_PLAYER", false),
                ),
                segment_list = segmentList,
                parse_timestamp_milli = 0,
                available_period_milli = 0,
                is_downloaded = false,
                is_resolved = true,
                time_length = 0,
                marlin_token = "",
                video_codec_id = 0,
                video_project = true,
                format = res.format,
                player_error = 0,
                need_vip = false,
                need_login = false,
                intact = false,
                user_agent = PlayerAPI().DEFAULT_USER_AGENT,
                referer = PlayerAPI().DEFAULT_REFERER,
            )
        }
    }

    private fun muxDashToMkv(videoDir: File) {
        runCatching {
            val videoFile = File(videoDir, "video.m4s")
            val audioFile = File(videoDir, "audio.m4s")
            val mkvFile = File(videoDir, "video.mkv")
            if (!videoFile.exists() || !audioFile.exists()) return
            if (mkvFile.exists()) return

            val process = ProcessBuilder(
                "ffmpeg",
                "-i", videoFile.absolutePath,
                "-i", audioFile.absolutePath,
                "-c", "copy",
                mkvFile.absolutePath,
                "-y",
            )
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            if (exitCode == 0 && mkvFile.exists()) {
                videoFile.delete()
                audioFile.delete()
            }
        }
    }

    private suspend fun downloadFile(info: CurrentDownloadInfo, file: File) {
        withContext(Dispatchers.IO) {
            val downloadLength = if (file.exists()) file.length() else 0L
            val requestBuilder = Request.Builder()
                .url(UrlUtil.autoHttps(info.url))
            if (downloadLength > 0 && info.size != 0L) {
                if (info.size == downloadLength) return@withContext
                requestBuilder.addHeader("RANGE", "bytes=$downloadLength-${info.size}")
            }
            for ((key, value) in info.header) {
                requestBuilder.addHeader(key, value)
            }
            val response = client.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            val body = response.body ?: throw Exception("empty body")

            val progressInfo = info.copy(status = CurrentDownloadInfo.STATUS_DOWNLOADING)
            if (info.size == 0L) {
                progressInfo.size = body.contentLength()
            }
            _curDownload.value = progressInfo

            val bis = BufferedInputStream(body.byteStream())
            val fos = FileOutputStream(file, true)
            val buffer = ByteArray(2048)
            var len: Int
            var downloaded = downloadLength
            while (bis.read(buffer).also { len = it } != -1
                && _curDownload.value?.status == CurrentDownloadInfo.STATUS_DOWNLOADING
            ) {
                fos.write(buffer, 0, len)
                downloaded += len
                progressInfo.progress = downloaded
                _curDownload.value = progressInfo
            }
            fos.flush()
            fos.close()
            bis.close()
        }
    }

    private fun completeDownload(info: BiliDownloadEntryAndPathInfo) {
        val entry = info.entry
        entry.downloaded_bytes = entry.total_bytes
        entry.is_completed = true
        entry.total_time_milli = (_curDownload.value?.length ?: 0L) * 1000
        updateEntryJson(info.entryDirPath, entry)
        _downloadListVersion.value++
        _curDownload.value = null
        nextDownload()
    }

    override fun cancelDownload(taskId: Long) {
        if (taskId == currentTaskId) {
            _curDownload.value?.status = CurrentDownloadInfo.STATUS_PAUSE
            currentDownloadJob?.cancel()
            currentDownloadJob = null
            currentTaskId = 0L
            stopDownload()
        }
    }

    private fun stopDownload() {
        _curDownload.value?.let { cur ->
            val info = _downloadList.find { cur.id == it.entry.key }
            if (info != null) {
                info.entry.total_bytes = cur.size
                info.entry.downloaded_bytes = cur.progress
                updateEntryJson(info.entryDirPath, info.entry)
                _downloadListVersion.value++
            }
        }
        _curDownload.value = null
        nextDownload()
    }

    override fun deleteDownload(pageDirPath: String, entryDirPath: String) {
        val index = _downloadList.indexOfFirst {
            it.pageDirPath == pageDirPath && it.entryDirPath == entryDirPath
        }
        if (index != -1) {
            val info = _downloadList[index]
            if (_curDownload.value?.id == info.entry.key) {
                cancelDownload(currentTaskId)
            }
        }
        val entryDir = File(entryDirPath)
        if (entryDir.exists()) entryDir.deleteRecursively()
        val pageDir = File(pageDirPath)
        if (pageDir.exists() && (pageDir.listFiles()?.isEmpty() == true)) {
            pageDir.delete()
        }
        if (index != -1) {
            _downloadList.removeAt(index)
            _downloadListVersion.value++
        }
    }

    private fun nextDownload() {
        if (waitDownloadQueue.isNotEmpty()) {
            val next = waitDownloadQueue.removeAt(0)
            if (_downloadList.indexOfFirst { it.entry.key == next.entry.key } != -1) {
                startDownload(next)
            } else {
                nextDownload()
            }
        }
    }

    private fun loadDownloadList() {
        val downloadDir = File(getDownloadPath())
        val list = mutableListOf<BiliDownloadEntryAndPathInfo>()
        if (downloadDir.exists()) {
            downloadDir.listFiles()
                .filter { it.isDirectory }
                .forEach { list.addAll(readDownloadDirectory(it.absolutePath)) }
        }
        _downloadList = list.reversed().toMutableList()
    }

    private fun updateEntryJson(entryDirPath: String, entry: BiliDownloadEntryInfo) {
        val entryJsonFile = File(entryDirPath, "entry.json")
        entryJsonFile.writeText(MiaoJson.toJson(entry))
    }

    private fun getDownloadFileDir(biliEntry: BiliDownloadEntryInfo): File {
        var dirName = ""
        var pageDirName = ""
        val ep = biliEntry.ep
        if (ep != null) {
            dirName = "s_" + biliEntry.season_id!!
            pageDirName = ep.episode_id.toString()
        }
        val page = biliEntry.page_data
        if (page != null) {
            dirName = biliEntry.avid!!.toString()
            pageDirName = "c_" + page.cid
        }
        val downloadDir = File(getDownloadPath(), dirName)
        if (!downloadDir.exists()) downloadDir.mkdir()
        val pageDir = File(downloadDir, pageDirName)
        if (!pageDir.exists()) pageDir.mkdir()
        return pageDir
    }
}
