package cn.a10miaomiao.bilimiao.danmaku.task

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.AbsDanmakuSync
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.platform.PlatformClock
import cn.a10miaomiao.bilimiao.danmaku.renderer.IRenderer
import cn.a10miaomiao.bilimiao.danmaku.renderer.RenderingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.util.LinkedList

/**
 * 弹幕引擎控制器
 *
 * 协程驱动的弹幕渲染引擎，替代原 DrawHandler（基于 Android Handler）。
 * 负责管理渲染循环、计时器同步、播放状态机和弹幕绘制任务的生命周期。
 *
 * 使用方式：
 * 1. 创建实例并传入 CoroutineScope
 * 2. 调用 setConfig() 设置弹幕上下文
 * 3. 调用 setParser() 设置弹幕解析器
 * 4. 调用 prepare() 准备弹幕数据
 * 5. 在 onPrepared 回调后调用 start() 开始播放
 * 6. 在 UI 层的绘制回调中调用 draw(canvas) 进行渲染
 *
 * @param scope 协程作用域，用于启动渲染循环
 * @param danmakuVisible 弹幕是否初始可见
 */
 // EN: Danmaku engine controller. Coroutine-driven danmaku rendering engine, replaces original DrawHandler (based on Android Handler).. Responsible for managing rendering loop, timer sync, playback state machine, and danmaku draw task lifecycle.. Usage:. 1. Create instance with CoroutineScope. 2. Call setConfig() to set danmaku context. 3. Call setParser() to set danmaku parser. 4. Call prepare() to prepare danmaku data. 5. Call start() after onPrepared callback to begin playback. 6. Call draw(canvas) in UI layer's draw callback for rendering
