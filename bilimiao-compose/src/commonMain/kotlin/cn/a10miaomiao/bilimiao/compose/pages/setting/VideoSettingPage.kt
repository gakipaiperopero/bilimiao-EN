package cn.a10miaomiao.bilimiao.compose.pages.setting

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import cn.a10miaomiao.bilimiao.compose.components.preference.customSetsPreference
import cn.a10miaomiao.bilimiao.compose.components.preference.multiSelectIntPreference
import cn.a10miaomiao.bilimiao.compose.components.preference.sliderIntPreference
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import kotlinx.serialization.Serializable
import cn.a10miaomiao.bilimiao.compose.common.preference.ProvidePreferenceLocals
import cn.a10miaomiao.bilimiao.compose.common.preference.listPreference
import cn.a10miaomiao.bilimiao.compose.common.preference.preference
import cn.a10miaomiao.bilimiao.compose.common.preference.preferenceCategory
import cn.a10miaomiao.bilimiao.compose.common.preference.switchPreference
import org.kodein.di.compose.rememberInstance
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

@Serializable
class VideoSettingPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: VideoSettingPageViewModel = diViewModel { VideoSettingPageViewModel(it) }
        VideoSettingPageContent(viewModel)
    }
}

private class VideoSettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()

    private val fnvalSelection = mapOf(
        SettingConstants.PLAYER_FNVAL_DASH to AnnotatedString("dash (supports 4K)"),
        SettingConstants.PLAYER_FNVAL_MP4 to AnnotatedString("mp4 (no 2K+)"),
    )

    fun fnvalSelectionName(value: Int) = fnvalSelection[value] ?: AnnotatedString(value.toString())
    val fnvalSelectionList = fnvalSelection.keys.toList()


    private val fullModeSelection = mapOf(
        SettingConstants.PLAYER_FULL_MODE_AUTO to AnnotatedString("Follow video"),
        SettingConstants.PLAYER_FULL_MODE_UNSPECIFIED to AnnotatedString("Follow system"),
        SettingConstants.PLAYER_FULL_MODE_SENSOR_LANDSCAPE to AnnotatedString("Landscape (auto)"),
        SettingConstants.PLAYER_FULL_MODE_LANDSCAPE to AnnotatedString("Landscape (fixed 1)"),
        SettingConstants.PLAYER_FULL_MODE_REVERSE_LANDSCAPE to AnnotatedString("Landscape (fixed 2)"),
    )

    fun fullModeSelectionName(value: Int) =
        fullModeSelection[value] ?: AnnotatedString(value.toString())

    val fullModeSelectionList = fullModeSelection.keys.toList()


    private val openModeSelection = mapOf(
        SettingConstants.PLAYER_OPEN_MODE_AUTO_PLAY to AnnotatedString("Auto-play when no video is playing"),
        SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE to AnnotatedString("Auto-replace when playing"),
        SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE_PAUSE to AnnotatedString("Auto-replace when paused"),
        SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE_COMPLETE to AnnotatedString("Auto-replace when completed"),
        SettingConstants.PLAYER_OPEN_MODE_AUTO_CLOSE to AnnotatedString("Auto-close when exiting detail page"),
        SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN to AnnotatedString("Auto-fullscreen in portrait mode"),
        SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN_LANDSCAPE to AnnotatedString("Auto-fullscreen in landscape mode"),
    )

    fun openModeSelectionName(value: Int) =
        openModeSelection[value] ?: AnnotatedString(value.toString())

    val openModeSelectionList = openModeSelection.keys.toList()

    private val orderSelection = mapOf(
        SettingConstants.PLAYER_ORDER_LOOP to AnnotatedString("Loop (list loop if options below are checked, single loop if not)"),
        SettingConstants.PLAYER_ORDER_NEXT_P to AnnotatedString("Auto next part"),
        SettingConstants.PLAYER_ORDER_NEXT_VIDEO to AnnotatedString("Auto next video"),
        SettingConstants.PLAYER_ORDER_NEXT_EPISODE to AnnotatedString("Auto next episode (anime)"),
    )

    fun orderSelectionName(value: Int) = orderSelection[value] ?: AnnotatedString(value.toString())
    val orderSelectionList = orderSelection.keys.toList()

    private val bottomProgressBarShowSelection = mapOf(
        SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_SMALL
                to AnnotatedString("Show bottom progress bar in small window"),
        SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_FULL
                to AnnotatedString("Show bottom progress bar in fullscreen"),
        SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_PIP
                to AnnotatedString("Show bottom progress bar in PiP mode"),
    )

    fun bottomProgressBarShowName(value: Int) = bottomProgressBarShowSelection[value]
        ?: AnnotatedString(value.toString())

    val bottomProgressBarShowSelectionList = bottomProgressBarShowSelection.keys.toList()

    fun proxyClick() {
        pageNavigation.navigate(ProxySettingPage())
    }

    fun autoStopTimerClick() {
        pageNavigation.navigate(AutoStopTimerPage())
    }

}


