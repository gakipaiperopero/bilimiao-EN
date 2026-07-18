package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Build
import android.util.Rational
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.datastore.preferences.core.Preferences
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiEpisodesPage
import cn.a10miaomiao.bilimiao.compose.pages.player.SendDanmakuPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuDisplaySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.DanmakuSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.AutoStopTimerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.VideoSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoPagesPage
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.entity.player.toVideoPlayerSource
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.datastore.edit
import com.a10miaomiao.bilimiao.comm.datastore.getData
import com.a10miaomiao.bilimiao.comm.datastore.mapData
import com.a10miaomiao.bilimiao.comm.delegate.helper.StatusBarHelper
import com.a10miaomiao.bilimiao.comm.navigation.openBottomSheet
import com.a10miaomiao.bilimiao.comm.store.AppStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.player.VideoPlayerCallBack
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


class PlayerController(
    private var activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
    private val scope: CoroutineScope,
    override val di: DI,
) : DIAware, VideoPlayerCallBack, GSYVideoProgressListener {

    private val userStore by instance<UserStore>()
    private val appStore by instance<AppStore>()
    private val playerStore by instance<PlayerStore>()
    private val playListStore by instance<PlayListStore>()
    private val statusBarHelper by instance<StatusBarHelper>()
    private val scaffoldApp get() = delegate.scaffoldApp
    private val views get() = delegate.views
    private val playerSourceInfo get() = delegate.playerSourceInfo
        ?: delegate.playerSource?.defaultPlayerSource
    private val danmakuContext = DanmakuContext.create()

    private var onlyFull = false // 仅全屏播放
    // EN: Fullscreen only
    private var showSubtitle = false // 默认显示字幕
    // EN: Default show subtitles
    private var showAiSubtitle = true // 默认显示AI字幕
    // EN: Default show AI subtitles
    private var canAutoCloseFullScreen = false
    var isBackgroundPlay = true // 后台播放
    // EN: Background playback
        private set

    private var preparedRunQueue = mutableListOf<Pair<String, Runnable>>()
    private fun currentDanmakuMode(): SettingPreferences.Danmaku {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            activity.isInPictureInPictureMode
        ) {
            return SettingPreferences.DanmakuPipMode
        }
        return when (views.videoPlayer.mode) {
            DanmakuVideoPlayer.PlayerMode.SMALL_TOP -> SettingPreferences.DanmakuSmallMode
            DanmakuVideoPlayer.PlayerMode.SMALL_FLOAT -> SettingPreferences.DanmakuSmallMode
            DanmakuVideoPlayer.PlayerMode.FULL -> SettingPreferences.DanmakuFullMode
        }
    }

    private fun getFullMode(preferences: Preferences): Int {
        return preferences[SettingPreferences.PlayerFullMode]
            ?: SettingConstants.PLAYER_FULL_MODE_AUTO
    }

    fun initController() = views.videoPlayer.run {
        val that = this@PlayerController
        statusBarHelper = that.statusBarHelper
        isFullHideActionBar = true
        backButton.setOnClickListener { onBackClick() }
        setIsTouchWiget(true)
        fullscreenButton.setOnClickListener(::changeFullscreen)
        fullscreenButton.setOnLongClickListener {
            showFullModeMenu(it)
            true
        }
        danmakuContext = that.danmakuContext

        qualityView.setOnClickListener(that::showQualityPopupMenu)
        speedView.setOnClickListener(that::showSpeedPopupMenu)
        moreBtn.setOnClickListener(that::showMoreMenu)
        setDanmakuSwitchOnClickListener(that::danmakuSwitchClick)
        setExpandButtonOnClickListener(that::showPagesOrEpisodes)
        setSendDanmakuButtonOnClickListener(that::showSendDanmakuPage)
        setSendDanmakuButtonOnLongClickListener {
            danmakuSwitchClick(it)
            true
        }
        autoStopTimerView.setOnClickListener {
            activity.openBottomSheet(AutoStopTimerPage())
        }
        videoPlayerCallBack = that
        setGSYVideoProgressListener(that)
        updatePlayerMode(activity.resources.configuration)
        scope.launch {
            initPlayerSetting()
        }

        // 无障碍适配
        // EN: Accessibility adaptation
        contentDescription = "播放窗口"
        // EN: Playback window
        accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun sendAccessibilityEvent(host: View, eventType: Int) {
                super.sendAccessibilityEvent(host, eventType)
                when (eventType) {
                    AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> {
                        showController()
                    }
                }
            }
        }
    }

    fun changeFullscreen(view: View) {
        scope.launch {
            if (scaffoldApp.fullScreenPlayer) {
                smallScreen()
            } else {
                val fullMode = SettingPreferences.mapData(activity) {
                    getFullMode(it)
                }
                fullScreen(fullMode)
            }
        }
    }

    /**
     * 全屏
     */
    // EN: Fullscreen
    fun fullScreen(fullMode: Int, onlyFull: Boolean = false) {
        this.onlyFull = onlyFull
        canAutoCloseFullScreen = false
        views.videoPlayer.mode = DanmakuVideoPlayer.PlayerMode.FULL
        scaffoldApp.fullScreenPlayer = true
        activity.requestedOrientation = when (fullMode) {
            // 横向全屏(自动旋转)
            // EN: Landscape fullscreen (auto-rotate)
            SettingConstants.PLAYER_FULL_MODE_SENSOR_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            // 横向全屏(固定方向1)
            // EN: Landscape fullscreen (fixed orientation 1)
            SettingConstants.PLAYER_FULL_MODE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            // 横向全屏(固定方向2)
            // EN: Landscape fullscreen (fixed orientation 2)
            SettingConstants.PLAYER_FULL_MODE_REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            // 跟随系统：不指定方向
            // EN: Follow system: unspecified orientation
            SettingConstants.PLAYER_FULL_MODE_UNSPECIFIED -> getAppSettingScreenOrientation()
            // 跟随视频：竖向视频时为不指定方向，横向视频时候为横向全屏(自动旋转)
            // EN: Follow video: portrait = unspecified, landscape = auto-rotate fullscreen
            SettingConstants.PLAYER_FULL_MODE_AUTO -> {
                if ((playerSourceInfo?.screenProportion ?: 1f) < 1f) {
                    getAppSettingScreenOrientation()
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
            }

            else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        statusBarHelper.isShowStatus = views.videoPlayer.topContainer.visibility == View.VISIBLE
        statusBarHelper.isShowNavigation = false

        scope.launch {
            SettingPreferences.getData(activity) {
                initVideoSetting(it)
                initDanmakuContext(it)
            }
        }
    }

    /**
     * 退出全屏
     */
    // EN: Exit fullscreen
    fun smallScreen() {
        views.videoPlayer.mode = DanmakuVideoPlayer.PlayerMode.SMALL_TOP
        updatePlayerMode(activity.resources.configuration)
        scaffoldApp.fullScreenPlayer = false
        activity.requestedOrientation = getAppSettingScreenOrientation()
        statusBarHelper.isShowStatus = true
        statusBarHelper.isShowNavigation = true

        scope.launch {
            SettingPreferences.getData(activity) {
                initVideoSetting(it)
                initDanmakuContext(it)
            }
        }
    }

    private fun getAppSettingScreenOrientation(): Int {
        // 是否锁定竖屏
        // EN: Whether to lock portrait orientation
        if (appStore.state.isLockScreenOrientationPortrait) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    fun updatePlayerMode(config: Configuration) {
        if (views.videoPlayer.mode != DanmakuVideoPlayer.PlayerMode.FULL) {
            views.videoPlayer.mode = if (config.orientation == PlayerHostState.VERTICAL) {
                DanmakuVideoPlayer.PlayerMode.SMALL_TOP
            } else {
                DanmakuVideoPlayer.PlayerMode.SMALL_FLOAT
            }
        }
    }

    /**
     * 屏幕方向改变
     */
    // EN: Screen orientation changed
    fun onChangedScreenOrientation(
        orientation: Int
    ) {
        if (!scaffoldApp.showPlayer) {
            return
        }
        scope.launch {
            val openMode = SettingPreferences.mapData(activity) {
                it[PlayerOpenMode] ?: SettingConstants.PLAYER_OPEN_MODE_DEFAULT
            }
            val autoFullScreen = if (orientation == PlayerHostState.VERTICAL) {
                openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN != 0
            } else {
                openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN_LANDSCAPE != 0
            }
            if (autoFullScreen && !scaffoldApp.fullScreenPlayer) {
                // 自动切换全屏
                // EN: Auto switch to fullscreen
                fullScreen(SettingConstants.PLAYER_FULL_MODE_UNSPECIFIED)
                canAutoCloseFullScreen = true
            } else if (!autoFullScreen && canAutoCloseFullScreen && scaffoldApp.fullScreenPlayer) {
                // 自动切回小屏
                // EN: Auto switch back to small screen
                smallScreen()
            }
        }
    }

    private suspend fun initPlayerSetting() {
        SettingPreferences.getData(activity) {
            GSYVideoType.setShowType(
                it[PlayerScreenType] ?: GSYVideoType.SCREEN_TYPE_DEFAULT
            )
            if (it[DanmakuSysFont] != true) {
                danmakuContext.setTypeface(
                    Typeface.createFromAsset(
                        activity.assets,
                        "fonts/danmaku.ttf"
                    )
                )
            }
        }
        appDataStore.data.collect {
            initVideoSetting(it)
            initDanmakuContext(it)
        }
    }

    fun initDanmakuContext(
        preferences: Preferences
    ) {
        val danmakuMode = currentDanmakuMode().let {
            if (preferences[it.enable] == true) {
                it
            } else {
                SettingPreferences.DanmakuDefault
            }
        }
        val danmakuShow = (preferences[SettingPreferences.DanmakuEnable] ?: true) &&
                (preferences[danmakuMode.show] ?: true)
        views.videoPlayer.isShowDanmaku = danmakuShow

        // 滚动弹幕显示
        // EN: Scrolling danmaku display
        val danmakuR2LShow = preferences[danmakuMode.r2lShow] ?: true
        // 顶部弹幕显示
        // EN: Top danmaku display
        val danmakuFTShow = preferences[danmakuMode.ftShow] ?: true
        // 底部弹幕显示
        // EN: Bottom danmaku display
        val danmakuFBShow = preferences[danmakuMode.fbShow] ?: true
        // 高级弹幕显示
        // EN: Advanced danmaku display
        val danmakuSpecialShow = preferences[danmakuMode.specialShow] ?: true
        // 字体大小
        // EN: Font size
        var scaleTextSize = preferences[danmakuMode.fontSize] ?: 1f
        // 弹幕速度
        // EN: Danmaku speed
        val danmakuSpeed = preferences[danmakuMode.speed] ?: 1f
        // 字体不透明度
        // EN: Font opacity
        val danmakuOpacity = preferences[danmakuMode.opacity] ?: 1f

        // 滚动弹幕最大行数
        // EN: Max lines for scrolling danmaku
        val danmakuR2LMaxLine = preferences[danmakuMode.r2lMaxLine].let {
            if (it == null || it == 0) null else it
        }
        // 顶部弹幕最大行数
        // EN: Max lines for top danmaku
        val danmakuFTMaxLine = preferences[danmakuMode.ftMaxLine].let {
            if (it == null || it == 0) null else it
        }
        // 底部弹幕最大行数
        // EN: Max lines for bottom danmaku
        val danmakuFBMaxLine = preferences[danmakuMode.fbMaxLine].let {
            if (it == null || it == 0) null else it
        }
        // 设置最大显示行数
        // EN: Set max display lines
        val maxLinesPair = mapOf(
            BaseDanmaku.TYPE_SCROLL_RL to danmakuR2LMaxLine,
            BaseDanmaku.TYPE_FIX_TOP to danmakuFTMaxLine,
            BaseDanmaku.TYPE_FIX_BOTTOM to danmakuFBMaxLine,
        )

        //设置弹幕样式
        // EN: Set danmaku style
        danmakuContext?.apply {
            ftDanmakuVisibility = danmakuFTShow
            fbDanmakuVisibility = danmakuFBShow
            r2LDanmakuVisibility = danmakuR2LShow
            specialDanmakuVisibility = danmakuSpecialShow
            setScrollSpeedFactor(1 / danmakuSpeed)
            setScaleTextSize(scaleTextSize)
            setMaximumLines(maxLinesPair)
            setDanmakuTransparency(danmakuOpacity)
        }
    }

    private fun danmakuSwitchClick(view: View) {
        scope.launch {
            val danmakuMode = currentDanmakuMode()
            val isEnable = SettingPreferences.mapData(activity) {
                it[DanmakuEnable] ?: true
            }
            if (isEnable) {
                val show = !views.videoPlayer.isShowDanmaku
                views.videoPlayer.isShowDanmaku = show
                SettingPreferences.edit(activity) {
                    it[DanmakuDefault.show] = show
                    it[danmakuMode.show] = show
                }
            } else {
                GlobalToaster.showWithAction("弹幕功能已关闭，请手动打开", "打开") {
                    scope.launch {
                        SettingPreferences.edit(activity) {
                            it[DanmakuEnable] = true
                            it[DanmakuDefault.show] = true
                            it[danmakuMode.show] = true
                        }
                    }
                    views.videoPlayer.isShowDanmaku = true
                }
            }
        }
    }

    fun initVideoSetting(preferences: Preferences) {
        val show = SettingPreferences.run {
            preferences[PlayerBottomProgressBarShow] ?: 0
        }
        views.videoPlayer.showBottomProgressBarInSmallMode = (
            show and SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_SMALL != 0
        )
        views.videoPlayer.showBottomProgressBarInFullMode = (
            show and SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_FULL != 0
        )
        views.videoPlayer.showBottomProgressBarInPipMode = (
            show and SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_PIP != 0
        )
        views.videoPlayer.enabledAudioFocus = SettingPreferences.run {
            preferences[PlayerAudioFocus] ?: true
        }
        showSubtitle = preferences[SettingPreferences.PlayerSubtitleShow] ?: true
        showAiSubtitle = preferences[SettingPreferences.PlayerAiSubtitleShow] ?: false
        isBackgroundPlay = preferences[SettingPreferences.PlayerBackground] ?: true
    }

    /**
     * 播放器是否默认全屏播放
     */
    // EN: Whether player defaults to fullscreen playback
    fun checkIsPlayerDefaultFull() = scope.launch {
        val (openMode, fullMode) = SettingPreferences.mapData(activity)  {
            Pair(
                it[PlayerOpenMode] ?: SettingConstants.PLAYER_OPEN_MODE_DEFAULT,
                it[PlayerFullMode] ?: SettingConstants.PLAYER_FULL_MODE_AUTO,
            )
        }
        if (scaffoldApp.orientation == PlayerHostState.VERTICAL
            && openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN != 0) {
            fullScreen(fullMode, onlyFull = true)
        } else if (scaffoldApp.orientation == PlayerHostState.HORIZONTAL
            && openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN_LANDSCAPE != 0
        ){
            fullScreen(fullMode, onlyFull = true)
        }
    }

    fun showQualityPopupMenu(view: View) {
        val sourceInfo = delegate.playerSourceInfo ?: return
        val popup = QualityPopupMenu(
            activity = activity,
            anchor = view,
            userStore = userStore,
            list = sourceInfo.acceptList,
            value = delegate.quality
        )
        popup.setOnChangedQualityListener(delegate::changedQuality)
        popup.show()
    }

    fun showSpeedPopupMenu(view: View) {
        scope.launch {
            val speedValueSets = SettingPreferences.mapData(activity) {
                it[PlayerSpeedValues] ?: SettingConstants.PLAYER_SPEED_SETS
            }
            val popup = SpeedPopupMenu(
                activity = activity,
                anchor = view,
                value = delegate.speed,
                list = speedValueSets.map { it.toFloat() }.sorted(),
            )
            popup.setOnChangedSpeedListener(delegate::changedSpeed)
            popup.show()
        }
    }

    fun showFullModeMenu(view: View) {
        val fullModeMenuItemClick = this::fullModeMenuItemClick
        scope.launch {
            val popupMenu = PopupMenu(activity, view)
            val fullMode = SettingPreferences.mapData(activity) {
                it[PlayerFullMode] ?: SettingConstants.PLAYER_FULL_MODE_AUTO
            }
            val checkMenuId = when (fullMode) {
                SettingConstants.PLAYER_FULL_MODE_SENSOR_LANDSCAPE -> R.id.full_mode_sl
                SettingConstants.PLAYER_FULL_MODE_LANDSCAPE -> R.id.full_mode_l
                SettingConstants.PLAYER_FULL_MODE_REVERSE_LANDSCAPE -> R.id.full_mode_rl
                SettingConstants.PLAYER_FULL_MODE_UNSPECIFIED -> R.id.full_mode_u
                SettingConstants.PLAYER_FULL_MODE_AUTO -> R.id.full_mode_auto
                else -> SettingConstants.PLAYER_FULL_MODE_AUTO
            }
            popupMenu.inflate(R.menu.player_full_mode)
            popupMenu.menu.findItem(checkMenuId).isChecked = true
            popupMenu.setOnMenuItemClickListener(fullModeMenuItemClick)
            popupMenu.show()
        }
    }

    private fun fullModeMenuItemClick(item: MenuItem): Boolean {
        item.isChecked = true
        val fullMode = when (item.itemId) {
            R.id.full_mode_sl -> SettingConstants.PLAYER_FULL_MODE_SENSOR_LANDSCAPE
            R.id.full_mode_l -> SettingConstants.PLAYER_FULL_MODE_LANDSCAPE
            R.id.full_mode_rl -> SettingConstants.PLAYER_FULL_MODE_REVERSE_LANDSCAPE
            R.id.full_mode_u -> SettingConstants.PLAYER_FULL_MODE_UNSPECIFIED
            R.id.full_mode_auto -> SettingConstants.PLAYER_FULL_MODE_AUTO
            else -> SettingConstants.PLAYER_FULL_MODE_AUTO
        }
        if (scaffoldApp.fullScreenPlayer) {
            fullScreen(fullMode)
        }
        scope.launch {
            SettingPreferences.edit(activity) {
                it[PlayerFullMode] = fullMode
            }
        }
        return true
    }

    fun showMoreMenu(view: View) {
        val popupMenu = PopupMenu(activity, view)
        popupMenu.inflate(R.menu.player_top_more)
        val checkMenuId = when (GSYVideoType.getShowType()) {
            GSYVideoType.SCREEN_TYPE_DEFAULT -> R.id.scale_1
            GSYVideoType.SCREEN_TYPE_16_9 -> R.id.scale_2
            GSYVideoType.SCREEN_TYPE_4_3 -> R.id.scale_3
            GSYVideoType.SCREEN_TYPE_FULL -> R.id.scale_4
            GSYVideoType.SCREEN_MATCH_FULL -> R.id.scale_5
            else -> R.id.scale_1
        }
        popupMenu.menu.findItem(checkMenuId).isChecked = true
        popupMenu.setOnMenuItemClickListener(this::moreMenuItemClick)
        popupMenu.show()
    }

    fun showPagesOrEpisodes(view: View) {
        val playerSource = delegate.playerSource
        if (playerSource is VideoPlayerSource) {
            activity.openBottomSheet(VideoPagesPage(playerSource.aid))
        }
        if (playerSource is BangumiPlayerSource) {
            activity.openBottomSheet(BangumiEpisodesPage(
                sid = playerSource.sid,
                title = playerSource.ownerName,
            ))
        }

    }

    private fun showSendDanmakuPage(view: View) {
        if (!userStore.isLogin()) {
            GlobalToaster.show("请先登录")
            return
        }
        if (
            views.videoPlayer.mode == DanmakuVideoPlayer.PlayerMode.FULL
            && delegate.isPlaying()
        ) {
            views.videoPlayer.onVideoPause()
            views.videoPlayer.hideController()
        }
        activity.openBottomSheet(SendDanmakuPage())
    }

    fun holdUpPlayer(view: View) {
        scaffoldApp.holdUpPlayer()
    }

    private fun moreMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mini_window -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val height = playerSourceInfo?.height
                    val width = playerSourceInfo?.width
                    // 设置宽高比例值
                    // EN: Set aspect ratio value
                    var aspectRatio = if (height == null || width == null) {
                        Rational(16, 9)
                    } else {
                        Rational(width, height)
                    }
                    try {
                        delegate.picInPicHelper?.enterPictureInPictureMode(aspectRatio)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        GlobalToaster.show("此设备不支持小窗播放")
                    }
                } else {
                    GlobalToaster.show("小窗播放功能需要安卓8.0及以上版本")
                }
            }

            R.id.video_setting -> {
                activity.openBottomSheet(VideoSettingPage())
            }

            R.id.danmuku_setting -> {
                val tabName = if (scaffoldApp.fullScreenPlayer){
                    SettingPreferences.DanmakuFullMode.name
                } else {
                    SettingPreferences.DanmakuSmallMode.name
                }
                activity.openBottomSheet(DanmakuDisplaySettingPage(tabName))
            }
            R.id.scale_1,
            R.id.scale_2,
            R.id.scale_3,
            R.id.scale_4,
            R.id.scale_5 -> {
                val type = when (item.itemId) {
                    R.id.scale_1 -> GSYVideoType.SCREEN_TYPE_DEFAULT
                    R.id.scale_2 -> GSYVideoType.SCREEN_TYPE_16_9
                    R.id.scale_3 -> GSYVideoType.SCREEN_TYPE_4_3
                    R.id.scale_4 -> GSYVideoType.SCREEN_TYPE_FULL
                    R.id.scale_5 -> GSYVideoType.SCREEN_MATCH_FULL
                    else -> GSYVideoType.SCREEN_TYPE_DEFAULT
                }
                GSYVideoType.setShowType(type)
                views.videoPlayer.updateTextureViewShowType()
                scope.launch {
                    SettingPreferences.edit(activity) {
                        it[PlayerScreenType] = type
                    }
                }
            }
        }
        return true
    }

    /**
     * 获取默认字幕
     */
    // EN: Get default subtitle
    fun getDefaultSubtitle(
        list: List<DanmakuVideoPlayer.SubtitleSourceInfo>
    ): DanmakuVideoPlayer.SubtitleSourceInfo? {
        if (showSubtitle) {
            return list.find { showAiSubtitle || it.ai_status == 0 }
        }
        return null
    }

    /**
     * 创建弹幕
     * type: 1从右至左滚动弹幕|6从左至右滚动弹幕|5顶端固定弹幕|4底端固定弹幕|7高级弹幕|8脚本弹幕
     */
    // EN: Create danmaku
    // EN: type: 1 R2L scroll|6 L2R scroll|5 top fixed|4 bottom fixed|7 advanced|8 script
    fun createDanmaku(type: Int): BaseDanmaku {
        return danmakuContext.mDanmakuFactory.createDanmaku(type, danmakuContext)
    }

    fun onBackClick() {
        if (!scaffoldApp.fullScreenPlayer || onlyFull) {
            delegate.closePlayer()
        }
        smallScreen()
    }

    /**
     * 到准备完成后执行
     */
    // EN: Execute after preparation completes
    fun postPrepared(id: String, action: Runnable) {
        preparedRunQueue.add(Pair(id, action))
    }

    /**
     * 准备完成
     */
    // EN: Preparation complete
    override fun onPrepared() {
        preparedRunQueue.forEach {
            val (id, action) = it
            if (id == delegate.playerSourceId) {
                views.videoPlayer.post(action)
            }
        }
        preparedRunQueue = mutableListOf()
    }

    /**
     * 播放结束
     */
    // EN: Playback ended
    override fun onAutoCompletion() {
        delegate.historyReport(views.videoPlayer.currentPosition)
        scope.launch {
            val currentPlayerSourceInfo = delegate.playerSource ?: return@launch
            val nextPlayerSourceInfo = currentPlayerSourceInfo.next()
            val (order, orderRandom) = SettingPreferences.mapData(activity) {
                val order = it[PlayerOrder] ?: SettingConstants.PLAYER_ORDER_DEFAULT
                val orderRandom = it[PlayerOrderRandom] ?: false
                order to orderRandom
            }
            // 循环播放
            // EN: Loop playback
            val isLoop = order and SettingConstants.PLAYER_ORDER_LOOP != 0
            if (nextPlayerSourceInfo is VideoPlayerSource
                && order and SettingConstants.PLAYER_ORDER_NEXT_P != 0) {
                // 自动播放下一P
                // EN: Auto play next P
                delegate.openPlayer(nextPlayerSourceInfo)
                return@launch
            } else if (nextPlayerSourceInfo is BangumiPlayerSource
                && order and SettingConstants.PLAYER_ORDER_NEXT_EPISODE != 0) {
                // 自动播放下一集
                // EN: Auto play next episode
                delegate.openPlayer(nextPlayerSourceInfo)
                return@launch
            }
            if (order and SettingConstants.PLAYER_ORDER_NEXT_VIDEO != 0) {
                // 自动下一个视频
                // EN: Auto play next video
                val nextVideo = playerStore.nextVideo(
                    orderRandom, isLoop
                )
                if (nextVideo != null) {
                    delegate.openPlayer(nextVideo.toVideoPlayerSource())
                    return@launch
                }
            }
            if (isLoop) {
                // 单个视频循环
                // EN: Single video loop
                currentPlayerSourceInfo.isLoop = true
                delegate.openPlayer(currentPlayerSourceInfo)
            } else {
                delegate.completionBoxController.show()
            }
        }
    }

    override fun onVideoPause() {
    }

    override fun onVideoResume(isResume: Boolean) {
    }

    override fun setStateAndUi(state: Int) {
        delegate.picInPicHelper?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && it.isInPictureInPictureMode) {
                try {
                    it.updatePictureInPictureActions(state)
                } catch (e: Exception) {
                }
            }
        }
        if (state >= GSYVideoView.CURRENT_STATE_PAUSE) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onVideoClose() {
        delegate.closePlayer()
    }

    override fun onClickUiToggle(e: MotionEvent?) {
        scaffoldApp.animatePlayerHeight(scaffoldApp.smallModePlayerMaxHeight)
    }

    private var lastRecordedPosition = 0L
    private var isTimerInitialized = false

    override fun onProgress(
        progress: Long,
        secProgress: Long,
        currentPosition: Long,
        duration: Long
    ) {
        delegate.historyReport(currentPosition)

        //定时关闭 - 只在播放状态时减少计时
        // EN: Auto stop timer - only count down when playing
        val autoStopDuration = playerStore.autoStopDuration
        if (autoStopDuration > 0 && delegate.isPlaying()) {
            // 第一次调用时，初始化lastRecordedPosition
            // EN: First call, initialize lastRecordedPosition
            if (!isTimerInitialized) {
                lastRecordedPosition = currentPosition
                isTimerInitialized = true
                return
            }

            val passedTime = (currentPosition - lastRecordedPosition) / 1000
            if (passedTime in 0L..5L) {
                var remainTimeNew = autoStopDuration - passedTime.toInt()
                if (remainTimeNew <= 0) {
                    // 时间被消耗完，暂停
                    // EN: Time consumed, pause
                    remainTimeNew = 0
                    delegate.views.videoPlayer.onVideoPause()
                    isTimerInitialized = false
                }
                playerStore.setAutoStopDuration(remainTimeNew)
                // 同步倒计时到UI
                // EN: Sync countdown to UI
                delegate.views.videoPlayer.updateAutoStopTimer(remainTimeNew)
            }
            lastRecordedPosition = currentPosition
        } else if (autoStopDuration == 0) {
            // 计时器被重置，隐藏UI
            // EN: Timer reset, hide UI
            delegate.views.videoPlayer.updateAutoStopTimer(0)
            isTimerInitialized = false
        }
    }
}