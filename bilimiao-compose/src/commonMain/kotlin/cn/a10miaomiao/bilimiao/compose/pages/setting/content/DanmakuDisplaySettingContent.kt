package cn.a10miaomiao.bilimiao.compose.pages.setting.content

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import cn.a10miaomiao.bilimiao.compose.components.preference.sliderIntPreference
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import kotlinx.coroutines.flow.map
import cn.a10miaomiao.bilimiao.compose.common.preference.ProvidePreferenceLocals
import cn.a10miaomiao.bilimiao.compose.common.preference.preferenceCategory
import cn.a10miaomiao.bilimiao.compose.common.preference.sliderPreference
import cn.a10miaomiao.bilimiao.compose.common.preference.switchPreference

@Composable
internal fun DanmakuDisplaySettingContent(
    danmakuPreferences: SettingPreferences.Danmaku,
) {
    val windowInsets = localContentInsets()

    val dataStore = remember {
        appDataStore
    }

    val enableSetting = if (danmakuPreferences.name != "default") {
        dataStore.data.map {
            it[danmakuPreferences.enable] ?: false
        }.collectAsState(initial = true).value
    } else {
        true
    }

    ProvidePreferenceLocals(
        flow = rememberPreferenceFlow(dataStore)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (danmakuPreferences.name != "default") {
                switchPreference(
                    key = danmakuPreferences.enable.name,
                    title = {
                        Text(text = "Enable independent settings")
                    },
                    defaultValue = false,
                )
            }
            preferenceCategory(
                key = "display",
                title = {
                    Text(text = "Display")
                }
            )
            switchPreference(
                key = danmakuPreferences.show.name,
                enabled = {
                    enableSetting
                },
                title = {
                    Text(text = "Show danmaku")
                },
                defaultValue = true,
            )
            switchPreference(
                key = danmakuPreferences.r2lShow.name,
                enabled = {
                    enableSetting
                },
                title = {
                    Text(text = "Scrolling danmaku")
                },
                defaultValue = true,
            )
            switchPreference(
                key = danmakuPreferences.ftShow.name,
                enabled = {
                    enableSetting
                },
                title = {
                    Text(text = "Top danmaku")
                },
                defaultValue = true,
            )
            switchPreference(
                key = danmakuPreferences.fbShow.name,
                enabled = {
                    enableSetting
                },
                title = {
                    Text(text = "Bottom danmaku")
                },
                defaultValue = true,
            )
            switchPreference(
                key = danmakuPreferences.specialShow.name,
                enabled = {
                    enableSetting
                },
                title = {
                    Text(text = "Advanced danmaku")
                },
                defaultValue = true,
            )
            // Max scrolling danmaku lines
            sliderIntPreference(
                key = danmakuPreferences.r2lMaxLine.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 0,
                title = { Text(text = "Max scrolling danmaku lines") },
                valueRange = 0..20,
                valueSteps = 19,
                valueText = {
                    if (it == 0) {
                        Text(text = "Unlimited")
                    } else {
                        Text(text = "%d lines".format(it))
                    }
                }
            )
            // Max top danmaku lines
            sliderIntPreference(
                key = danmakuPreferences.ftMaxLine.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 0,
                title = { Text(text = "Max top danmaku lines") },
                valueRange = 0..20,
                valueSteps = 19,
                valueText = {
                    if (it == 0) {
                        Text(text = "Unlimited")
                    } else {
                        Text(text = "%d lines".format(it))
                    }
                }
            )
            // Max bottom danmaku lines
            sliderIntPreference(
                key = danmakuPreferences.fbMaxLine.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 0,
                title = { Text(text = "Max bottom danmaku lines") },
                valueRange = 0..20,
                valueSteps = 19,
                valueText = {
                    if (it == 0) {
                        Text(text = "Unlimited")
                    } else {
                        Text(text = "%d lines".format(it))
                    }
                }
            )
 
            preferenceCategory(
                key = "font",
                title = {
                    Text(text = "Font")
                }
            )
            // Font size
            sliderPreference(
                key = danmakuPreferences.fontSize.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 1f,
                title = { Text(text = "Font size") },
                valueRange = 0.1f..4f,
                valueSteps = 24,
                valueText = {
                    Text(text = "%.1fx".format(it))
                }
            )
            // Opacity
            sliderPreference(
                key = danmakuPreferences.opacity.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 1f,
                title = { Text(text = "Font opacity") },
                valueRange = 0f..1f,
                valueSteps = 99,
                valueText = {
                    Text(text = "${(it * 100).toInt()}%")
                }
            )

            preferenceCategory(
                key = "speed",
                title = {
                    Text(text = "Speed")
                }
            )
            // Danmaku speed
            sliderPreference(
                key = danmakuPreferences.speed.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 1f,
                title = { Text(text = "Danmaku speed") },
                valueRange = 0.1f..2f,
                valueSteps = 18,
                valueText = {
                    Text(text = "%.1fx".format(it))
                }
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