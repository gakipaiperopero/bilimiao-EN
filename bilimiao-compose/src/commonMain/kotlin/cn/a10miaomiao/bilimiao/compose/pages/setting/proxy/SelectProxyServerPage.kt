package cn.a10miaomiao.bilimiao.compose.pages.setting.proxy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.proxy.ProxyRepository
import cn.a10miaomiao.bilimiao.compose.pages.setting.components.ProxyServerCard
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.proxy.BiliUposInfo
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import org.kodein.di.compose.rememberInstance
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

@Serializable
class SelectProxyServerPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: SelectProxyServerPageViewModel = diViewModel { SelectProxyServerPageViewModel(it) }
        SelectProxyServerPageContent(viewModel)
    }

}

internal class SelectProxyServerPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val proxyRepository by instance<ProxyRepository>()
    private val pageNavigation by instance<PageNavigation>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    val serverList = MutableStateFlow(emptyList<ProxyServerInfo>())
    var selectedServerIndex = MutableStateFlow(-1)

    val uposList = listOf(
        BiliUposInfo("none", "No replacement", ""),
        BiliUposInfo("ali", "ali (Alibaba Cloud)", "upos-sz-mirrorali.bilivideo.com"),
        BiliUposInfo("cos", "cos (Tencent Cloud)", "upos-sz-mirrorcos.bilivideo.com"),
        BiliUposInfo("hw", "hw (Huawei Cloud)", "upos-sz-mirrorhw.bilivideo.com"),
        BiliUposInfo("akamai", "akamai (Akamai Overseas)", "upos-hz-mirrorakam.akamaized.net"),
        BiliUposInfo("aliov", "aliov (Alibaba Overseas)", "upos-sz-mirroraliov.bilivideo.com"),
        BiliUposInfo("aliov", "cosov (Tencent Overseas)", "upos-sz-mirrorcosov.bilivideo.com"),
        BiliUposInfo("tf_hw", "tf_hw (Huawei)", "upos-tf-all-hw.bilivideo.com"),
        BiliUposInfo("tf_tx", "tf_tx (Tencent)", "upos-tf-all-tx.bilivideo.com"),
    )

    val selectedUpos = MutableStateFlow(uposList[0])

    fun readServerList() {
        serverList.value = proxyRepository.serverList()
        val uposName = proxyRepository.uposName()
        uposList.find { it.name == uposName }?.let {
            selectedUpos.value = it
        }
    }

    fun selectedServer(index: Int) {
        selectedServerIndex.value = index
    }

    fun clearServer() {
        selectedServerIndex.value = -1
    }

    fun changeUpos(value: BiliUposInfo) {
        selectedUpos.value = value
    }

    fun applyServer() {
        proxyRepository.saveUposName(selectedUpos.value.name)
        val uposHost = selectedUpos.value.host
        val proxyServer = serverList.value[selectedServerIndex.value]
        basePlayerDelegate.setProxy(proxyServer, uposHost)
        pageNavigation.popBackStack()
    }

    fun toAddPage() {
        pageNavigation.navigate(AddProxyServerPage())
    }

    fun toEditPage(
        index: Int
    ) {
        pageNavigation.navigate(EditProxyServerPage(
            index = index
        ))
    }
}

@Composable
internal fun SelectProxyServerPageContent(
    viewModel: SelectProxyServerPageViewModel
) {
    PageConfig(
        title = "Region restriction - Select proxy"
    )
    val basePlayerDelegate: BasePlayerDelegate by rememberInstance()
    val windowInsets = localContentInsets()

    val selectedUpos by viewModel.selectedUpos.collectAsState()
    var uposMenuExpanded by remember { mutableStateOf(false) }

    val serverList by viewModel.serverList.collectAsState()
    val selectedServerIndex by viewModel.selectedServerIndex.collectAsState()

    val serverListVersion by viewModel.proxyRepository.serverListVersion.collectAsState()
    LaunchedEffect(viewModel, serverListVersion) {
        viewModel.readServerList()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(top = windowInsets.topDp.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Proxy server list",
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Button(onClick = viewModel::toAddPage) {
                Text(text = "Add server")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                serverList.size,
                key = { it },
            ) { i ->
                val item = serverList[i]
                ProxyServerCard(
                    name = item.name,
                    host = item.host,
                    isTrust = item.isTrust,
                    onClick = { viewModel.selectedServer(i) }
                )
            }

            item {
                if (serverList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(bottom = windowInsets.bottom),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No servers added yet",
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .padding(bottom = windowInsets.bottom),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "That's all",
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        } // LazyColumn
        if (selectedServerIndex >= 0) {
            val selectedServer = serverList[selectedServerIndex]
            AlertDialog(
                onDismissRequest = viewModel::clearServer,
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = selectedServer.name,
                            fontWeight = FontWeight.W700,
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(
                            onClick = {
                                viewModel.toEditPage(selectedServerIndex)
                            },
                        ) {
                            Text("Edit server")
                        }
                    }
                },
                text = {
                    Column() {
                        Text(
                            text = "Server: ${selectedServer.host}",
                            fontSize = 16.sp,
                        )
                        if (selectedServer.isTrust) {
                            Text(
                                text = "Trusted server",
                                color = Color.Red,
                                fontSize = 16.sp,
                            )
                        } else {
                            Text(
                                text = "Untrusted, login info will not be submitted",
                                color = Color.Gray,
                                fontSize = 16.sp,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Replace upos video server:"
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .clickable { uposMenuExpanded = !uposMenuExpanded },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = selectedUpos.label,
                                        textAlign = TextAlign.Center,
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand upos video server menu"
                                    )
                                }

                                DropdownMenu(
                                    expanded = uposMenuExpanded,
                                    onDismissRequest = { uposMenuExpanded = false },
                                ) {
                                    viewModel.uposList.forEach {
                                        DropdownMenuItem(
                                            text = {
                                                Text(text = it.label)
                                            },
                                            onClick = {
                                                viewModel.changeUpos(it)
                                                uposMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = viewModel::applyServer,
                    ) {
                        Text(
                            "Use this proxy",
                            fontWeight = FontWeight.W700,
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = viewModel::clearServer
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.W700,
                        )
                    }
                }
            ) // AlertDialog
        }  // if
    }
}
