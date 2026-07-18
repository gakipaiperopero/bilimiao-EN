package cn.a10miaomiao.bilimiao.danmaku.cache

import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.SpecialDanmaku
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuBitmap
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuCanvas
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuPaint
import cn.a10miaomiao.bilimiao.danmaku.platform.FontMetrics
import cn.a10miaomiao.bilimiao.danmaku.platform.PaintStyle

/**
 * 弹幕绘制填充器代理
 *
 * 用于在弹幕显示前自定义文本内容和释放资源。
 */
 // EN: Danmaku drawing stuffer proxy. Used to customize text content and release resources before danmaku display.
interface CacheStufferProxy {
    /**
     * 在弹幕显示前准备绘制数据
     *
     * @param danmaku 弹幕对象
     * @param fromWorkerThread 是否在工作线程（true 时可执行耗时操作）
     */
     // EN: Prepare drawing data before danmaku display
    fun prepareDrawing(danmaku: BaseDanmaku, fromWorkerThread: Boolean)

    /**
     * 释放弹幕相关资源
     */
    // EN: Release danmaku-related resources
    fun releaseResource(danmaku: BaseDanmaku)
}

/**
 * 弹幕绘制填充器基类
 *
 * 负责弹幕文本的测量和绘制。子类可覆写绘制方法实现自定义样式。
 * 通过 [CacheStufferProxy] 可在绘制前修改弹幕内容。
 */
 // EN: Base danmaku drawing stuffer. Responsible for measuring and drawing danmaku text. Subclasses can override drawing methods for custom styles.. Danmaku content can be modified before drawing via [CacheStufferProxy].
abstract class BaseCacheStuffer {

    companion object {
        /** 默认背景色（透明） */
        // EN: Default background color (transparent)
        const val DEFAULT_BACKGROUND_COLOR = 0x00000000

        /** 默认阴影色（透明，无阴影） */
        // EN: Default shadow color (transparent, no shadow)
        const val SHADOW_COLOR = 0x00000000

        /** 默认描边宽度 */
        // EN: Default stroke width
        const val DEFAULT_STROKE_WIDTH = 2f

        /** 默认下划线高度 */
        // EN: Default underline height
        const val UNDERLINE_HEIGHT = 2f

        /** 默认边框宽度 */
        // EN: Default border width
        const val BORDER_WIDTH = 2f
    }

    /** 弹幕上下文 */
    // EN: Danmaku context
    protected var mContext: DanmakuContext? = null

    /** 绘制代理 */
    // EN: Drawing proxy
    protected var mProxy: CacheStufferProxy? = null

    /**
     * 设置弹幕上下文
     */
    // EN: Set danmaku context
    fun setContext(context: DanmakuContext) {
        mContext = context
    }

    /**
     * 设置绘制代理
     */
    // EN: Set drawing proxy
    fun setProxy(proxy: CacheStufferProxy?) {
        mProxy = proxy
    }

    /**
     * 测量弹幕文本的宽高
     *
     * 测量结果会写入 danmaku.paintWidth 和 danmaku.paintHeight。
     *
     * @param danmaku 弹幕对象
     * @param paint 画笔
     * @param fromWorkerThread 是否在工作线程
     */
     // EN: Measure danmaku text width and height. Measurement results will be written to danmaku.paintWidth and danmaku.paintHeight.
    abstract fun measure(danmaku: BaseDanmaku, paint: DanmakuPaint, fromWorkerThread: Boolean)

    /**
     * 绘制弹幕文本
     *
     * @param danmaku 弹幕对象
     * @param canvas 画布
     * @param left 左边坐标
     * @param top 上边坐标
     * @param fromWorkerThread 是否在工作线程
     * @param paint 画笔
     */
     // EN: Draw danmaku text
    abstract fun drawDanmaku(
        danmaku: BaseDanmaku,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        fromWorkerThread: Boolean,
        paint: DanmakuPaint
    )

    /**
     * 绘制缓存的弹幕位图
     *
     * @param danmaku 弹幕对象
     * @param canvas 画布
     * @param left 左边坐标
     * @param top 上边坐标
     * @param paint 画笔
     * @return 是否成功绘制缓存
     */
     // EN: Draw cached danmaku bitmap
    open fun drawCache(
        danmaku: BaseDanmaku,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        paint: DanmakuPaint
    ): Boolean {
        val cache = danmaku.getDrawingCache() ?: return false
        val holder = cache.get() ?: return false
        // holder 应实现 draw 方法，由平台特定实现提供
        // EN: holder should implement draw method (platform-specific)
        return false
    }

