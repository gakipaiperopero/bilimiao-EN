package cn.a10miaomiao.bilimiao.danmaku.platform

/**
 * 弹幕位图抽象，对应 Android Bitmap
 */
// EN: Danmaku bitmap abstraction (Android Bitmap equivalent)
interface DanmakuBitmap {
    val width: Int
    val height: Int

    fun eraseColor(color: Int)
    fun recycle()
    fun isRecycled(): Boolean
}

enum class BitmapConfig {
    ARGB_8888,
    ARGB_4444,
    RGB_565
}

/**
 * 位图工厂
 */
// EN: Bitmap factory
interface DanmakuBitmapFactory {
    fun create(width: Int, height: Int, config: BitmapConfig = BitmapConfig.ARGB_8888): DanmakuBitmap
}
