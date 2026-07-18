package com.a10miaomiao.bilimiao.comm.datastore

object SettingConstants {

    const val HOME_ENTRY_VIEW_DEFAULT = 0 // Time姬 (default home)
    const val HOME_ENTRY_VIEW_RECOMMEND = 1 // Recommended
    const val HOME_ENTRY_VIEW_POPULAR = 2 // Popular

    const val THEME_TYPE_DEFAULT = 0
    const val THEME_TYPE_DYNAMIC_COLOR = 1

    const val PLAYER_DECODER_DEFAULT = "default"
    const val PLAYER_DECODER_AV1 = "AV1"

    const val PLAYER_FNVAL_FLV = 2
    const val PLAYER_FNVAL_MP4 = 2
    const val PLAYER_FNVAL_DASH = 4048

    // 0000 0000: Do nothing
    const val PLAYER_OPEN_MODE_DEFAULT = 0
    // 0000 0001: Auto-play when nothing is playing
    const val PLAYER_OPEN_MODE_AUTO_PLAY = 1
    // 0000 0010: Auto-replace currently playing video
    const val PLAYER_OPEN_MODE_AUTO_REPLACE = 2
    // 0000 0100: Auto-replace paused video
    const val PLAYER_OPEN_MODE_AUTO_REPLACE_PAUSE = 4
    // 0000 1000: Auto-replace completed video
    const val PLAYER_OPEN_MODE_AUTO_REPLACE_COMPLETE = 8
    // 0001 0000: Auto-close
    const val PLAYER_OPEN_MODE_AUTO_CLOSE = 16
    // 0010 0000: Auto-fullscreen in portrait mode
    const val PLAYER_OPEN_MODE_AUTO_FULL_SCREEN = 32
    // 0100 0000: Auto-fullscreen in landscape mode
    const val PLAYER_OPEN_MODE_AUTO_FULL_SCREEN_LANDSCAPE = 64

    // 0000: End after playback
    const val PLAYER_ORDER_END = 0
    // 0001: Loop after playback
    const val PLAYER_ORDER_LOOP = 1
    // 0010: Auto next part
    const val PLAYER_ORDER_NEXT_P = 2
    // 0100: Auto next video
    const val PLAYER_ORDER_NEXT_VIDEO = 4
    // 1000: Auto next episode (anime)
    const val PLAYER_ORDER_NEXT_EPISODE = 8
    // Default: Auto next part + auto next video + auto next episode (anime)
    const val PLAYER_ORDER_DEFAULT = PLAYER_ORDER_NEXT_P or PLAYER_ORDER_NEXT_VIDEO or PLAYER_ORDER_NEXT_EPISODE

    // Follow video orientation
    const val PLAYER_FULL_MODE_AUTO = 0
    // Follow system
    const val PLAYER_FULL_MODE_UNSPECIFIED = 8
    // Landscape fullscreen (auto-rotate)
    const val PLAYER_FULL_MODE_SENSOR_LANDSCAPE = 3
    // Landscape fullscreen (fixed orientation 1)
    const val PLAYER_FULL_MODE_LANDSCAPE = 1
    // Landscape fullscreen (fixed orientation 2)
    const val PLAYER_FULL_MODE_REVERSE_LANDSCAPE = 2

    // Show bottom progress bar in small mode
    const val  PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_SMALL = 1
    // Show bottom progress bar in full mode
    const val  PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_FULL = 2
    // Show bottom progress bar in PiP mode
    const val  PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_PIP = 4

    // Speed value set
    val PLAYER_SPEED_SETS = setOf("0.5", "1.0", "2.0")

    // Player auto-stop duration default (seconds), 0 means disabled
    const val PLAYER_AUTO_STOP_DURATION_DEFAULT = 0

}