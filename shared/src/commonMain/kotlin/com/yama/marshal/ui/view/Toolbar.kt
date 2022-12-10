package com.yama.marshal.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.ui.theme.Sizes

@Composable
internal fun YamaToolbar(
    title: @Composable () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary,
    onBack: (() -> Unit)? = null,
    backIcon: ImageVector = Icons.Filled.ArrowBack,
    actions: (@Composable RowScope.() -> Unit)? = null,
) = Surface(color = color) {
    Row(
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = LocalAppDimens.current.toolbar_height),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null)
            IconButton(
                modifier = Modifier.size(Sizes.buttonSize),
                onClick = { onBack.invoke() }) {
                Icon(
                    modifier = Modifier.size(Sizes.button_icon_size),
                    imageVector = backIcon, contentDescription = null
                )
            }

        Box(modifier = Modifier.weight(1f)) {
            title()
        }

        if (actions != null)
            actions()
        else
            Spacer(modifier = Modifier.width(Sizes.screenPadding))
    }
}