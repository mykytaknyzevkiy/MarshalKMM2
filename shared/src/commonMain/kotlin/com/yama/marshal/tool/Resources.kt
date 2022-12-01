package com.yama.marshal.tool

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.Font

@Composable
internal expect fun fontResources(
    font: String
): Font

internal expect fun painterResource(
    path: String
): Painter