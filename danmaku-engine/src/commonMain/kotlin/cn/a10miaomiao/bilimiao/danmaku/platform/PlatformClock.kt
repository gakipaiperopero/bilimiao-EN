package cn.a10miaomiao.bilimiao.danmaku.platform

/**
 * 平台时钟抽象
 */
// EN: Platform clock abstraction
expect object PlatformClock {
    /**
     * 获取系统启动至今的毫秒数（不含休眠）
     */
    // EN: Get uptime milliseconds (excluding sleep)
    fun uptimeMillis(): Long

    /**
     * 休眠指定毫秒
     */
    // EN: Sleep for specified milliseconds
    fun sleep(millis: Long)
}
