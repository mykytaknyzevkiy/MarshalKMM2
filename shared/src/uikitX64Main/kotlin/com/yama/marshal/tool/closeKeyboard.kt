package com.yama.marshal.tool

import co.touchlab.kermit.Logger
import com.yama.marshal.currentRootView
import platform.UIKit.endEditing

internal actual fun closeKeyboard() {
    Logger.d("Keyboard", message = {
        "closeKeyboard"
    })
    currentRootView.view().endEditing(true)
    currentRootView.view().endEditing(true)
}