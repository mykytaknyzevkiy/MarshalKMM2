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
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.ui.theme.Sizes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun YamaToolbar(
    title: @Composable () -> Unit,
    onBack: (() -> Unit)? = null,
    backIcon: ImageVector = Icons.Filled.ArrowBack,
    actions: (@Composable RowScope.() -> Unit)? = null,
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