package cn.a10miaomiao.bilimiao.danmaku.parser

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.DanmakuTimer
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer

/**
 * 弹幕解析器基类
 *
 * 提供链式调用的加载、配置、解析流程。子类需实现 [parse] 方法完成实际解析逻辑。
 */
 // EN: Base danmaku parser. Chain-call loading, config, parsing. Subclasses implement [parse] for actual logic.
abstract class BaseDanmakuParser {

    /**
     * 解析器监听器
     */
    // EN: Parser listener
    interface Listener {
        /**
         * 弹幕添加回调
         */
        // EN: Danmaku add callback
        fun onDanmakuAdd(danmaku: BaseDanmaku)

        /**
         * 弹幕数据变更回调
         */
        // EN: Danmaku data change callback
        fun onDanmakuDataChanged() {}

        /**
         * 弹幕解析完成回调
         */
        // EN: Danmaku parse complete callback
        fun onDanmakusParsed() {}
    }

    /** 数据源 */
    // EN: Data source
    protected var mDataSource: IDataSource<*>? = null

    /** 计时器 */
    // EN: Timer
    protected var mTimer: DanmakuTimer? = null

    /** 显示器宽度 */
    // EN: Displayer width
    protected var mDispWidth: Int = 0

    /** 显示器高度 */
    // EN: Displayer height
    protected var mDispHeight: Int = 0

    /** 显示器密度 */
    // EN: Displayer density
    protected var mDispDensity: Float = 0f

    /** 缩放密度 */
    // EN: Scaled density
    protected var mScaledDensity: Float = 0f

    /** 已解析的弹幕集合 */
    // EN: Parsed danmaku collection
    private var mDanmakus: IDanmakus? = null

    /** 显示器 */
    // EN: Displayer
    protected var mDisp: IDisplayer? = null

    /** 弹幕上下文 */
    // EN: Danmaku context
    protected var mContext: DanmakuContext? = null

    /** 监听器 */
    // EN: Listener
    protected var mListener: Listener? = null

    /**
     * 设置显示器，同时更新视口状态
     */
    // EN: Set displayer and update viewport state
    fun setDisplayer(disp: IDisplayer): BaseDanmakuParser {
        mDisp = disp
        mDispWidth = disp.width
        mDispHeight = disp.height
        mDispDensity = disp.density
        mScaledDensity = disp.scaledDensity
        mContext?.let { ctx ->
            ctx.mDanmakuFactory.updateViewportState(
                mDispWidth.toFloat(), mDispHeight.toFloat(), getViewportSizeFactor()
            )
            ctx.mDanmakuFactory.updateMaxDanmakuDuration()
        }
        return this
    }

    /**
     * 获取显示器
     */
    // EN: Get displayer
    fun getDisplayer(): IDisplayer? = mDisp

    /**
     * 设置监听器
     */
    // EN: Set listener
    fun setListener(listener: Listener): BaseDanmakuParser {
        mListener = listener
        return this
    }

    /**
     * 计算视口缩放因子，影响滚动弹幕的速度
     */
    // EN: Calculate viewport scale factor (affects scrolling speed)
    protected fun getViewportSizeFactor(): Float {
        return 1f / (mDispDensity - 0.6f)
    }

    /**
     * 加载数据源
     */
    // EN: Load data source
    fun load(source: IDataSource<*>): BaseDanmakuParser {
        mDataSource = source
        return this
    }

    /**
     * 设置计时器
     */
    // EN: Set timer
    fun setTimer(timer: DanmakuTimer): BaseDanmakuParser {
        mTimer = timer
        return this
    }

    /**
     * 获取计时器
     */
    // EN: Get timer
    fun getTimer(): DanmakuTimer? = mTimer

    /**
     * 获取弹幕集合
     *
     * 首次调用时执行解析，之后返回缓存结果。
     * 解析完成后会释放数据源并更新工厂的最大弹幕时长。
     */
     // EN: Get collection. Parses on first call, returns cached result.. Releases source after parse, updates factory max duration.
    fun getDanmakus(): IDanmakus? {
        if (mDanmakus != null) return mDanmakus
        mContext?.mDanmakuFactory?.resetDurationsData()
        mDanmakus = parse()
        releaseDataSource()
        mContext?.mDanmakuFactory?.updateMaxDanmakuDuration()
        mListener?.onDanmakusParsed()
        return mDanmakus
    }

    /**
     * 释放数据源
     */
    // EN: Release data source
    protected fun releaseDataSource() {
        mDataSource?.release()
        mDataSource = null
    }

    /**
     * 执行解析，由子类实现
     *
     * @return 解析后的弹幕集合
     */
     // EN: Parse, implemented by subclass
    protected abstract fun parse(): IDanmakus?

    /**
     * 释放资源
     */
    // EN: Release resources
    fun release() {
        releaseDataSource()
    }

    /**
     * 设置弹幕上下文配置
     */
    // EN: Set danmaku context config
    fun setConfig(config: DanmakuContext): BaseDanmakuParser {
        mContext = config
        return this
    }
}
