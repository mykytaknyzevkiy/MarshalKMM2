package com.yama.marshal.ui.tool

import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation

internal actual fun currentOrientation(): Orientation {
    return if (UIDevice.currentDevice().orientation == UIDeviceOrientation.UIDeviceOrientationPortrait) {
        Orientation.LANDSCAPE
    } else {
        Orientation.PORTRAIT
    }
}