class DanmakuEngine(
    private val scope: CoroutineScope,
    danmakuVisible: Boolean = true
) {

    /**
     * 弹幕引擎回调接口
     */
    // EN: Danmaku engine callback interface
    interface Callback {
        /** 弹幕数据准备完成 */
        // EN: Data ready
        fun prepared()

        /** 计时器更新，可用于同步外部播放器时间 */
        // EN: Timer update, for syncing external player
        fun updateTimer(timer: DanmakuTimer) {}

        /** 弹幕显示在屏幕上 */
        // EN: Danmaku shown on screen
        fun danmakuShown(danmaku: BaseDanmaku) {}

        /** 所有弹幕绘制完成 */
        // EN: All danmaku drawing finished
        fun drawingFinished() {}
    }

    companion object {
        private const val INDEFINITE_TIME = 10000000L
        private const val MAX_RECORD_SIZE = 500
        private const val FRAME_UPDATE_RATE = 16L
        private const val CORDON_TIME = 60L
        private const val CORDON_TIME2 = 150L
    }

    // region 状态
    // EN: region State

    /** 引擎是否处于停止状态 */
    // EN: Whether engine is stopped
    var quitFlag: Boolean = true
        private set

    /** 弹幕数据是否准备完成 */
    // EN: Whether danmaku data is ready
    private var mReady: Boolean = false

    /** 弹幕是否可见 */
    // EN: Whether danmaku is visible
    var isDanmakusVisible: Boolean = danmakuVisible
        private set

    /** 是否正在执行跳转 */
    // EN: Whether seeking
    private var mInSeekingAction: Boolean = false

    /** 跳转目标时间 */
    // EN: Seek target time
    private var mDesireSeekingTime: Long = 0L

    /** 是否正在同步计时器 */
    // EN: Whether syncing timer
    private var mInSyncAction: Boolean = false

    /** 是否处于等待渲染状态（空闲休眠） */
    // EN: Whether in waiting/idle sleep state
    private var mInWaitingState: Boolean = false

    /** 启用空闲休眠（无弹幕时暂停渲染循环） */
    // EN: Enable idle sleep (pause when no danmaku)
    var idleSleep: Boolean = true

    /** 启用非阻塞模式（由外部驱动渲染，不自行循环） */
    // EN: Enable non-block mode (externally driven)
    var nonBlockModeEnable: Boolean = false

    /** 外部播放器位置（毫秒），由 UI 线程写入，渲染线程读取 */
    // EN: External player position (ms); UI writes, render reads
    @Volatile
    var externalPlayerPosition: Long = -1L

    /** 上次同步的播放器位置，用于检测位置是否真正变化 */
    // EN: Last synced position; detects actual changes
    private var lastSyncedPosition: Long = -1L

    /** 上次播放器位置对应的 wall clock，用于插值估算实时位置 */
    // EN: Wall clock at last sync; for real-time interpolation
    private var lastSyncedWallTime: Long = 0L

    // endregion

    // region 依赖
    // EN: region Dependencies

    private var mContext: DanmakuContext? = null
    private var mParser: BaseDanmakuParser? = null
    private var mCallback: Callback? = null
    var drawTask: IDrawTask? = null
        private set

    // endregion

    // region 计时
    // EN: region Timing

    private val timer = DanmakuTimer()
    private var mTimeBase: Long = 0L
    private var pausedPosition: Long = 0L
    private var mRemainingTime: Long = 0L
    private var mLastDeltaTime: Long = 0L

    /** 渲染耗时记录 */
    // EN: Render time records
    private val mDrawTimes = LinkedList<Long>()

    /** 渲染状态 */
    // EN: Rendering state
    private val mRenderingState = RenderingState()

    // endregion

    // region 协程
    // EN: region Coroutines

    private var renderJob: Job? = null

    // endregion

    // region 配置
    // EN: region Configuration

    fun setConfig(config: DanmakuContext) {
        mContext = config
    }

    fun getConfig(): DanmakuContext? = mContext

    fun setParser(parser: BaseDanmakuParser) {
        mParser = parser
        val parserTimer = parser.getTimer()
        if (parserTimer != null) {
            timer.update(parserTimer.currMillisecond)
        }
    }

    fun setCallback(cb: Callback?) {
        mCallback = cb
    }

    // endregion

    // region 生命周期
    // EN: region Lifecycle

    /**
     * 准备弹幕数据
     *
     * 加载并解析弹幕，完成后触发 Callback.prepared() 回调。
     * 如果解析器或上下文未设置，会延迟重试。
     */
     // EN: Prepare danmaku data. Load and parse danmaku, triggers Callback.prepared() on completion.. If parser or context is not set, retries after a delay.
    fun prepare() {
        mReady = false
        mTimeBase = PlatformClock.uptimeMillis()
        doPrepare()
    }

    private fun doPrepare() {
        val parser = mParser
        val context = mContext
        if (parser == null || context == null) {
            // 延迟重试
            // EN: Deferred retry
            scope.launch {
                delay(100)
                doPrepare()
            }
            return
        }
        prepareDrawTask(parser, context) {
            pausedPosition = 0
            mReady = true
            mCallback?.prepared()
        }
    }

    private fun prepareDrawTask(
        parser: BaseDanmakuParser,
        context: DanmakuContext,
        onReady: () -> Unit
    ) {
        if (drawTask != null) {
            onReady()
            return
        }
        val disp = context.mDisplayer
        disp.resetSlopPixel(context.scaleTextSize)
        val task: IDrawTask = DrawTask(timer, context, object : IDrawTask.TaskListener {
            override fun ready() {
                onReady()
            }

            override fun onDanmakuAdd(danmaku: BaseDanmaku) {
                if (danmaku.isTimeOut()) return
                val delay = danmaku.getActualTime() - getCurrentTime()
                if (delay < context.mDanmakuFactory.MAX_DANMAKU_DURATION
                    && (mInWaitingState || mRenderingState.nothingRendered)
                ) {
                    notifyRendering()
                }
            }

            override fun onDanmakuShown(danmaku: BaseDanmaku) {
                mCallback?.danmakuShown(danmaku)
            }

            override fun onDanmakusDrawingFinished() {
                mCallback?.drawingFinished()
            }

            override fun onDanmakuConfigChanged() {
                redrawIfNeeded()
            }
        })
        task.setParser(parser)
        task.prepare()
        drawTask = task
    }

    fun isPrepared(): Boolean = mReady

    /**
     * 开始播放弹幕
     */
    // EN: Start playing
    fun start() {
        resume()
    }

    /**
     * 恢复播放
     */
    // EN: Resume
    fun resume() {
        quitFlag = false
        if (!mReady) {
            scope.launch {
                delay(100)
                resume()
            }
            return
        }
        mRenderingState.reset()
        mDrawTimes.clear()
        mTimeBase = PlatformClock.uptimeMillis() - pausedPosition
        timer.update(pausedPosition)
        drawTask?.start()
        notifyRendering()
        mInSeekingAction = false
        drawTask?.onPlayStateChanged(IDrawTask.PLAY_STATE_PLAYING)
        if (!nonBlockModeEnable) {
            startRenderLoop()
        }
    }

    /**
     * 暂停播放
     */
    // EN: Pause
    fun pause() {
        quitFlag = true
        drawTask?.onPlayStateChanged(IDrawTask.PLAY_STATE_PAUSE)
        syncTimerIfNeeded()
        pausedPosition = timer.currMillisecond
        stopRenderLoop()
    }

    /**
     * 跳转到指定时间
     *
     * @param ms 目标时间（毫秒）
     */
     // EN: Seek to specified time
    fun seekTo(ms: Long) {
        mInSeekingAction = true
        mDesireSeekingTime = ms
        stopRenderLoop()
        val deltaMs = ms - timer.currMillisecond
        mTimeBase -= deltaMs
        timer.update(ms)
        mContext?.mGlobalFlagValues?.updateMeasureFlag()
        drawTask?.seek(ms)
        pausedPosition = ms
        mInSeekingAction = false
        if (!quitFlag && !nonBlockModeEnable) {
            startRenderLoop()
        }
    }

    /**
     * 退出引擎，释放所有资源
     */
    // EN: Release all resources
    fun release() {
        quitFlag = true
        stopRenderLoop()
        syncTimerIfNeeded()
        pausedPosition = timer.currMillisecond
        drawTask?.quit()
        drawTask = null
        mParser?.release()
        mParser = null
    }

    /**
     * 是否处于停止状态
     */
    // EN: Whether stopped
    fun isStop(): Boolean = quitFlag

    // endregion

    // region 弹幕操作
    // EN: region Danmaku Operations

    /**
     * 添加单条弹幕
     */
    // EN: Add single danmaku
    fun addDanmaku(item: BaseDanmaku) {
        val task = drawTask ?: return
        val context = mContext ?: return
        item.flags = context.mGlobalFlagValues
        item.setTimer(timer)
        task.addDanmaku(item)
        notifyRendering()
    }

    /**
     * 使弹幕失效并触发重绘
     */
    // EN: Invalidate danmaku and redraw
    fun invalidateDanmaku(item: BaseDanmaku, remeasure: Boolean) {
        drawTask?.invalidateDanmaku(item, remeasure)
        redrawIfNeeded()
    }

    /**
     * 显示弹幕
     *
     * @param position 恢复播放的时间位置，null 表示从当前时间继续
     */
     // EN: Show danmaku
    fun showDanmakus(position: Long?) {
        if (isDanmakusVisible) return
        isDanmakusVisible = true
        val task = drawTask
        if (task != null) {
            if (position == null) {
                timer.update(getCurrentTime())
                task.requestClear()
            } else {
                task.start()
                task.seek(position)
                task.requestClear()
                // 恢复播放
                // EN: Resume
                quitFlag = false
                pausedPosition = position
                mTimeBase = PlatformClock.uptimeMillis() - pausedPosition
                timer.update(pausedPosition)
                mRenderingState.reset()
                mDrawTimes.clear()
                task.onPlayStateChanged(IDrawTask.PLAY_STATE_PLAYING)
                if (!nonBlockModeEnable) {
                    startRenderLoop()
                }
            }
        }
        if (quitFlag) {
            notifyRendering()
        }
    }

    /**
     * 隐藏弹幕
     *
     * @param quitDrawTask 是否同时退出绘制任务
     * @return 当前时间
     */
     // EN: Hide danmaku
    fun hideDanmakus(quitDrawTask: Boolean): Long {
        if (!isDanmakusVisible) return timer.currMillisecond
        isDanmakusVisible = false
        drawTask?.requestClear()
        drawTask?.requestHide()
        if (quitDrawTask) {
            drawTask?.quit()
            pause()
        }
        return timer.currMillisecond
    }

    /**
     * 移除所有弹幕
     */
    // EN: Remove all danmaku
    fun removeAllDanmakus(isClearDanmakusOnScreen: Boolean) {
        drawTask?.removeAllDanmakus(isClearDanmakusOnScreen)
    }

    /**
     * 移除所有直播弹幕
     */
    // EN: Remove all live danmaku
    fun removeAllLiveDanmakus() {
        drawTask?.removeAllLiveDanmakus()
    }

    /**
     * 强制重新渲染
     */
    // EN: Force re-render
    fun forceRender() {
        drawTask?.requestRender()
    }

    /**
     * 清除屏幕上的弹幕
     */
    // EN: Clear on-screen danmaku
    fun clearDanmakusOnScreen() {
        drawTask?.clearDanmakusOnScreen(getCurrentTime())
    }

    /**
     * 获取当前可见弹幕
     */
    // EN: Get visible danmaku
    fun getCurrentVisibleDanmakus(): IDanmakus? {
        return drawTask?.getVisibleDanmakusOnTime(getCurrentTime())
    }

    /**
     * 通知显示器尺寸变更
     */
    // EN: Notify displayer size change
    fun notifyDispSizeChanged(width: Int, height: Int) {
        val context = mContext ?: return
        val disp = context.mDisplayer
        if (disp.width != width || disp.height != height) {
            disp.setSize(width, height)
            context.mDanmakuFactory.notifyDispSizeChanged(context)
            context.mGlobalFlagValues.updateMeasureFlag()
            context.mGlobalFlagValues.updateVisibleFlag()
            drawTask?.requestClearRetainer()
        }
    }

    // endregion

    // region 渲染
    // EN: region Rendering

    /**
     * 绘制弹幕到画布
     *
     * 此方法应在 UI 线程的绘制回调中调用（如 Compose 的 DrawScope 或 Canvas）。
     *
     * @param canvas 画布
     * @return 渲染状态
     */
     // EN: Draw danmaku to canvas. This method should be called in the UI thread's draw callback (e.g., Compose DrawScope or Canvas).
    fun draw(canvas: IDisplayer): RenderingState? {
        val task = drawTask ?: return mRenderingState

        if (!mInWaitingState) {
            val danmakuSync = mContext?.danmakuSync
            if (danmakuSync != null) {
                val isSyncPlayingState = danmakuSync.isSyncPlayingState()
                if (!isSyncPlayingState && quitFlag) {
                    // 不同步
                    // EN: Not syncing
                } else {
                    val syncState = danmakuSync.getSyncState()
                    if (syncState == AbsDanmakuSync.SYNC_STATE_PLAYING) {
                        val fromTime = timer.currMillisecond
                        val toTime = danmakuSync.getUptimeMillis()
                        val offset = toTime - fromTime
                        if (Math.abs(offset) > danmakuSync.getThresholdTimeMills()) {
                            if (isSyncPlayingState && quitFlag) {
                                resume()
                            }
                            task.requestSync(fromTime, toTime, offset)
                            timer.update(toTime)
                            mTimeBase -= offset
                            mRemainingTime = 0
                        }
                    } else if (syncState == AbsDanmakuSync.SYNC_STATE_HALT) {
                        if (isSyncPlayingState && !quitFlag) {
                            pause()
                        }
                    }
                }
            }
        }

        val result = task.draw(canvas)
        if (result != null) {
            mRenderingState.set(result)
            recordRenderingTime()
        }
        return mRenderingState
    }

    /**
     * 同步计时器并绘制弹幕（外部渲染循环专用）
     *
     * 当播放器位置真正变化时（~1秒一次），直接对齐引擎计时器，避免 seekTo 清除弹幕。
     * 两次同步之间使用 wall clock 平滑推进。
     *
     * @param canvas 画布
     * @return 渲染状态
     */
     // EN: Sync timer and draw danmaku (for external rendering loop). When player position actually changes (~1s interval), directly align engine timer to avoid seekTo clearing danmaku.. Use wall clock for smooth progression between syncs.
    fun drawWithSync(canvas: IDisplayer): RenderingState? {
        if (!mInSeekingAction && !mInSyncAction) {
            mInSyncAction = true

            val now = PlatformClock.uptimeMillis()

            // 播放器位置变化时直接对齐计时器（不 seekTo，不清除弹幕状态）
            // EN: Timer
            val extPos = externalPlayerPosition
            if (extPos >= 0 && extPos != lastSyncedPosition) {
                val syncDelta = extPos - timer.currMillisecond
                if (kotlin.math.abs(syncDelta) > 16) {
                    timer.update(extPos)
                    mTimeBase = now - extPos
                    mRemainingTime = 0
                    mLastDeltaTime = 0
                    synchronized(mDrawTimes) { mDrawTimes.clear() }
                }
                lastSyncedPosition = extPos
                lastSyncedWallTime = now
            }

            // wall clock 平滑推进
            // EN: Wall clock smooth progression
            val time = now - mTimeBase
            var gapTime = time - timer.currMillisecond
            val averageTime = maxOf(FRAME_UPDATE_RATE, minOf(CORDON_TIME, getAverageRenderingTime()))
            val d: Long
            if (gapTime > 2000 || mRenderingState.consumingTime > CORDON_TIME) {
                d = gapTime
                gapTime = 0
                synchronized(mDrawTimes) { mDrawTimes.clear() }
            } else {
                var dd = averageTime + gapTime / FRAME_UPDATE_RATE
                dd = maxOf(FRAME_UPDATE_RATE, dd)
                dd = minOf(CORDON_TIME, dd)
                val a = dd - mLastDeltaTime
                if (a > 3 && a < 8 && mLastDeltaTime >= FRAME_UPDATE_RATE && mLastDeltaTime <= CORDON_TIME) {
                    dd = mLastDeltaTime
                }
                gapTime -= dd
                mLastDeltaTime = dd
                d = dd
            }
            mRemainingTime = gapTime
            timer.add(d)

            // Clamp: 计时器不超过播放器插值位置，防止 snap 时弹幕后退
            // EN: Timer
            // 插值 = 上次同步位置 + 距上次同步的 wall clock 间隔
            // EN: Interpolation = last sync position + wall clock interval since last sync
            if (lastSyncedPosition >= 0 && lastSyncedWallTime > 0) {
                val estimatedPlayerPos = lastSyncedPosition + (now - lastSyncedWallTime)
                if (timer.currMillisecond > estimatedPlayerPos) {
                    timer.update(estimatedPlayerPos)
                    mRemainingTime = 0
                }
            }

            mInSyncAction = false
        }
        return draw(canvas)
    }

    /**
     * 获取当前时间
     */
    // EN: Get current time
    fun getCurrentTime(): Long {
        if (!mReady) return 0
        if (mInSeekingAction) return mDesireSeekingTime
        if (quitFlag || !mInWaitingState) {
            return timer.currMillisecond - mRemainingTime
        }
        return PlatformClock.uptimeMillis() - mTimeBase
    }

    /**
     * 获取当前计时器
     */
    // EN: Get current timer
    fun getTimer(): DanmakuTimer = timer

    /**
     * 获取显示器
     */
    // EN: Get displayer
    fun getDisplayer(): IDisplayer? = mContext?.mDisplayer

    /**
     * 获取当前渲染状态
     */
    // EN: Get current rendering state
    fun getRenderingState(): RenderingState = mRenderingState

    /**
     * 获取弹幕可见性
     */
    // EN: Get visibility
    fun getVisibility(): Boolean = isDanmakusVisible

    // endregion

    // region 渲染循环
    // EN: region Rendering

    private fun startRenderLoop() {
        stopRenderLoop()
        renderJob = scope.launch {
            while (isActive && !quitFlag) {
                val startMS = PlatformClock.uptimeMillis()
                val d = syncTimer(startMS)
                if (d < 0 && !nonBlockModeEnable) {
                    delay(60 - d)
                    continue
                }
                // 通知外部绘制（由外部调用 draw()）
                // EN: Draw
                val drawTime = if (isDanmakusVisible) {
                    notifyDraw()
                } else {
                    0L
                }
                if (drawTime > CORDON_TIME2) {
                    timer.add(drawTime)
                    mDrawTimes.clear()
                }
                if (!isDanmakusVisible) {
                    waitRendering(INDEFINITE_TIME)
                } else if (mRenderingState.nothingRendered && idleSleep) {
                    val dTime = mRenderingState.endTime - timer.currMillisecond
                    if (dTime > 500) {
                        waitRendering(dTime - 10)
                    }
                }
                val elapsed = PlatformClock.uptimeMillis() - startMS
                val sleepTime = FRAME_UPDATE_RATE - elapsed
                if (sleepTime > 0) {
                    delay(sleepTime)
                }
            }
        }
    }

    private fun stopRenderLoop() {
        renderJob?.cancel()
        renderJob = null
        mInWaitingState = false
    }

    /**
     * 通知外部执行绘制
     *
     * @return 绘制耗时（毫秒）
     */
     // EN: Notify external to perform drawing
    private fun notifyDraw(): Long {
        val start = PlatformClock.uptimeMillis()
        // 实际绘制由外部调用 draw() 完成
        // EN: Actual drawing by external draw() call
        // 这里仅计算一次渲染循环的开销
        // EN: Only calculates one render loop overhead
        return PlatformClock.uptimeMillis() - start
    }

    // endregion

    // region 计时器同步
    // EN: Timer

    private fun syncTimer(startMS: Long): Long {
        if (mInSeekingAction || mInSyncAction) return 0
        mInSyncAction = true
        var d = 0L
        val time = startMS - mTimeBase
        if (nonBlockModeEnable) {
            mCallback?.updateTimer(timer)
            d = timer.lastInterval
        } else if (!isDanmakusVisible || mRenderingState.nothingRendered || mInWaitingState) {
            timer.update(time)
            mRemainingTime = 0
            mCallback?.updateTimer(timer)
        } else {
            var gapTime = time - timer.currMillisecond
            val averageTime = maxOf(FRAME_UPDATE_RATE, minOf(CORDON_TIME, getAverageRenderingTime()))
            if (gapTime > 2000 || mRenderingState.consumingTime > CORDON_TIME) {
                d = gapTime
                gapTime = 0
                synchronized(mDrawTimes) { mDrawTimes.clear() }
            } else {
                d = averageTime + gapTime / FRAME_UPDATE_RATE
                d = maxOf(FRAME_UPDATE_RATE, d)
                d = minOf(CORDON_TIME, d)
                val a = d - mLastDeltaTime
                if (a > 3 && a < 8 && mLastDeltaTime >= FRAME_UPDATE_RATE && mLastDeltaTime <= CORDON_TIME) {
                    d = mLastDeltaTime
                }
                gapTime -= d
                mLastDeltaTime = d
            }
            mRemainingTime = gapTime
            timer.add(d)
            mCallback?.updateTimer(timer)
        }
        mInSyncAction = false
        return d
    }

    private fun syncTimerIfNeeded() {
        if (mInWaitingState) {
            syncTimer(PlatformClock.uptimeMillis())
        }
    }

    // endregion

    // region 空闲等待
    // EN: region Idle Wait

    private fun notifyRendering() {
        if (!mInWaitingState) return
        drawTask?.requestClear()
        mDrawTimes.clear()
        mInWaitingState = false
    }

    private fun waitRendering(dTime: Long) {
        if (isStop() || !isPrepared() || mInSeekingAction) return
        mRenderingState.sysTime = PlatformClock.uptimeMillis()
        mInWaitingState = true
        if (dTime != INDEFINITE_TIME) {
            // 延迟唤醒
            // EN: Deferred wakeup
            scope.launch {
                delay(dTime)
                notifyRendering()
            }
        }
    }

    // endregion

    // region 渲染统计
    // EN: region Rendering

    @Synchronized
    private fun getAverageRenderingTime(): Long {
        val frames = mDrawTimes.size
        if (frames <= 0) return 0
        val first = mDrawTimes.peekFirst() ?: return 0
        val last = mDrawTimes.peekLast() ?: return 0
        return (last - first) / frames
    }

    @Synchronized
    private fun recordRenderingTime() {
        val lastTime = PlatformClock.uptimeMillis()
        mDrawTimes.addLast(lastTime)
        if (mDrawTimes.size > MAX_RECORD_SIZE) {
            mDrawTimes.removeFirst()
        }
    }

    // endregion

    // region 辅助
    // EN: region Helpers

    private fun redrawIfNeeded() {
        if (quitFlag && isDanmakusVisible) {
            drawTask?.requestClear()
            // 延迟触发重绘
            // EN: Deferred trigger redraw
            scope.launch {
                delay(100)
                if (quitFlag && isDanmakusVisible) {
                    drawTask?.requestClear()
                }
            }
        }
    }

    // endregion
}
