package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.ui.graphics.vector.ImageVector
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPageMenu

/**
 * 菜单项数据
 * 用于在 Compose 侧构建 AppBar 菜单
 */
// EN: Menu item data
// EN: For building AppBar menu on the Compose side
data class MenuItemData(
    /** 菜单项唯一标识 */
    // EN: Unique menu item identifier
    val key: Int,
    /** 显示标题 */
    // EN: Display title
    val title: String,
    /** 副标题（可选） */
    // EN: Subtitle (optional)
    val subTitle: String? = null,
    /** 图标 */
    // EN: Icon
    val iconVector: ImageVector? = null,
    /** 子菜单 */
    // EN: Submenu
    val childMenu: List<MenuItemData>? = null,
    /** 子菜单是否为可选中菜单 */
    // EN: Whether submenu is checkable
    val checkable: Boolean = false,
    /** 子菜单当前选中的 key */
    // EN: Currently checked key in submenu
    val checkedKey: Int? = null,
    /** 内容描述（无障碍） */
    // EN: Content description (accessibility)
    val contentDescription: String? = null,
    /** 操作动作 */
    // EN: Action
    val action: String? = null,
) {
    companion object {
        /**
         * 从 MenuItemPropInfo 转换
         */
        // EN: Convert from MenuItemPropInfo
        fun fromPropInfo(propInfo: MenuItemPropInfo): MenuItemData {
            return MenuItemData(
                key = propInfo.key ?: 0,
                title = propInfo.title ?: "",
                subTitle = propInfo.subTitle,
                iconVector = propInfo.iconVector,
                childMenu = propInfo.childMenu?.items?.let { items ->
                    items.map { fromPropInfo(it) }
                },
                checkable = propInfo.childMenu?.checkable == true,
                checkedKey = propInfo.childMenu?.takeIf { it.checkable }?.checkedKey,
                contentDescription = propInfo.contentDescription,
                action = propInfo.action,
            )
        }
    }

    /**
     * 转换为 MenuItemPropInfo（用于与旧代码兼容）
     */
    // EN: Convert to MenuItemPropInfo (for compatibility with legacy code)
    fun toPropInfo(): MenuItemPropInfo {
        return MenuItemPropInfo(
            key = key,
            title = title,
            subTitle = subTitle,
            iconVector = iconVector,
            childMenu = childMenu?.toMyPageMenu(
                checkable = checkable,
                checkedKey = checkedKey,
            ),
            contentDescription = contentDescription,
            action = action,
        )
    }

    private fun List<MenuItemData>.toMyPageMenu(
        checkable: Boolean,
        checkedKey: Int?,
    ): MyPageMenu {
        return MyPageMenu().apply {
            this.checkable = checkable
            this.checkedKey = checkedKey ?: 0
            forEach { child ->
                myItem {
                    key = child.key
                    title = child.title
                    subTitle = child.subTitle
                    iconVector = child.iconVector
                    childMenu = child.childMenu?.toMyPageMenu(
                        checkable = child.checkable,
                        checkedKey = child.checkedKey,
                    )
                    contentDescription = child.contentDescription
                    action = child.action
                }
            }
        }
    }
}

/**
 * AppBar 导航图标类型
 */
// EN: AppBar navigation icon type
enum class AppBarNavigationIcon {
    /** 返回箭头 */
    // EN: Back arrow
    Back,
    /** 菜单图标 */
    // EN: Menu icon
    Menu,
}

/**
 * AppBar 布局方向
 */
// EN: AppBar layout orientation
enum class AppBarOrientation {
    /** 竖屏 - 标题在上，菜单水平排列 */
    // EN: Portrait - title on top, menus arranged horizontally
    Vertical,
    /** 横屏 - 导航在左，菜单垂直排列 */
    // EN: Landscape - navigation on left, menus arranged vertically
    Horizontal,
}
