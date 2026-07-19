package cn.a10miaomiao.bilimiao.compose.common.download

import android.content.Context
import android.util.Log
import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryAndPathInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.compose.common.download.entry.CurrentDownloadInfo
import cn.a10miaomiao.bilimiao.download.DownloadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class DownloadManagerAndroid(
    private val context: Context,
) : DownloadManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _downloadListVersion = MutableStateFlow(0)
    private val _curDownload = MutableStateFlow<CurrentDownloadInfo?>(null)
    private val pendingCreates = mutableListOf<BiliDownloadEntryInfo>()
    private val pendingLock = Any()

    private val service: DownloadService?
        get() = DownloadService.instance

    init {
        Log.d("DownloadDebug", "DownloadManagerAndroid init start")
        DownloadService.startService(context.applicationContext)
        scope.launch {
            var waited = 0
            while (waited < 100) {
                val svc = service
                if (svc != null) {
                    Log.d("DownloadDebug", "DownloadManagerAndroid service connected")
                    launch { svc.downloadListVersion.collect { _downloadListVersion.value = it } }
                    launch { svc.curDownload.collect { raw -> _curDownload.value = raw?.toCommon() } }
                    flushPendingCreates(svc)
                    return@launch
                }
                kotlinx.coroutines.delay(100)
                waited++
            }
            Log.e("DownloadDebug", "DownloadService failed to start after 10s")
        }
        Log.d("DownloadDebug", "DownloadManagerAndroid init done")
    }

    override val downloadListVersion: StateFlow<Int> get() = _downloadListVersion
    override val curDownload: StateFlow<CurrentDownloadInfo?> get() = _curDownload

    override val downloadList: List<BiliDownloadEntryAndPathInfo>
        get() = service?.downloadList?.map { it.toCommon() } ?: emptyList()

    override fun getDownloadPath(): String {
        val svc = service
        if (svc != null) return svc.getDownloadPath()
        val baseDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        return File(baseDir, "bilimiao_EN").also { it.mkdirs() }.canonicalPath
    }

    override fun readDownloadDirectory(dirPath: String): List<BiliDownloadEntryAndPathInfo> {
        return service?.readDownloadDirectory(File(dirPath))?.map { it.toCommon() } ?: emptyList()
    }

    private fun flushPendingCreates(svc: DownloadService) {
        val pending: List<BiliDownloadEntryInfo>
        synchronized(pendingLock) {
            if (pendingCreates.isEmpty()) {
                return
            }
            pending = pendingCreates.toList()
            pendingCreates.clear()
        }
        pending.forEach { entry ->
            Log.d("DownloadDebug", "DownloadManagerAndroid.flushPendingCreates title=${entry.title}")
            svc.createDownload(entry.toOriginal())
        }
    }

    override fun createDownload(biliEntry: BiliDownloadEntryInfo) {
        Log.d("DownloadDebug", "DownloadManagerAndroid.createDownload title=${biliEntry.title}")
        val svc = service
        if (svc != null) {
            Log.d("DownloadDebug", "DownloadManagerAndroid service available, forwarding directly")
            svc.createDownload(biliEntry.toOriginal())
        } else {
            Log.d("DownloadDebug", "DownloadManagerAndroid service null, queuing + starting service")
            synchronized(pendingLock) {
                pendingCreates.add(biliEntry)
            }
            DownloadService.startService(context.applicationContext)
        }
    }

    override fun startDownload(entryDirPath: String) {
        service?.startDownload(entryDirPath)
    }

    override fun cancelDownload(taskId: Long) {
        service?.cancelDownload(taskId)
    }

    override fun deleteDownload(pageDirPath: String, entryDirPath: String) {
        service?.deleteDownload(pageDirPath, entryDirPath)
    }
}

// ---- bilimiao-download 类型 → common 类型 ----

private fun cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryAndPathInfo.toCommon()
    = BiliDownloadEntryAndPathInfo(pageDirPath, entryDirPath, entry.toCommon())

private fun cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.toCommon()
    = BiliDownloadEntryInfo(
        media_type, has_dash_audio, is_completed, total_bytes, downloaded_bytes,
        title, type_tag, cover, video_quality, prefered_video_quality,
        quality_pithy_description, guessed_total_bytes, total_time_milli,
        danmaku_count, time_update_stamp, time_create_stamp, can_play_in_advance,
        interrupt_transform_temp_file, avid, spid, bvid, owner_id,
        page_data?.toCommon(), season_id, source?.toCommon(), ep?.toCommon(),
    )

private fun cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.PageInfo.toCommon()
    = BiliDownloadEntryInfo.PageInfo(cid, page, from, part, vid, has_alias, tid, width, height, rotate, download_title, download_subtitle)

private fun cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.SourceInfo.toCommon()
    = BiliDownloadEntryInfo.SourceInfo(av_id, cid)

private fun cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.EpInfo.toCommon()
    = BiliDownloadEntryInfo.EpInfo(av_id, page, danmaku, cover, episode_id, index, index_title, from, season_type, width, height, rotate, link, bvid, sort_index)

private fun cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo.toCommon()
    = CurrentDownloadInfo(taskId, parentDirPath, parentId, id, name, url, length, size, progress, status, header)

// ---- common 类型 → bilimiao-download 类型 ----

private fun BiliDownloadEntryInfo.toOriginal()
    = cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo(
        media_type, has_dash_audio, is_completed, total_bytes, downloaded_bytes,
        title, type_tag, cover, video_quality, prefered_video_quality,
        quality_pithy_description, guessed_total_bytes, total_time_milli,
        danmaku_count, time_update_stamp, time_create_stamp, can_play_in_advance,
        interrupt_transform_temp_file, avid, spid, bvid, owner_id,
        page_data?.let { cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.PageInfo(it.cid, it.page, it.from, it.part, it.vid, it.has_alias, it.tid, it.width, it.height, it.rotate, it.download_title, it.download_subtitle) },
        season_id,
        source?.let { cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.SourceInfo(it.av_id, it.cid) },
        ep?.let { cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo.EpInfo(it.av_id, it.page, it.danmaku, it.cover, it.episode_id, it.index, it.index_title, it.from, it.season_type, it.width, it.height, it.rotate, it.link, it.bvid, it.sort_index) },
    )
