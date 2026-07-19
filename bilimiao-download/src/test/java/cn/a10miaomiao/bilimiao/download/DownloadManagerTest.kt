package cn.a10miaomiao.bilimiao.download

import cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadManagerTest {
    @Test
    fun shouldStopReadingWhenDownloadedLengthReachesExpectedSize() {
        val manager = DownloadManager(CoroutineScope(Dispatchers.Unconfined), CurrentDownloadInfo(
            taskId = 1,
            parentDirPath = "/tmp",
            parentId = "1",
            id = 1,
            name = "test",
            url = "https://example.com"
        ), object : DownloadManager.Callback {
            override fun onTaskRunning(info: CurrentDownloadInfo) = Unit
            override fun onTaskComplete(info: CurrentDownloadInfo) = Unit
            override fun onTaskError(info: CurrentDownloadInfo, error: Throwable) = Unit
        })

        assertTrue(manager.shouldStopReading(100, 100))
        assertTrue(manager.shouldStopReading(101, 100))
        assertFalse(manager.shouldStopReading(99, 100))
    }
}
