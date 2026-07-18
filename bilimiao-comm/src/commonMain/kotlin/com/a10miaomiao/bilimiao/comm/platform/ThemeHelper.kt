package com.a10miaomiao.bilimiao.comm.platform

/**
 * 设置深色模式
 * 0: 跟随系统, 1: 浅色模式, 2: 深色模式
 */
// EN: Set dark mode. 0: follow system, 1: light mode, 2: dark mode
expect fun setDarkMode(mode: Int)

/**
 * 获取 Material You 动态主题颜色
 * 仅 Android 12+ 支持，其他平台返回默认颜色
 */
// EN: Get Material You dynamic theme color. Only Android 12+ supported, other platforms return default color
expect fun getMaterialYouColor(): Int
