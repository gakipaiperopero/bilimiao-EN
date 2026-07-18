package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * AppBar 尺寸配置
 * 对应原有 ViewConfig 中的 appBar 相关常量
 */
// EN: AppBar size configuration
// EN: Corresponds to appBar related constants in original ViewConfig
object AppBarConfig {
    /** AppBar总高度 */
    // EN: AppBar total height
    val Height: Dp = 70.dp

    /** 标题区域高度 */
    // EN: Title area height
    val TitleHeight: Dp = 20.dp

    /** 菜单区域高度 */
    // EN: Menu area height
    val MenuHeight: Dp = 50.dp

    /** 横屏时菜单宽度 */
    // EN: Menu width in landscape
    val MenuWidth: Dp = 120.dp

    /** 菜单项最小宽度 */
    // EN: Minimum menu item width
    val MenuItemMinWidth: Dp = 60.dp

    /** 菜单项图标大小 */
    // EN: Menu item icon size
    val MenuItemIconSize: Dp = 20.dp

    /** 菜单项图标水平间距 */
    // EN: Menu item icon horizontal margin
    val MenuItemIconMargin: Dp = 2.dp

    /** 标题文字大小 */
    // EN: Title text size
    val TitleTextSize: Dp = 12.dp

    /** 副标题文字大小 */
    // EN: Subtitle text size
    val SubTitleTextSize: Dp = 10.dp

    /** 副标题上边距 */
    // EN: Subtitle top margin
    val SubTitleMarginTop: Dp = 2.dp

    /** 导航内边距 */
    // EN: Navigation padding
    val NavigationPadding: Dp = 10.dp

    /** 导航图标大小 */
    // EN: Navigation icon size
    val NavigationIconSize: Dp = 24.dp

    /** 分割线高度 */
    // EN: Divider height
    val DividerHeight: Dp = 1.dp
}
