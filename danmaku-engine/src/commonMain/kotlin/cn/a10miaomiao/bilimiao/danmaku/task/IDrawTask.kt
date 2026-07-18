package cn.a10miaomiao.bilimiao.danmaku.task

import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.renderer.RenderingState

/**
 * 弹幕绘制任务接口
 *
 * 定义弹幕渲染任务的生命周期和绘制操作。
 * 负责管理弹幕数据的加载、过滤、布局和绘制。
 */
 // EN: Danmaku draw task interface. Defines the lifecycle and drawing operations of the danmaku rendering task.. Responsible for managing danmaku data loading, filtering, layout and drawing.
interface IDrawTask {

    companion object {
        /** 播放状态：播放中 */
        // EN: Play state: playing
        const val PLAY_STATE_PLAYING = 1

        /** 播放状态：暂停 */
        // EN: Play state: paused
        const val PLAY_STATE_PAUSE = 2
    }

    /**
     * 添加单条弹幕
     *
     * @param item 弹幕对象
     */
     // EN: Add a single danmaku
    fun addDanmaku(item: BaseDanmaku)

    /**
     * 移除所有弹幕
     *
     * @param isClearDanmakusOnScreen 是否同时清除屏幕上的弹幕
     */
     // EN: Remove all danmaku
    fun removeAllDanmakus(isClearDanmakusOnScreen: Boolean)

    /**
     * 移除所有直播弹幕
     */
    // EN: Remove all live danmaku
    fun removeAllLiveDanmakus()

    /**
     * 清除当前屏幕上的弹幕
     *
     * @param currMillis 当前时间（毫秒）
     */
     // EN: Clear current on-screen danmaku
    fun clearDanmakusOnScreen(currMillis: Long)

    /**
     * 获取指定时间点的可见弹幕
     *
     * @param time 时间点（毫秒）
     * @return 可见弹幕集合
     */
     // EN: Get visible danmaku at specified time
    fun getVisibleDanmakusOnTime(time: Long): IDanmakus

    /**
     * 执行弹幕绘制
     *
     * @param displayer 显示器
     * @return 渲染状态
     */
     // EN: Execute danmaku drawing
    fun draw(displayer: IDisplayer): RenderingState?

    /**
     * 重置绘制任务状态
     */
    // EN: Reset draw task state
    fun reset()

    /**
     * 跳转到指定时间
     *
     * @param mills 目标时间（毫秒）
     */
     // EN: Seek to specified time
    fun seek(mills: Long)

    /**
     * 启动绘制任务
     */
    // EN: Start draw task
    fun start()

    /**
     * 退出绘制任务，释放资源
     */
    // EN: Quit and release
    fun quit()

    /**
     * 准备绘制任务，加载弹幕数据
     */
    // EN: Prepare, load data
    fun prepare()

    /**
     * 播放状态变更通知
     *
     * @param state 播放状态（[PLAY_STATE_PLAYING] 或 [PLAY_STATE_PAUSE]）
     */
     // EN: Play state change notification
    fun onPlayStateChanged(state: Int)

    /**
     * 请求清除渲染状态
     */
    // EN: Request clear render state
    fun requestClear()

    /**
     * 请求清除弹幕位置保留器
     */
    // EN: Request clear retainer
    fun requestClearRetainer()

    /**
     * 请求同步弹幕时间偏移
     *
     * 用于播放器跳转后保持已在屏幕上的弹幕的相对位置。
     *
     * @param fromTimeMills 起始时间
     * @param toTimeMills 目标时间
     * @param offsetMills 时间偏移量
     */
     // EN: Request sync danmaku time offset. Used to maintain the relative position of on-screen danmaku after player seek.
    fun requestSync(fromTimeMills: Long, toTimeMills: Long, offsetMills: Long)

    /**
     * 设置弹幕解析器
     *
     * @param parser 弹幕解析器
     */
     // EN: Set danmaku parser
    fun setParser(parser: BaseDanmakuParser)

    /**
     * 使指定弹幕失效，触发重绘
     *
     * @param item 弹幕对象
     * @param remeasure 是否需要重新测量
     */
     // EN: Invalidate specified danmaku, trigger redraw
    fun invalidateDanmaku(item: BaseDanmaku, remeasure: Boolean)

    /**
     * 请求隐藏弹幕（不清除数据）
     */
    // EN: Request hide (keep data)
    fun requestHide()

    /**
     * 请求渲染（即使处于隐藏状态也触发一次绘制）
     */
    // EN: Request render (even if hidden)
    fun requestRender()

    /**
     * 绘制任务监听器
     */
    // EN: Draw task listener
    interface TaskListener {
        /**
         * 弹幕数据准备完成
         */
        // EN: Data ready
        fun ready()

        /**
         * 弹幕添加到列表
         *
         * @param danmaku 被添加的弹幕
         */
         // EN: Danmaku added to list
        fun onDanmakuAdd(danmaku: BaseDanmaku)

        /**
         * 弹幕首次显示在屏幕上
         *
         * @param danmaku 被显示的弹幕
         */
         // EN: Danmaku first shown on screen
        fun onDanmakuShown(danmaku: BaseDanmaku)

        /**
         * 弹幕配置变更
         */
        // EN: Config changed
        fun onDanmakuConfigChanged()

        /**
         * 所有弹幕绘制完成（最后一条弹幕已超时）
         */
        // EN: All drawing finished (last danmaku timed out)
        fun onDanmakusDrawingFinished()
    }
}
