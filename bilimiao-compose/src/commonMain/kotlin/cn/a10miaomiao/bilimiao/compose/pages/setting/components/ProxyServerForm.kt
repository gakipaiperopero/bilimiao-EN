package cn.a10miaomiao.bilimiao.compose.pages.setting.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun KeyValueListCard(
    title: String,
    buttonContent: @Composable RowScope.() -> Unit,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    content:  @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(
                    onClick = onButtonClick,
                    content = buttonContent,
                )
            }
            content()
        }
    }
}

@Stable
class KeyValueInputState(
    initialKey: String,
    initialValue: String,
) {
    var key by mutableStateOf(initialKey)
        private set
    var value by mutableStateOf(initialValue)
        private set

    fun changeKey(str: String) {
        key = str
    }

    fun changeValue(str: String) {
        value = str
    }
}

class KeyValueInputStateCarrier(
    private val initialKey: String = "",
    private val initialValue: String = "",
) {
    var inputState: KeyValueInputState? = null
    val key: String get() = inputState?.key ?: ""
    val value: String get() = inputState?.value ?: ""

    @Composable
    fun rememberKeyValueInputState(): KeyValueInputState {
        return inputState ?: remember {
            KeyValueInputState(initialKey, initialValue).also {
                inputState = it
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KeyValueInput(
    state: KeyValueInputState,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    infixSymbol: String = ":",
    keyPlaceholder: @Composable (() -> Unit)? = null,
    valuePlaceholder: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            modifier = Modifier.weight(1f),
            placeholder = keyPlaceholder,
            value = state.key,
            onValueChange = state::changeKey,
            singleLine = true,
        )
        Text(
            text = infixSymbol,
            modifier = Modifier.padding(horizontal = 5.dp)
        )
        TextField(
            modifier = Modifier.weight(2f),
            placeholder = valuePlaceholder,
            value = state.value,
            onValueChange = state::changeValue,
            singleLine = true,
        )
        IconButton(
//            modifier = Modifier.padding(start = 5.dp),
            onClick = onRemoveClick
        ) {
            Icon(
                imageVector = Icons.Filled.RemoveCircleOutline,
                contentDescription = "Remove",
            )
        }
    }
}

class ProxyServerFormState() {
    var name by mutableStateOf("")
        private set
    var host by mutableStateOf("")
        private set

    var isTrust by mutableStateOf(false)
        private set

    var enableAdvanced by mutableStateOf(false)
        private set

    val queryArgStates = mutableStateListOf<KeyValueInputStateCarrier>()
    val headerStates = mutableStateListOf<KeyValueInputStateCarrier>()

    fun changeName(str: String) {
        name = str
    }

    fun changeHost(str: String) {
        host = str
    }

    fun changeIsTrust(bool: Boolean) {
        isTrust = bool
    }

    fun changeEnableAdvanced(bool: Boolean) {
        enableAdvanced = bool
    }

    fun initQueryArgStates(list: List<KeyValueInputStateCarrier>) {
        queryArgStates.clear()
        queryArgStates.addAll(list)
    }

    fun initHeaderStates(list: List<KeyValueInputStateCarrier>) {
        headerStates.clear()
        headerStates.addAll(list)
    }
}

@Composable
fun rememberProxyServerFormState(): ProxyServerFormState {
    return remember {
        ProxyServerFormState()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyServerForm(
    state: ProxyServerFormState,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            value = state.name,
            onValueChange = state::changeName,
            label = {
                Text(text = "Server name")
            },
            singleLine = true,
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            value = state.host,
            onValueChange = state::changeHost,
            label = {
                Text(text = "Server address")
            },
            singleLine = true,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = state.isTrust,
                onCheckedChange = state::changeIsTrust,
            )
            Text(
                text = "Trust this server",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = state.enableAdvanced,
                onCheckedChange = { state.changeEnableAdvanced(it) },
            )
            Text(
                text = "Advanced settings",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (state.enableAdvanced) {
            KeyValueListCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                title = "Query parameters",
                buttonContent = {
                    Text(text = "Add parameter")
                },
                onButtonClick = {
                    state.queryArgStates.add(KeyValueInputStateCarrier())
                },
            ) {
                state.queryArgStates.forEachIndexed { index, stateCarrier ->
                    KeyValueInput(
                        state = stateCarrier.rememberKeyValueInputState(),
                        modifier = Modifier.padding(bottom = 5.dp),
                        infixSymbol = "=",
                        keyPlaceholder = { Text(text = "Key") },
                        valuePlaceholder = { Text(text = "Value") },
                        onRemoveClick = {
                            state.queryArgStates.removeAt(index)
                        }
                    )
                }
            }
            KeyValueListCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                title = "Headers",
                buttonContent = {
                    Text(text = "Add header")
                },
                onButtonClick = {
                    state.headerStates.add(KeyValueInputStateCarrier())
                },
            ) {
                state.headerStates.forEachIndexed { index, stateCarrier ->
                    KeyValueInput(
                        state = stateCarrier.rememberKeyValueInputState(),
                        modifier = Modifier.padding(bottom = 5.dp),
                        keyPlaceholder = { Text(text = "Name") },
                        valuePlaceholder = { Text(text = "Value") },
                        onRemoveClick = {
                            state.headerStates.removeAt(index)
                        }
                    )
                }
            }
        }
        Text(
            text = """Notes:
1. Server name is arbitrary, e.g.: Cat's server, Mouse's server.
2. Server address is the domain, e.g.: 10miaomiao.cn, fuck.bilibili.com.
3. Checking "Trust this server" will submit login info (token) to this server. Only check if you trust the server.
4. After checking "Trust this server", if you notice any account anomalies, change your password immediately and untrust or delete the server.""",
            modifier = Modifier.padding(vertical = 5.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}