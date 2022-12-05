package com.yama.marshal.ui.tool

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
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

@RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR2)
@Composable
internal actual fun screenSize(): ScreenSize {
    val context = LocalContext.current

    return ScreenSize(
        width = context.resources.configuration.screenWidthDp,
        height = context.resources.configuration.screenHeightDp
    )
}