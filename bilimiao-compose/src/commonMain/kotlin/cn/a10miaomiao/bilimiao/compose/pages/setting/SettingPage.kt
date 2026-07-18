package cn.a10miaomiao.bilimiao.compose.pages.setting

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.platform.AppInfo
import cn.a10miaomiao.bilimiao.compose.common.platform.FileStorage
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import cn.a10miaomiao.bilimiao.compose.components.preference.imageCachePreference
import cn.a10miaomiao.bilimiao.compose.pages.filter.FilterSettingPage
import cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoSettingInfo
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import cn.a10miaomiao.bilimiao.compose.common.preference.ProvidePreferenceLocals
import cn.a10miaomiao.bilimiao.compose.common.preference.preference
import cn.a10miaomiao.bilimiao.compose.common.preference.preferenceCategory
import cn.a10miaomiao.bilimiao.compose.common.preference.switchPreference
import org.kodein.di.compose.rememberInstance
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

@Serializable
class SettingPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: SettingPageViewModel = diViewModel { SettingPageViewModel(it) }
        SettingPageContent(viewModel)
    }
}

private class SettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val appInfo by instance<AppInfo>()
    private val fileStorage by instance<FileStorage>()
    private val pageNavigation by instance<PageNavigation>()

    val moreSettingList = MutableStateFlow(listOf<MiaoSettingInfo>())

    val versionName: String = appInfo.versionName

    init {
        loadMoreSettingList()
    }

    private fun loadMoreSettingList() {
        try {
            val jsonStr = fileStorage.readText("settingList.json") ?: return
            moreSettingList.value = MiaoJson.fromJson<List<MiaoSettingInfo>>(jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun preferenceClick(item: MiaoSettingInfo, openUrl: (String) -> Unit) {
        openUrl(item.url)
    }

    fun toThemePage() {
        pageNavigation.navigate(ThemeSettingPage())
    }

    fun toHomeSettingPage() {
        pageNavigation.navigate(HomeSettingPage())
    }

    fun toVideoSettingPage() {
        pageNavigation.navigate(VideoSettingPage())
    }

    fun toDanmakuSettingPage() {
        pageNavigation.navigate(DanmakuSettingPage())
    }

    fun toFilterSettingPage() {
        pageNavigation.navigate(FilterSettingPage())
    }

    fun toFlagsSettingPage() {
        pageNavigation.navigate(FlagsSettingPage())
    }

    fun toAboutPage() {
        pageNavigation.navigate(AboutPage())
    }
}


@Composable
private fun SettingPageContent(
    viewModel: SettingPageViewModel
) {
    PageConfig(
        title = "Settings"
    )
    val userStore: UserStore by rememberInstance()
    val userState = userStore.stateFlow.collectAsState().value
    val windowInsets = localContentInsets()
    val platformContext = LocalPlatformContext.current
    val moreSettingList by viewModel.moreSettingList.collectAsState()

    val dataStore = remember {
        appDataStore
    }
    val showLogoutDialog = remember {
        mutableStateOf(false)
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
                key = "general",
                title = {
                    Text( "General")
                }
            )
            switchPreference(
                key = SettingPreferences.IsBestRegion.name,
                defaultValue = false,
                title = {
                    Text( "Use old category layout")
                },
                summary = {
                    Text("你知道雪为什么是白色的吗")
                }
            )
            switchPreference(
                key = SettingPreferences.IsLockScreenOrientationPortrait.name,
                defaultValue = false,
                title = {
                    Text( "Lock app in portrait mode")
                },
                summary = {
                    Text("Doesn't affect fullscreen playback orientation")
                }
            )
            preference(
                key = "theme",
                title = {
                    Text("Switch theme")
                },
                summary = {
                    Text("你知道雪为什么是白色的吗")
                },
                onClick = viewModel::toThemePage,
            )
            preference(
                key = "home",
                title = {
                    Text("Home settings")
                },
                summary = {
                    Text("整个宇宙将为你闪烁")
                },
                onClick = viewModel::toHomeSettingPage
            )
            preference(
                key = "video",
                title = {
                    Text("Playback settings")
                },
                summary = {
                    Text("咖啡拿铁,咖啡摩卡,卡布奇诺!")
                },
                onClick = viewModel::toVideoSettingPage
            )
            preference(
                key = "danmaku",
                title = {
                    Text("Danmaku settings")
                },
                summary = {
                    Text("相信的心就是你的魔法")
                },
                onClick = viewModel::toDanmakuSettingPage,
            )
            preference(
                key = "filter",
                title = {
                    Text("Filter management")
                },
                summary = {
                    Text("Applies to Time Machine, home recommended, and trending")
                },
                onClick = viewModel::toFilterSettingPage
            )
            switchPreference(
                key = SettingPreferences.IsAutoCheckVersion.name,
                title = {
                    Text("Auto-check for updates")
                },
                summary = {
                    Text("已经没有什么好害怕的了")
                },
                defaultValue = true,
            )
            imageCachePreference(
                key = "image_cache",
            )

            preference(
                key = "flags_setting",
                title = {
                    Text("Experimental features")
                },
                summary = {
                    Text("自然选择号，前进四！")
                },
                onClick = viewModel::toFlagsSettingPage,
            )

            preferenceCategory(
                key = "other",
                title = {
                    Text( "Other")
                }
            )
            preference(
                key = "about",
                title = {
                    Text("About")
                },
                summary = {
                    Text("Version: ${viewModel.versionName}")
                },
                onClick = viewModel::toAboutPage
            )
            moreSettingList.forEach {
                if (it.type == "pref") {
                    preference(
                        key = it.name,
                        title = {
                            Text(text = it.title)
                        },
                        summary = {
                            Text(text = it.summary)
                        },
                        onClick = {
                            viewModel.preferenceClick(it) { url ->
                                platformContext.openUrl(url)
                            }
                        },
                    )
                }
            }
            if (userState.isLogin()) {
                preference(
                    key = "logout",
                    title = {
                        Text(
                            text = "Log out",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color.Red,
                        )
                    },
                    onClick = {
                        showLogoutDialog.value = true
                    }
                )
             }

            item("bottom") {
                Spacer(
                    modifier = Modifier.height(
                        windowInsets.bottom
                    )
                )
            }
        }
    }

    if (showLogoutDialog.value) {
        AlertDialog(
            title = {
                Text(text = "Prompt")
            },
            text = {
                Text(text = "Confirm log out?")
            },
            onDismissRequest = {
                showLogoutDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userStore.logout()
                        showLogoutDialog.value = false
                    }
                ) {
                    Text(text = "Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog.value = false
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}