@Composable
private fun VideoSettingPageContent(
    viewModel: VideoSettingPageViewModel
) {
    PageConfig(
        title = "Playback settings"
    )
    val windowInsets = localContentInsets()
    val playerStore: PlayerStore by rememberInstance()

    val dataStore = remember {
        appDataStore
    }

    ProvidePreferenceLocals(
        flow = rememberPreferenceFlow(dataStore)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                )
        ) {
            item("top") {
                Spacer(
                    modifier = Modifier.height(windowInsets.topDp.dp)
                )
            }
            preferenceCategory(
                key = "player",
                title = {
                    Text("Player settings")
                }
            )
            switchPreference(
                key = SettingPreferences.PlayerBackground.name,
                title = {
                    Text("Background playback")
                },
                summary = {
                    Text("遇到困难时，不要停下来.")
                },
                defaultValue = true,
            )
            switchPreference(
                key = SettingPreferences.PlayerAudioFocus.name,
                title = {
                    Text("Request audio focus")
                },
                summary = {
                    Text("When disabled, can play alongside other apps")
                },
                defaultValue = true,
            )

            preferenceCategory(
                key = "source",
                title = {
                    Text("Video source settings")
                }
            )
            listPreference(
                key = SettingPreferences.PlayerFnval.name,
                title = {
                    Text("Video format selection")
                },
                summary = {
                    Text("Try a different format if playback fails")
                },
                defaultValue = SettingConstants.PLAYER_FNVAL_DASH,
                values = viewModel.fnvalSelectionList,
                valueToText = viewModel::fnvalSelectionName
            )
            preference(
                key = SettingPreferences.PlayerProxy.name,
                title = {
                    Text("Region restriction settings")
                },
                summary = {
                    Text("Drip, business trip card")
                },
                onClick = viewModel::proxyClick
            )

            preferenceCategory(
                key = "control",
                title = {
                    Text("Playback control settings")
                }
            )
            switchPreference(
                key = SettingPreferences.PlayerNotification.name,
                title = {
                    Text("Show notification player controller")
                },
                summary = {
                    if (it) {
                        Text(text = "Only shows when playing")
                    } else {
                        Text(text = "这个家里已经没有你的位置啦！")
                    }
                },
                defaultValue = true,
            )
            multiSelectIntPreference(
                key = SettingPreferences.PlayerOpenMode.name,
                title = {
                    Text("Auto player controls")
                },
                summary = {
                    Text("Actions when opening/closing video details")
                },
                values = viewModel.openModeSelectionList,
                defaultValue = SettingConstants.PLAYER_OPEN_MODE_DEFAULT,
                valueToText = viewModel::openModeSelectionName,
            )
            multiSelectIntPreference(
                key = SettingPreferences.PlayerOrder.name,
                title = {
                    Text("Playback order")
                },
                summary = {
                    Text("Multiple options can be combined")
                },
                defaultValue = SettingConstants.PLAYER_ORDER_DEFAULT,
                values = viewModel.orderSelectionList,
                valueToText = viewModel::orderSelectionName
            )
            switchPreference(
                key = SettingPreferences.PlayerOrderRandom.name,
                title = {
                    Text("Shuffle")
                },
                summary = {
                    Text("Play random next video, not effective when single video is looping")
                },
                defaultValue = false,
            )
            listPreference(
                key = SettingPreferences.PlayerFullMode.name,
                title = {
                    Text("Fullscreen orientation")
                },
                summary = {
                    Text("Long-press the fullscreen button in player to access this option")
                },
                defaultValue = SettingConstants.PLAYER_FULL_MODE_AUTO,
                values = viewModel.fullModeSelectionList,
                valueToText = viewModel::fullModeSelectionName
            )
            multiSelectIntPreference(
                key = SettingPreferences.PlayerBottomProgressBarShow.name,
                title = {
                    Text("Bottom progress bar control")
                },
                defaultValue = 0,
                values = viewModel.bottomProgressBarShowSelectionList,
                valueToText = viewModel::bottomProgressBarShowName
            )
            customSetsPreference(
                key = SettingPreferences.PlayerSpeedValues.name,
                title = {
                    Text("Custom speed menu")
                },
                defaultValue = SettingConstants.PLAYER_SPEED_SETS,
                valueText = {
                    Text(
                        text = it + "x speed",
                        modifier = Modifier.widthIn(min = 48.dp),
                        textAlign = TextAlign.Center,
                    )
                },
                valueCanEdit = {
                    it !in SettingConstants.PLAYER_SPEED_SETS
                },
                canAdd = {
                    it.size < 10
                }
            )
            preference(
                key = "auto_stop_duration",
                title = {
                    Text("Player auto-stop timer")
                },
                summary = {
                    Text("Based on video playback duration, not real time")
                },
                onClick = viewModel::autoStopTimerClick
            )

            preferenceCategory(
                key = "small",
                title = {
                    Text(text = "Landscape small window settings")
                }
            )
            switchPreference(
                key = SettingPreferences.PlayerSmallDraggable.name,
                title = {
                    Text(text = "Draggable player in small window")
                },
                summary = {
                    if (it) {
                        Text(text = "Enabled, can drag the small window player")
                    } else {
                        Text(text = "When enabled, gestures in small window mode are disabled")
                    }
                },
                defaultValue = false,
            )
            sliderIntPreference(
                key = SettingPreferences.PlayerSmallShowArea.name,
                title = {
                    Text(text = "Small window playback area")
                },
                valueRange = 150..600,
                defaultValue = 480,
                valueText = {
                    Text(text = it.toString())
                }
            )
            sliderIntPreference(
                key = SettingPreferences.PlayerHoldShowArea.name,
                title = {
                    Text(text = "Small window minimum area")
                },
                valueRange = 100..300,
                defaultValue = 130,
                valueText = {
                    Text(text = it.toString())
                }
            )

            preferenceCategory(
                key = "subtitle",
                title = {
                    Text("Subtitle settings")
                }
            )
            switchPreference(
                key = SettingPreferences.PlayerSubtitleShow.name,
                title = {
                    Text("Show subtitles")
                },
                summary = {
                    if (it) {
                        Text("Subtitles enabled")
                    } else {
                        Text("Subtitles disabled")
                    }
                },
                defaultValue = true,
            )
            switchPreference(
                key = SettingPreferences.PlayerAiSubtitleShow.name,
                title = {
                    Text("AI subtitles")
                },
                summary = {
                    Text("These are AI subtitles generated by the uploader, not available on every video")
                },
                defaultValue = false,
            )

            item("bottom") {
                Spacer(
                    modifier = Modifier.height(
                        windowInsets.bottom
                    )
                )
            }
        }
    }
}