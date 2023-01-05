package com.yama.marshal.tool

import co.touchlab.kermit.Logger
import com.yama.marshal.currentRootViewController
import platform.UIKit.endEditing

internal actual fun closeKeyboard() {
    Logger.d("Keyboard", message = {
        "closeKeyboard"
    })
    currentRootViewController.view().endEditing(true)
    currentRootViewController.view().endEditing(true)
}