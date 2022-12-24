package com.yama.marshal.tool

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font

@Composable
actual fun fontResources(
    font: String
): Font {
    val context: Context = LocalContext.current
    val name = font.substringBefore(".")
    val resId: Int =
        context.resources.getIdentifier(name, "font", context.packageName)
    return Font(resId)
}

@Composable
actual fun painterResource(path: String): Painter {
    val context: Context = LocalContext.current
    val name = path.substringBefore(".")
    val resId: Int =
        context.resources.getIdentifier(name, "drawable", context.packageName)
    return androidx.compose.ui.res.painterResource(resId)
}

@Composable
internal actual fun stringResource(key: String): String {
    val context: Context = LocalContext.current
    val resId: Int =
        context.resources.getIdentifier(key, "string", context.packageName)
    return androidx.compose.ui.res.stringResource(resId)
}