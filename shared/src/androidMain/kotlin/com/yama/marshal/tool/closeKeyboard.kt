package com.yama.marshal.tool

import androidx.compose.ui.ExperimentalComposeUiApi
import com.yama.marshal.keyboardController

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun closeKeyboard() {
    keyboardController?.hide()
}