    /**
     * 准备弹幕绘制数据
     *
     * 在弹幕显示前调用，可用于自定义文本内容。
     * 默认实现委托给 [CacheStufferProxy]。
     *
     * @param danmaku 弹幕对象
     * @param fromWorkerThread 是否在工作线程
     */
     // EN: Prepare danmaku drawing data. Called before danmaku display, can be used to customize text content.. Default implementation delegates to [CacheStufferProxy].
    open fun prepare(danmaku: BaseDanmaku, fromWorkerThread: Boolean) {
        mProxy?.prepareDrawing(danmaku, fromWorkerThread)
    }

    /**
     * 清除缓存
     */
    // EN: Clear cache
    abstract fun clearCaches()

    /**
     * 清除指定弹幕的缓存
     */
    // EN: Clear cache for specific danmaku
    open fun clearCache(danmaku: BaseDanmaku) {
        // 默认无操作，子类可覆写
        // EN: Default no-op; subclasses can override
    }

    /**
     * 释放弹幕资源
     */
    // EN: Release danmaku resources
    open fun releaseResource(danmaku: BaseDanmaku) {
        mProxy?.releaseResource(danmaku)
    }
}

/**
 * 纯文本弹幕绘制填充器
 *
 * 支持纯文本显示，处理文字描边、阴影、下划线和边框。
 * 对应原始 Android 版本的 SimpleTextCacheStuffer。
 */
 // EN: Plain text danmaku drawing stuffer. Supports plain text display, handles text stroke, shadow, underline and border.. Corresponds to the original Android version of SimpleTextCacheStuffer.
class SimpleTextCacheStuffer : BaseCacheStuffer() {

    companion object {
        /** 文本高度缓存，避免重复计算 */
        // EN: Text height cache, avoids recalculation
        private val sTextHeightCache = mutableMapOf<Float, Float>()
    }

    /**
     * 获取缓存的文本行高
     *
     * 相同字号的文本行高相同，使用缓存避免重复计算。
     */
     // EN: Get cached text line height. Text with the same font size has the same line height, use cache to avoid repeated calculation.
    protected fun getCacheHeight(danmaku: BaseDanmaku, paint: DanmakuPaint): Float {
        val textSize = paint.textSize
        val cached = sTextHeightCache[textSize]
        if (cached != null) return cached
        val fontMetrics = paint.getFontMetrics()
        val textHeight = fontMetrics.descent - fontMetrics.ascent + fontMetrics.leading
        sTextHeightCache[textSize] = textHeight
        return textHeight
    }

    override fun measure(danmaku: BaseDanmaku, paint: DanmakuPaint, fromWorkerThread: Boolean) {
        var w = 0f
        var textHeight = 0f
        if (danmaku.lines == null) {
            if (danmaku.text == null) {
                w = 0f
            } else {
                w = paint.measureText(danmaku.text.toString())
                textHeight = getCacheHeight(danmaku, paint)
            }
            danmaku.paintWidth = w
            danmaku.paintHeight = textHeight
        } else {
            textHeight = getCacheHeight(danmaku, paint)
            for (line in danmaku.lines!!) {
                if (line.isNotEmpty()) {
                    val tw = paint.measureText(line)
                    w = maxOf(tw, w)
                }
            }
            danmaku.paintWidth = w
            danmaku.paintHeight = danmaku.lines!!.size * textHeight
        }
    }

    /**
     * 绘制描边文本
     *
     * @param danmaku 弹幕对象
     * @param lineText 单行文本（null 时使用 danmaku.text）
     * @param canvas 画布
     * @param left 左边坐标
     * @param top 上边坐标（基线位置）
     * @param paint 画笔（已设置为描边样式）
     */
     // EN: Draw stroked text
    protected open fun drawStroke(
        danmaku: BaseDanmaku,
        lineText: String?,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        paint: DanmakuPaint
    ) {
        val text = lineText ?: danmaku.text?.toString() ?: return
        canvas.drawText(text, left, top, paint)
    }

    /**
     * 绘制填充文本
     *
     * @param danmaku 弹幕对象
     * @param lineText 单行文本（null 时使用 danmaku.text）
     * @param canvas 画布
     * @param left 左边坐标
     * @param top 上边坐标（基线位置）
     * @param paint 画笔（已设置为填充样式）
     * @param fromWorkerThread 是否在工作线程
     */
     // EN: Draw filled text
    protected open fun drawText(
        danmaku: BaseDanmaku,
        lineText: String?,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        paint: DanmakuPaint,
        fromWorkerThread: Boolean
    ) {
        // 特殊弹幕在工作线程绘制时设置完全不透明
        // EN: Set special danmaku fully opaque when drawing in worker thread
        if (fromWorkerThread && danmaku is SpecialDanmaku) {
            paint.alpha = 255
        }
        val text = lineText ?: danmaku.text?.toString() ?: return
        canvas.drawText(text, left, top, paint)
    }

