package cn.a10miaomiao.bilimiao.compose.pages.setting

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import cn.a10miaomiao.bilimiao.compose.common.preference.ProvidePreferenceLocals
import cn.a10miaomiao.bilimiao.compose.common.preference.preference
import cn.a10miaomiao.bilimiao.compose.common.preference.preferenceCategory
import cn.a10miaomiao.bilimiao.compose.common.preference.switchPreference
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

@Serializable
class DanmakuSettingPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: DanmakuSettingPageViewModel = diViewModel { DanmakuSettingPageViewModel(it) }
        DanmakuSettingPageContent(viewModel)
    }
}

private class DanmakuSettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()


    private fun toDisplaySettingPage(
        name: String,
    ) {
        pageNavigation.navigate(DanmakuDisplaySettingPage(
            name = name
        ))
    }

    fun defaultDisplayClick() {
        toDisplaySettingPage(
            SettingPreferences.DanmakuDefault.name
        )
    }
    fun smallModeDisplayClick() {
        toDisplaySettingPage(
            SettingPreferences.DanmakuSmallMode.name
        )
    }
    fun fullModeDisplayClick() {
        toDisplaySettingPage(
            SettingPreferences.DanmakuFullMode.name
        )
    }
    fun pipModeDisplayClick() {
        toDisplaySettingPage(
            SettingPreferences.DanmakuPipMode.name
        )
    }
}


@Composable
private fun DanmakuSettingPageContent(
    viewModel: DanmakuSettingPageViewModel
) {
    PageConfig(
        title = "Danmaku settings"
    )
    val windowInsets = localContentInsets()

    val dataStore = remember {
        appDataStore
    }
    val danmakuEnableArr by dataStore.data.map {
        arrayOf(
            it[SettingPreferences.DanmakuEnable] ?: true,
            it[SettingPreferences.DanmakuSmallMode.enable] ?: false,
            it[SettingPreferences.DanmakuFullMode.enable] ?: false,
            it[SettingPreferences.DanmakuPipMode.enable] ?: false,
        )
    }.collectAsState(initial = arrayOf(true, false, false, false))
    val danmakuEnable = danmakuEnableArr[0]

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
                key = "0",
                title = {
                    Text("Basic settings")
                }
            )
            switchPreference(
                key = SettingPreferences.DanmakuEnable.name,
                title = {
                    Text("Enable danmaku")
                },
                summary = {
                    if (it) {
                        Text("Enabled")
                    } else {
                        Text("Disabled, enable to configure other settings")
                    }
                },
                defaultValue = true,
            )
            switchPreference(
                key = SettingPreferences.DanmakuSysFont.name,
                enabled = {
                    danmakuEnable
                },
                title = {
                    Text("Use system font for danmaku")
                },
                summary = {
                    Text("Restart the app to take effect")
                },
                defaultValue = true,
            )

            preferenceCategory(
                key = "1",
                title = {
                    Text("Display settings")
                }
            )
            preference(
                key = "Default",
                enabled = danmakuEnable,
                title = {
                    Text("Default display settings")
                },
                summary = {
                    Text("Uses default settings when not independently configured")
                },
                onClick = viewModel::defaultDisplayClick
            )
            preference(
                key = "Small window",
                enabled = danmakuEnable,
                title = {
                    Text("Small window display settings")
                },
                summary = {
                      if (danmakuEnableArr[1]) {
                          Text("Independent settings enabled")
                      } else {
                          Text("Independent settings disabled, using defaults")
                      }
                },
                onClick = viewModel::smallModeDisplayClick
            )
            preference(
                key = "Fullscreen",
                enabled = danmakuEnable,
                title = {
                    Text("Fullscreen display settings")
                },
                summary = {
                    if (danmakuEnableArr[2]) {
                          Text("Independent settings enabled")
                      } else {
                          Text("Independent settings disabled, using defaults")
                      }
                },
                onClick = viewModel::fullModeDisplayClick
            )
            preference(
                key = "PiP",
                enabled = danmakuEnable,
                title = {
                    Text("PiP display settings")
                },
                summary = {
                    if (danmakuEnableArr[3]) {
                          Text("Independent settings enabled")
                      } else {
                          Text("Independent settings disabled, using defaults")
                      }
                },
                onClick = viewModel::pipModeDisplayClick
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
        