package cn.a10miaomiao.bilimiao.danmaku.model

/**
 * 底部固定弹幕
 */
// EN: Bottom fixed danmaku
class FBDanmaku(duration: Duration) : FTDanmaku(duration) {
    override fun getType(): Int = TYPE_FIX_BOTTOM
}
