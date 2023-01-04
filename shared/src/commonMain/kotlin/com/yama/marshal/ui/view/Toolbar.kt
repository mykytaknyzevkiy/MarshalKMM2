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
import androidx.compose.ui.unit.dp
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.ui.theme.Sizes

@Composable
internal fun YamaToolbar(
    title: @Composable () -> Unit,
    onBack: (() -> Unit)? = null,
    backIcon: ImageVector = Icons.Filled.ArrowBack,
    actions: (@Composable RowScope.() -> Unit)? = null,
) = PlatformToolbar(
    title, onBack, backIcon, actions
)

@Composable
internal expect fun PlatformToolbar(
    title: @Composable () -> Unit,
    onBack: (() -> Unit)?,
    backIcon: ImageVector,
    actions: (@Composable RowScope.() -> Unit)?
)