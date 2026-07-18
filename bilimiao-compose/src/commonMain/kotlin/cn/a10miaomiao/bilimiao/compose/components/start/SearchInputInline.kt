package cn.a10miaomiao.bilimiao.compose.components.start

import cn.a10miaomiao.bilimiao.compose.common.BackHandler
import cn.a10miaomiao.bilimiao.compose.common.isCompactWindow
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.base.PageSearchMethod
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoOutlinedCard
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchInputViewModel
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchInputViewModel.SuggestInfo
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchResultPage
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.delay
import org.kodein.di.compose.rememberInstance

@Composable
fun SearchInputInline(
    modifier: Modifier = Modifier,
    initKeyword: String,
    initMode: Int,
    pageSearchMethod: PageSearchMethod?,
    onDismissRequest: () -> Unit,
) {
    val isCompact = isCompactWindow()

    val viewModel: SearchInputViewModel = diViewModel { SearchInputViewModel(it) }
    val pageNavigation: PageNavigation by rememberInstance()

    var text by remember {
        mutableStateOf(
            TextFieldValue(
                text = initKeyword,
                selection = TextRange(initKeyword.length)
            )
        )
    }
    var mode by remember { mutableStateOf(initMode) }
    val focusRequester = remember { FocusRequester() }
    var isEditingHistory by remember { mutableStateOf(false) }

    LaunchedEffect(text.text) {
        viewModel.loadSuggestData(text.text, text.text)
        if (text.text.isNotEmpty()) {
            isEditingHistory = false
        }
    }

    BackHandler {
        onDismissRequest()
    }

    var showClearAll by remember { mutableStateOf(false) }

    fun startSearch(keyword: String) {
        if (keyword.isEmpty()) {
            GlobalToaster.show("Enter ID or keyword")
            return
        }
        viewModel.addSearchHistory(keyword)
        if (mode == 0) {
            pageNavigation.navigate(SearchResultPage(keyword))
        } else {
            pageSearchMethod?.onSearch(keyword)
        }
        onDismissRequest()
    }

    fun deleteHistory(text: String) {
        viewModel.deleteSearchHistory(text)
        GlobalToaster.show("Deleted")
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .let {
                if (isCompact) {
                    it.safeDrawingPadding()
                } else {
                    it
                        .safeDrawingPadding()
                        .padding(8.dp)
                }
            }
            .then(modifier),
        contentAlignment = if (isCompact) Alignment.BottomCenter
            else Alignment.TopStart,
    ) {
        MiaoOutlinedCard(
            modifier = Modifier
                .widthIn(max = 400.dp),
            enabled = false,
        ) {
            val historySuggestList by viewModel.historyListFlow.collectAsState()
            val suggestList by viewModel.suggestListFlow.collectAsState()
            val scrollState = rememberScrollState()
            LaunchedEffect(historySuggestList) {
                if (historySuggestList.isEmpty()) {
                    isEditingHistory = false
                }
            }
            val showSuggestList by remember {
                derivedStateOf {
                    when {
                        text.text.isEmpty() -> historySuggestList
                        else -> suggestList
                    }.let {
                        if (isCompact) it.asReversed() else it
                    }
                }
            }
            LaunchedEffect(showSuggestList) {
                if (isEditingHistory) {
                    return@LaunchedEffect
                }
                if (isCompact && showSuggestList.isNotEmpty()) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                } else {
                    scrollState.scrollTo(0)
                }
            }

            if (!isCompact) {
                SearchTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    isCompact = false,
                    text = text.text,
                    onTextChange = { text = TextFieldValue(it, TextRange(it.length)) },
                    onSearch = ::startSearch,
                    focusRequester = focusRequester,
                    mode = mode,
                    onModeChange = { mode = it },
                    pageSearchMethod = pageSearchMethod,
                )
            }

            if (text.text.isEmpty() && showSuggestList.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Search history",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (historySuggestList.isNotEmpty()) {
                        val contentPadding = PaddingValues(0.dp)
                        if (isEditingHistory) {
                            TextButton(
                                onClick = { showClearAll = true },
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .height(32.dp),
                                contentPadding = contentPadding,
                            ) {
                                Text("Clear")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = {
                                    isEditingHistory = false
                                },
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .height(32.dp),
                                contentPadding = contentPadding,
                            ) {
                                Text("Done")
                            }
                        } else {
                            TextButton(
                                onClick = { isEditingHistory = true },
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .height(32.dp),
                                contentPadding = contentPadding,
                            ) {
                                Text("Edit")
                            }
                        }
                    }
                }
            }

            // Suggestions list displayed as flow chips
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .verticalScroll(scrollState),
                contentAlignment = Alignment.BottomStart
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    showSuggestList.forEach { item: SuggestInfo ->
                        val isHistoryItem = item.type == SearchInputViewModel.SuggestType.HISTORY
                        if (text.text.isEmpty() && isHistoryItem && isEditingHistory) {
                            SuggestionChip(
                                onClick = {
                                    deleteHistory(item.text)
                                },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(item.text)
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete search history",
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        } else {
                            key(item.text) {
                                SuggestionChip(
                                    onClick = {
                                        when (item.type) {
                                            SearchInputViewModel.SuggestType.AV -> {
                                                pageNavigation.navigate(VideoDetailPage(id = item.value))
                                            }
                                            SearchInputViewModel.SuggestType.SS -> {
                                                pageNavigation.navigate(BangumiDetailPage(id = item.value))
                                            }
                                            else -> {
                                                startSearch(item.value)
                                            }
                                        }
                                        onDismissRequest()
                                    },
                                    label = { Text(item.text) }
                                )
                            }
                        }
                    }
                }
            }
            if (isCompact) {
                SearchTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    isCompact = true,
                    text = text.text,
                    onTextChange = { text = TextFieldValue(it, TextRange(it.length)) },
                    onSearch = ::startSearch,
                    focusRequester = focusRequester,
                    mode = mode,
                    onModeChange = { mode = it },
                    pageSearchMethod = pageSearchMethod,
                )
            }
        }
    }


    if (showClearAll) {
        AlertDialog(
            onDismissRequest = { showClearAll = false },
            title = { Text("Confirm clear, meow~") },
            text = { Text("Search history will be cleared") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllSearchHistory()
                    GlobalToaster.show("Cleared~")
                    isEditingHistory = false
                    showClearAll = false
                }) { Text("Confirm clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearAll = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SearchTextField(
    modifier: Modifier = Modifier,
    isCompact: Boolean,
    text: String,
    onTextChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    focusRequester: FocusRequester,
    mode: Int,
    onModeChange: (Int) -> Unit,
    pageSearchMethod: PageSearchMethod?,
) {
    // Bottom input and actions bar
    Surface(
        modifier = modifier,
        tonalElevation = 3.dp,
        shadowElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            if (isCompact) {
                SearchModeSelector(
                    mode = mode,
                    onModeChange = onModeChange,
                    pageSearchMethod = pageSearchMethod,
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    value = text,
                    onValueChange = onTextChange,
                    singleLine = true,
                    placeholder = { Text("Enter ID or keyword") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (text.isNotEmpty()) {
                                IconButton(
                                    modifier = Modifier.size(24.dp),
                                    onClick = { onTextChange("") },
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                            TextButton(
                                onClick = { onSearch(text) },
                                enabled = text.isNotEmpty()
                            ) {
                                Text("Search")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearch(text) }
                    ),
                    shape = MaterialTheme.shapes.large,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }
            if (!isCompact) {
                SearchModeSelector(
                    mode = mode,
                    onModeChange = onModeChange,
                    pageSearchMethod = pageSearchMethod,
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SearchModeSelector(
    mode: Int,
    onModeChange: (Int) -> Unit,
    pageSearchMethod: PageSearchMethod?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = mode == 0,
            onClick = { onModeChange(0) },
            label = { Text("Site-wide search") }
        )
        pageSearchMethod?.let {
            FilterChip(
                selected = mode == 1,
                onClick = { onModeChange(1) },
                label = { Text(it.name) }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