    /**
     * 绘制弹幕背景
     *
     * 默认无背景，子类可覆写添加背景绘制。
     */
     // EN: Draw danmaku background. No background by default, subclasses can override to add background drawing.
    protected open fun drawBackground(
        danmaku: BaseDanmaku,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float
    ) {
        // 默认无背景
        // EN: No background by default
    }

    override fun drawDanmaku(
        danmaku: BaseDanmaku,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        fromWorkerThread: Boolean,
        paint: DanmakuPaint
    ) {
        var _left = left
        var _top = top
        var textLeft = left + danmaku.padding
        var textTop = top + danmaku.padding

        // 边框偏移
        // EN: Border offset
        if (danmaku.borderColor != 0) {
            textLeft += BORDER_WIDTH
            textTop += BORDER_WIDTH
        }

        // 配置画笔参数
        // EN: Configure paint parameters
        val hasShadow = danmaku.textShadowColor != 0
        val hasBorder = danmaku.borderColor != 0
        val hasUnderline = danmaku.underlineColor != 0

        drawBackground(danmaku, canvas, _left, _top)

        val fontMetrics = paint.getFontMetrics()
        val ascentOffset = -fontMetrics.ascent

        if (danmaku.lines != null) {
            val lines = danmaku.lines!!
            if (lines.size == 1) {
                // 单行文本
                // EN: Single line text
                drawDanmakuLine(
                    danmaku, lines[0], canvas, textLeft, textTop + ascentOffset,
                    paint, fromWorkerThread, hasShadow
                )
            } else {
                // 多行文本
                // EN: Multi-line text
                val textHeight = (danmaku.paintHeight - 2 * danmaku.padding) / lines.size
                for (t in lines.indices) {
                    if (lines[t].isEmpty()) continue
                    drawDanmakuLine(
                        danmaku, lines[t], canvas,
                        textLeft, t * textHeight + textTop + ascentOffset,
                        paint, fromWorkerThread, hasShadow
                    )
                }
            }
        } else {
            // 无多行拆分的文本
            // EN: Text without multi-line splitting
            drawDanmakuLine(
                danmaku, null, canvas, textLeft, textTop + ascentOffset,
                paint, fromWorkerThread, hasShadow
            )
        }

        // 绘制下划线
        // EN: Draw underline
        if (hasUnderline) {
            paint.style = PaintStyle.FILL
            paint.color = danmaku.underlineColor
            paint.strokeWidth = UNDERLINE_HEIGHT
            val bottom = _top + danmaku.paintHeight - UNDERLINE_HEIGHT
            canvas.drawLine(_left, bottom, _left + danmaku.paintWidth, bottom, paint)
        }

        // 绘制边框
        // EN: Draw border
        if (hasBorder) {
            paint.style = PaintStyle.STROKE
            paint.color = danmaku.borderColor
            paint.strokeWidth = BORDER_WIDTH
            canvas.drawRect(
                _left, _top,
                _left + danmaku.paintWidth, _top + danmaku.paintHeight,
                paint
            )
        }
    }

    /**
     * 绘制单行弹幕文本（描边 + 填充）
     */
    // EN: Draw single-line danmaku text (stroke + fill)
    private fun drawDanmakuLine(
        danmaku: BaseDanmaku,
        lineText: String?,
        canvas: DanmakuCanvas,
        left: Float,
        top: Float,
        paint: DanmakuPaint,
        fromWorkerThread: Boolean,
        hasShadow: Boolean
    ) {
        if (hasShadow) {
            // 绘制描边/阴影层
            // EN: Draw stroke/shadow layer
            paint.style = PaintStyle.STROKE
            paint.color = danmaku.textShadowColor
            paint.strokeWidth = DEFAULT_STROKE_WIDTH
            drawStroke(danmaku, lineText, canvas, left, top, paint)
        }

        // 绘制填充层
        // EN: Draw fill layer
        paint.style = PaintStyle.FILL
        paint.color = danmaku.textColor
        drawText(danmaku, lineText, canvas, left, top, paint, fromWorkerThread)
    }

    override fun clearCaches() {
        sTextHeightCache.clear()
    }
}
