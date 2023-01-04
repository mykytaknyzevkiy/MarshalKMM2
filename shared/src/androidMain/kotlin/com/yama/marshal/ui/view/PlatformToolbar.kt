package com.yama.marshal.ui.view

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun PlatformToolbar(
    title: @Composable () -> Unit,
    onBack: (() -> Unit)?,
    backIcon: ImageVector,
    actions: @Composable (RowScope.() -> Unit)?
) = TopAppBar(
    title = {
        title()
    },
    actions = actions ?: {},
    navigationIcon = {
        if (onBack != null)
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = backIcon, contentDescription = null
                )
            }
    },
    colors = TopAppBarDefaults.smallTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        scrolledContainerColor = MaterialTheme.colorScheme.primary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
    )
)