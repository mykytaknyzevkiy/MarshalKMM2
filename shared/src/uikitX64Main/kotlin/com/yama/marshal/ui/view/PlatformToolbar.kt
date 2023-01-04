package com.yama.marshal.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.useContents
import platform.UIKit.UIApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun PlatformToolbar(
    title: @Composable () -> Unit,
    onBack: (() -> Unit)?,
    backIcon: ImageVector,
    actions: @Composable (RowScope.() -> Unit)?
) = Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
    Spacer(modifier = Modifier.height(
        (UIApplication.sharedApplication().statusBarFrame().useContents { this.size.height } / 1.5).dp
    ))
    TopAppBar(
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
}