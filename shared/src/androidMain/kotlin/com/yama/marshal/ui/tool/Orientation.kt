package com.yama.marshal.ui.tool

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun currentOrientation(): Orientation {
    val context = LocalContext.current

    val orientation: Int = context.resources.configuration.orientation

    return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        Orientation.LANDSCAPE
    } else {
        Orientation.PORTRAIT
    }
}