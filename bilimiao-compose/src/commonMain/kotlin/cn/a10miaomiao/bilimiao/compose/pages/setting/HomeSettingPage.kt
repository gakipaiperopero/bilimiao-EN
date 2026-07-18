package cn.a10miaomiao.bilimiao.compose.pages.setting

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import cn.a10miaomiao.bilimiao.compose.components.preference.listStylePreference
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import kotlinx.serialization.Serializable
import cn.a10miaomiao.bilimiao.compose.common.preference.ListPreferenceType
import cn.a10miaomiao.bilimiao.compose.common.preference.ProvidePreferenceLocals
import cn.a10miaomiao.bilimiao.compose.common.preference.listPreference
import cn.a10miaomiao.bilimiao.compose.common.preference.preferenceCategory
import cn.a10miaomiao.bilimiao.compose.common.preference.switchPreference
import org.kodein.di.DI
import org.kodein.di.DIAware

@Serializable
class HomeSettingPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: HomeSettingPageViewModel = diViewModel {
            HomeSettingPageViewModel(it)
        }
        HomeSettingPageContent(viewModel)
    }
}

private class HomeSettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val entryViews = mapOf(
        SettingConstants.HOME_ENTRY_VIEW_DEFAULT to "Default",
        SettingConstants.HOME_ENTRY_VIEW_RECOMMEND to "Recommended",
        SettingConstants.HOME_ENTRY_VIEW_POPULAR to "Trending",
    )

}


@Composable
private fun HomeSettingPageContent(
    viewModel: HomeSettingPageViewModel
) {
    PageConfig(
        title = "Home settings"
    )
    val windowInsets = localContentInsets()

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
                key = "top_nav",
                title = {
                    Text("Home top navigation settings")
                }
            )
            listPreference(
                key = SettingPreferences.HomeEntryView.name,
                defaultValue = SettingConstants.HOME_ENTRY_VIEW_DEFAULT,
                type = ListPreferenceType.DROPDOWN_MENU,
                title = {
                    Text("Home entry")
                },
                summary = {
                    Text(text = "Current: " + viewModel.entryViews[it])
                },
                values = viewModel.entryViews.keys.toList(),
                valueToText = {
                    val text = viewModel.entryViews[it]
                    AnnotatedString(text ?: "Unknown")
                },
            )
            switchPreference(
                key = SettingPreferences.HomeRecommendShow.name,
                title = {
                    Text("Show recommended")
                },
                defaultValue = true,
            )
            switchPreference(
                key = SettingPreferences.HomePopularShow.name,
                title = {
                    Text("Show trending")
                },
                defaultValue = true,
            )

            preferenceCategory(
                key = "popular",
                title = {
                    Text("Trending settings")
                }
            )
            switchPreference(
                key = SettingPreferences.HomePopularCarryToken.name,
                title = {
                    Text("Personalized trending list")
                },
                summary = {
                    Text("Manual refresh required after change")
                },
                defaultValue = true,
            )

            preferenceCategory(
                key = "recommend",
                title = {
                    Text("Recommended settings")
                }
            )
            listStylePreference(
                key = SettingPreferences.HomeRecommendListStyle.name,
                defaultValue = 0,
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
