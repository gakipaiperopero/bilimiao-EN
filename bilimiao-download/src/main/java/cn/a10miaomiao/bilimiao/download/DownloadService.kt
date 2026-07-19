
package cn.a10miaomiao.bilimiao.download

import android.Manifest
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryAndPathInfo
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadMediaFileInfo
import cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.CompressionTools
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import java.io.*
import kotlin.coroutines.CoroutineContext

class DownloadService: Service(), CoroutineScope, DownloadManager.Callback {
    companion object {
        private const val TAG = "DownloadService"
        private val channel = Channel<DownloadService>()
        private var _instance: DownloadService? = null

        val instance get() = _instance

        suspend fun getService(context: Context): DownloadService{
            _instance?.let { return it }
            startService(context)
            return channel.receive().also {
                _instance = it
            }
        }

        fun startService(context: Context) {
            Log.d("DownloadDebug", "DownloadService.startService")
            val intent = Intent(context, DownloadService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                context.startService(intent)
            } else {
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private val downloadNotify by lazy { DownloadNotify(this) }
    private var downloadManager: DownloadManager? = null
    private var audioDownloadManager: DownloadManager? = null
    private var currentTaskId = 1L
    private var idCounter = 1L
    private var downloadTimeoutJob: Job? = null

    private var audioDownloadManagerCallback = object : DownloadManager.Callback {
        override fun onTaskRunning(info: CurrentDownloadInfo) {
        }

        override fun onTaskComplete(info: CurrentDownloadInfo) {
            if (downloadManager?.downloadInfo?.status == CurrentDownloadInfo.STATUS_COMPLETED) {
                downloadNotify.showCompletedStatusNotify(info)
                completeDownload()
            }
        }

        override fun onTaskError(info: CurrentDownloadInfo, error: Throwable) {
            Log.e("DownloadDebug", "Audio download failed: ${error.message}")
            if (downloadManager?.downloadInfo?.status == CurrentDownloadInfo.STATUS_COMPLETED) {
                Log.d("DownloadDebug", "Video is complete, completing download without audio")
                downloadNotify.showCompletedStatusNotify(info)
                completeDownload()
            }
        }

    }

    var downloadList = mutableListOf<BiliDownloadEntryAndPathInfo>()
    var downloadListVersion = MutableStateFlow(0)
    var waitDownloadQueue = mutableListOf<BiliDownloadEntryAndPathInfo>()
    val curDownload = MutableStateFlow<CurrentDownloadInfo?>(null)
    private val curBiliDownloadEntryAndPathInfo: BiliDownloadEntryAndPathInfo?
        get() = curDownload.value?.let { cur ->
            downloadList.find { it.entry.key == cur.id }
        }
    private var curMediaFile: File? = null
    private var curMediaFileInfo: BiliDownloadMediaFileInfo? = null


    override fun onCreate() {
        super.onCreate()
        Log.d("DownloadDebug", "DownloadService onCreate")
        _instance = this
        job = Job()
        startForegroundIfPossible()
        launch {
            readDownloadList()
            channel.send(this@DownloadService)
        }
        launch {
            curDownload.collect { info ->
                if (info == null) {
                    downloadNotify.cancel()
                } else {
                    downloadNotify.notifyData(info)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelDownloadTimeout()
        job.cancel()
        _instance = null
    }

    private fun startForegroundIfPossible() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            val notification = downloadNotify.builder
                .setContentTitle("Download service")
                .setContentText("Preparing downloads")
                .setProgress(100, 0, true)
                .setOngoing(true)
                .build()
            startForeground(downloadNotify.notificationID, notification)
        } catch (_: Exception) {
            // Ignore on older or restricted platforms; downloads should still proceed.
        }
    }

    private fun readDownloadList() {
        val downloadDir = File(getDownloadPath())
        val list = mutableListOf<BiliDownloadEntryAndPathInfo>()
        val children = downloadDir.listFiles() ?: emptyArray()
        children
            .filter { it.isDirectory }
            .forEach {
                list.addAll(readDownloadDirectory(it))
            }
        downloadList = list.reversed().toMutableList()
    }

    fun readDownloadDirectory(dir: File): List<BiliDownloadEntryAndPathInfo>{
        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }
        return dir.listFiles()?.filter { pageDir -> pageDir.isDirectory }
            ?.map { File(it.path, "entry.json") }
            ?.filter { it.exists() }
            ?.map {
                val entryJson = it.readText()
                val entry = MiaoJson.fromJson<BiliDownloadEntryInfo>(entryJson)
                BiliDownloadEntryAndPathInfo(
                    entry = entry,
                    entryDirPath = it.parent,
                    pageDirPath = it.parentFile.parent
                )
            } ?: emptyList()
    }

    /**
     * 是否处于等待下载队列中
     */
    fun isInWaitDownloadQueue(dirPath: String): Boolean {
        return waitDownloadQueue.indexOfFirst { it.entryDirPath == dirPath } > 0
    }

    /**
     * 创建任务
     */
    // EN: Create task
    fun createDownload(
        biliEntry: BiliDownloadEntryInfo
    ) {
        Log.d("DownloadDebug", "createDownload title=${biliEntry.title} avid=${biliEntry.avid} season=${biliEntry.season_id}")
        val entryDir = getDownloadFileDir(biliEntry)
        // 保存视频信息
        // EN: Save video info
        val entryJsonFile = File(entryDir, "entry.json")
        val entryJsonStr = MiaoJson.toJson(biliEntry)
        entryJsonFile.writeText(entryJsonStr)
        val biliDownInfo = BiliDownloadEntryAndPathInfo(
            entry = biliEntry,
            pageDirPath = entryDir.parent,
            entryDirPath = entryDir.absolutePath,
        )
        val index = downloadList.indexOfFirst {
            if (biliEntry.avid != null) {
                biliEntry.avid == it.entry.avid
            } else {
                biliEntry.season_id == it.entry.season_id
            }
        }
        downloadList.add(index + 1, biliDownInfo)
        downloadListVersion.value++
        if (curDownload.value == null) {
            startDownload(biliDownInfo)
        } else {
            waitDownloadQueue.add(biliDownInfo)
        }
    }

    fun startDownload(entryDirPath: String) {
        Log.d("DownloadDebug", "startDownload entryDirPath=$entryDirPath")
        val biliDownInfo = downloadList.find {
            it.entryDirPath == entryDirPath
        }
        if (biliDownInfo != null) {
            startDownload(biliDownInfo)
        } else {
//            val entryFile = File(entryDirPath, "entry.json")
//            if (entryFile.exists()) {
//
//            }
        }
    }
    /**
     * 开始任务
     */
    // EN: Start task
    fun startDownload(biliDownInfo: BiliDownloadEntryAndPathInfo) = launch {
        Log.d("DownloadDebug", "startDownload task entryDir=${biliDownInfo.entryDirPath}")
        // 取消当前任务
        // EN: Cancel current task
        downloadManager?.cancel()
        audioDownloadManager?.cancel()
        downloadManager = null
        audioDownloadManager = null
        
        // Cancel any existing timeout job
        downloadTimeoutJob?.cancel()
        
        // Start a watchdog timer to prevent hanging
        startDownloadTimeout()
        
        // 开始任务/继续任务
        // EN: Start task / Resume task
        val entryDir = File(biliDownInfo.entryDirPath)
        val danmakuXMLFile = File(entryDir, "danmaku.xml")
        val entry = biliDownInfo.entry
        val parentId = entry.season_id ?: entry.avid?.toString() ?: ""
        val id = entry.page_data?.cid ?: entry.source?.cid ?: 0L
        currentTaskId = idCounter++
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
        curDownload.value = currentDownloadInfo.copy(
            status = CurrentDownloadInfo.STATUS_PREPARING,
        )
        downloadVideo(currentDownloadInfo, biliDownInfo)
    }

    private suspend fun downloadVideo(
        currentDownloadInfo: CurrentDownloadInfo,
        biliDownInfo: BiliDownloadEntryAndPathInfo,
    ) {
        if (currentDownloadInfo.taskId != currentTaskId) {
            return
        }
        val entry = biliDownInfo.entry
        val entryDir = File(biliDownInfo.entryDirPath)
        val videoDir = File(entryDir, entry.type_tag)
        if (!videoDir.exists()) {
            videoDir.mkdir()
        }
        try {
            curDownload.value = currentDownloadInfo.copy(
                status = CurrentDownloadInfo.STATUS_GET_PLAYURL,
            )
            val mediaFileInfo = BiliPalyUrlHelper.playUrl(entry)
            if (mediaFileInfo !is BiliDownloadMediaFileInfo.Type2) {
                curDownload.value = currentDownloadInfo.copy(
                    status = CurrentDownloadInfo.STATUS_FAIL_PLAYURL,
                )
                return
            }

            val httpHeader = mediaFileInfo.httpHeader()
            val mediaJsonFile = File(videoDir, "index.json")
            mediaJsonFile.writeText(MiaoJson.toJson(mediaFileInfo))

            if (currentDownloadInfo.taskId != currentTaskId) {
                return
            }

            curMediaFile = mediaJsonFile
            curMediaFileInfo = mediaFileInfo

            val targetFile = File(videoDir, "video.mkv")
            val videoFile = File(videoDir, "video.m4s")
            val audioFile = File(videoDir, "audio.m4s")

            val downloadUrl = mediaFileInfo.video[0].base_url
            val downloadSize = mediaFileInfo.video[0].size.takeIf { it > 0 } ?: entry.total_bytes
            Log.d("DownloadDebug", "Starting video download: url=$downloadUrl size=$downloadSize")
            downloadManager = DownloadManager(this, currentDownloadInfo.copy(
                url = downloadUrl,
                header = httpHeader,
                size = downloadSize,
                length = mediaFileInfo.duration
            ), this)
            curDownload.value = currentDownloadInfo.copy(
                status = CurrentDownloadInfo.STATUS_DOWNLOADING,
            )
            downloadManager?.start(videoFile)

            val audio = mediaFileInfo.audio
            if (audio != null && audio.isNotEmpty()) {
                audioDownloadManager = DownloadManager(this, CurrentDownloadInfo(
                    taskId = currentDownloadInfo.taskId,
                    parentDirPath = currentDownloadInfo.parentDirPath,
                    parentId = currentDownloadInfo.parentId,
                    id = currentDownloadInfo.id,
                    name = entry.name,
                    url = audio[0].base_url,
                    header = httpHeader,
                    size = audio[0].size,
                    length = mediaFileInfo.duration
                ), audioDownloadManagerCallback)
                audioDownloadManager?.start(audioFile)
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
            updateBiliDownloadEntryJson(biliDownInfo.entryDirPath, entry)

            // Wait until both files are downloaded, then mux them into a single MKV.
            while (
                curDownload.value?.status == CurrentDownloadInfo.STATUS_DOWNLOADING ||
                curDownload.value?.status == CurrentDownloadInfo.STATUS_AUDIO_DOWNLOADING ||
                audioDownloadManager?.downloadInfo?.status == CurrentDownloadInfo.STATUS_DOWNLOADING
            ) {
                delay(200)
            }

            if (videoFile.exists() && audioFile.exists()) {
                muxVideoAudioToMkv(videoFile, audioFile, targetFile)
            }
        } catch (e: Exception) {
            curDownload.value = currentDownloadInfo.copy(
                status = CurrentDownloadInfo.STATUS_FAIL_PLAYURL,
            )
            e.printStackTrace()
            downloadManager?.cancel()
            audioDownloadManager?.cancel()
            nextDownload()
        }
    }

    fun cancelDownload(taskId: Long) {
        cancelDownloadTimeout()
        if (taskId == currentTaskId) {
            downloadManager?.cancel()
            audioDownloadManager?.cancel()
            downloadManager = null
            audioDownloadManager = null
            currentTaskId = 0L
            stopDownload()
        }
    }

    /**
     * 结束当前任务
     */
    fun stopDownload () {
        curDownload.value?.let { cur ->
            val entryAndPathInfo = downloadList.find {
                cur.id == it.entry.key
            }
            if (entryAndPathInfo != null) {
                entryAndPathInfo.entry.total_bytes = cur.size
                entryAndPathInfo.entry.downloaded_bytes = cur.progress
                updateBiliDownloadEntryJson(
                    entryAndPathInfo.entryDirPath,
                    entryAndPathInfo.entry,
                )
                downloadListVersion.value++
                downloadManager?.cancel()
            }
        }
        curDownload.value = null
        curMediaFile = null
        curMediaFileInfo = null
        nextDownload()
    }

    /**
     * 删除当前任务
     */
    // EN: Delete current task
    fun deleteDownload (
        pageDirPath: String,
        entryDirPath: String,
    ) {
        val index = downloadList.indexOfFirst {
            it.pageDirPath == pageDirPath && it.entryDirPath == entryDirPath
        }
        if (index != -1) {
            // 如果为当前下载任务则先停止任务
            // EN: If it's the current download task, stop it first
            val entryAndPathInfo = downloadList[index]
            if (curDownload.value?.id == entryAndPathInfo.entry.key) {
                cancelDownload(currentTaskId)
            }
        }
        val downloadDir = File(pageDirPath)
        if (downloadDir.exists()) {
            val entryDir = File(entryDirPath)
            if (entryDir.exists()) {
                entryDir.deleteRecursively()
            }
            if (downloadDir.listFiles()?.size == 0) {
                downloadDir.delete()
            }
        }
        if (index != -1) {
            // 从列表移除
            // EN: Remove from list
            downloadList.removeAt(index)
            downloadListVersion.value++
        }
    }

    /**
     * Start download timeout watchdog
     */
    private fun startDownloadTimeout() {
        downloadTimeoutJob = launch {
            delay(120000) // 2 minutes timeout (reduced from 5 min)
            if (curDownload.value?.status == CurrentDownloadInfo.STATUS_DOWNLOADING) {
                Log.w("DownloadDebug", "Download timed out after 2 minutes")
                downloadManager?.cancel()
                audioDownloadManager?.cancel()
                curDownload.value = curDownload.value?.copy(
                    status = CurrentDownloadInfo.STATUS_FAIL_DOWNLOAD
                )
                downloadNotify.showErrorStatusNotify(curDownload.value!!)
                nextDownload()
            }
        }
    }

    /**
     * Cancel download timeout watchdog
     */
    private fun cancelDownloadTimeout() {
        downloadTimeoutJob?.cancel()
        downloadTimeoutJob = null
    }

    /**
     * 完成下载
     */
    private fun completeDownload() {
        cancelDownloadTimeout()
        val (_, entryDirPath, entry) = curBiliDownloadEntryAndPathInfo ?: return
        entry.downloaded_bytes = entry.total_bytes
        entry.total_bytes = entry.total_bytes
        entry.is_completed = true
        entry.total_time_milli = (curDownload.value?.length ?: 0L) * 1000
        updateBiliDownloadEntryJson(entryDirPath, entry)

        if (entry.type_tag != null) {
            val videoDir = File(entryDirPath, entry.type_tag)
            val videoFile = File(videoDir, "video.m4s")
            val audioFile = File(videoDir, "audio.m4s")
            val mkvFile = File(videoDir, "video.mkv")
            finalizeDownloadedFile(videoFile, audioFile, mkvFile)
        }
        downloadListVersion.value++
        curDownload.value = null
        curMediaFile = null
        curMediaFileInfo = null
        downloadManager = null
        audioDownloadManager = null
        nextDownload()
    }

    private fun finalizeDownloadedFile(videoFile: File, audioFile: File, targetFile: File) {
        if (!videoFile.exists()) return
        if (targetFile.exists()) return

        targetFile.parentFile?.mkdirs()
        if (audioFile.exists()) {
            muxVideoAudioToMkv(videoFile, audioFile, targetFile)
        } else {
            runCatching {
                val mp4File = File(targetFile.parent, "video.mp4")
                videoFile.copyTo(mp4File, overwrite = true)
                videoFile.delete()
                copyToPublicDownloads(mp4File, "video/mp4")
                return
            }.onFailure { e ->
                e.printStackTrace()
            }
        }

        if (targetFile.exists()) {
            copyToPublicDownloads(targetFile, "video/x-matroska")
        }
    }

    private fun copyToPublicDownloads(file: File, mimeType: String) {
        try {
            val displayName = "bilimiao_${System.currentTimeMillis()}.${file.extension}"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/bilimiao_EN")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(file).copyTo(out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }
            } else {
                val destDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val destFile = File(destDir, "bilimiao_EN/$displayName")
                destFile.parentFile?.mkdirs()
                file.copyTo(destFile, overwrite = true)
            }
            Log.d("DownloadDebug", "Copied to public Downloads: $displayName")
        } catch (e: Exception) {
            Log.e("DownloadDebug", "Failed to copy to public Downloads: ${e.message}")
        }
    }

    private fun copyToPublicDownloads(file: File) {
        try {
            val displayName = "bilimiao_${System.currentTimeMillis()}.mkv"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                    put(MediaStore.Downloads.MIME_TYPE, "video/x-matroska")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/bilimiao_EN")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(file).copyTo(out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }
            } else {
                val destDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val destFile = File(destDir, "bilimiao_EN/$displayName")
                destFile.parentFile?.mkdirs()
                file.copyTo(destFile, overwrite = true)
            }
            Log.d("DownloadDebug", "Copied to public Downloads: $displayName")
        } catch (e: Exception) {
            Log.e("DownloadDebug", "Failed to copy to public Downloads: ${e.message}")
        }
    }

    private fun muxVideoAudioToMkv(videoFile: File, audioFile: File, targetFile: File) {
        runCatching {
            if (!videoFile.exists() || !audioFile.exists()) return
            if (targetFile.exists()) return

            val process = ProcessBuilder(
                "ffmpeg",
                "-i", videoFile.absolutePath,
                "-i", audioFile.absolutePath,
                "-c", "copy",
                targetFile.absolutePath,
                "-y",
            )
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            if (exitCode == 0 && targetFile.exists()) {
                videoFile.delete()
                audioFile.delete()
            }
        }
    }

    /**
     * 完成下载
     */
    // EN: Complete download
    private fun nextDownload() {
        if (waitDownloadQueue.isNotEmpty()) {
            val next = waitDownloadQueue[0]
            waitDownloadQueue.removeAt(0)
            if (downloadList.indexOfFirst { it.entry.key == next.entry.key } != -1) {
                startDownload(next)
            } else {
                nextDownload()
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onTaskRunning(info: CurrentDownloadInfo) {
        Log.d("DownloadDebug", "Download progress: ${info.progress}/${info.size} (${info.rate * 100}%)")
        
        // 获取视频文件长度
        // EN: Get video file length
        if (info.progress == 0L && info.size != 0L) {
            (curMediaFileInfo as BiliDownloadMediaFileInfo.Type2)?.let {
                if (it.video[0].size == 0L && info.size != 0L) {
                    it.video[0].size = info.size
                    val mediaJsonStr = MiaoJson.toJson(it)
                    curMediaFile?.writeText(mediaJsonStr)
                }
            }
            val entryAndPathInfo = downloadList.find {
                info.id == it.entry.key
            }
            if (entryAndPathInfo != null) {
                entryAndPathInfo.entry.total_bytes = info.size
                updateBiliDownloadEntryJson(
                    entryAndPathInfo.entryDirPath,
                    entryAndPathInfo.entry,
                )
                downloadListVersion.value++
            }
        }
        curDownload.value = info.copy()
    }

    override fun onTaskComplete(info: CurrentDownloadInfo) {
        if (info.size == 0L) {
            return
        }
        Log.d("DownloadDebug", "Download completed with progress=${info.progress}/${info.size}")
        when (audioDownloadManager?.downloadInfo?.status) {
            CurrentDownloadInfo.STATUS_DOWNLOADING,
            CurrentDownloadInfo.STATUS_WAIT -> {
                // 等待音频下载完成
                // EN: Wait for audio download to complete
                curDownload.value = info.copy(
                    status = CurrentDownloadInfo.STATUS_AUDIO_DOWNLOADING
                )
            }
            CurrentDownloadInfo.STATUS_FAIL_DOWNLOAD -> {
                // 重新下载音频
                // EN: Redownload audio
                curBiliDownloadEntryAndPathInfo?.let(::startDownload)
            }
            CurrentDownloadInfo.STATUS_COMPLETED, null -> {
                // 完成下载
                // EN: Download complete
                downloadNotify.showCompletedStatusNotify(info)
                completeDownload()
            }
        }
    }

    override fun onTaskError(info: CurrentDownloadInfo, error: Throwable) {
        error.printStackTrace()
        curDownload.value = info.copy(
            status = CurrentDownloadInfo.STATUS_FAIL_DOWNLOAD
        )
        downloadNotify.showErrorStatusNotify(info)
        val entryAndPathInfo = downloadList.find {
            info.id == it.entry.key
        }
        if (entryAndPathInfo != null) {
            entryAndPathInfo.entry.total_bytes = info.size
            entryAndPathInfo.entry.downloaded_bytes = info.progress
            updateBiliDownloadEntryJson(
                entryAndPathInfo.entryDirPath,
                entryAndPathInfo.entry,
            )
            downloadListVersion.value++
        }
        // Clean up and try next download
        downloadManager?.cancel()
        audioDownloadManager?.cancel()
        nextDownload()
    }

    private fun updateBiliDownloadEntryJson(
        entryDirPath: String,
        entry: BiliDownloadEntryInfo,
    ) {
        // 保存视频信息
        // EN: Save video info
        val entryJsonFile = File(entryDirPath, "entry.json")
        val entryJsonStr = MiaoJson.toJson(entry)
        entryJsonFile.writeText(entryJsonStr)
    }

    fun getDownloadPath(): String {
        val baseDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: filesDir
        val file = ensureDownloadDirectory(baseDir, "bilimiao_EN")
        return file.canonicalPath
    }

    internal fun ensureDownloadDirectory(baseDir: File, childDirName: String): File {
        val dir = File(baseDir, childDirName)
        dir.mkdirs()
        return dir
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
        // 创建文件夹
        // EN: Create folder
        if (!downloadDir.exists()) {
            downloadDir.mkdir()
        }
        val pageDir = File(downloadDir, pageDirName)
        if (!pageDir.exists()) {
            pageDir.mkdir()
        }
        return pageDir
    }

}