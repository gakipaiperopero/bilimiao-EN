package cn.a10miaomiao.bilimiao.compose.components.preference

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cn.a10miaomiao.bilimiao.compose.common.clearImageCache
import cn.a10miaomiao.bilimiao.compose.common.getImageCacheSize
import cn.a10miaomiao.bilimiao.compose.common.preference.Preference
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster

inline fun LazyListScope.imageCachePreference(
    key: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    item(key = key, contentType = "ImageCachePreference") {
        ImageCachePreference(modifier = modifier)
    }
}

@Composable
fun ImageCachePreference(
    modifier: Modifier = Modifier,
) {
    var cacheSize by remember {
        mutableStateOf(getImageCacheSize())
    }
    var showDialog by remember {
        mutableStateOf(false)
    }

    Preference(
        modifier = modifier,
        title = {
            Text("Image cache")
        },
        summary = {
            Text(cacheSize)
        },
        onClick = {
            showDialog = true
        }
    )

    if (showDialog) {
        AlertDialog(
            title = {
                Text("Prompt")
            },
            text = {
                Text("Clear image cache? Current cache size: $cacheSize")
            },
            onDismissRequest = {
                showDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        clearImageCache()
                        GlobalToaster.show("Cleaned up, cleared $cacheSize")
                        cacheSize = "0Byte"
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
