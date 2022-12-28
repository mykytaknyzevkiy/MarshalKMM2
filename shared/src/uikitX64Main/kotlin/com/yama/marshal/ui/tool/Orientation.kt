package com.yama.marshal.ui.tool

import kotlinx.cinterop.copy
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIScreen
import platform.UIKit.UIUserInterfaceIdiomPad

internal actual fun currentOrientation(): Orientation {
    return if (UIDevice.currentDevice().userInterfaceIdiom == UIUserInterfaceIdiomPad)
        Orientation.LANDSCAPE
    else
        Orientation.PORTRAIT
}

internal actual fun screenSize(): ScreenSize {
    var width = 0
    var height = 0

    UIScreen.mainScreen.bounds.copy {
        width = this.size.width.toInt()
        height = this.size.height.toInt()
    }

    return ScreenSize(
        width = width,
        height = height
    )
}