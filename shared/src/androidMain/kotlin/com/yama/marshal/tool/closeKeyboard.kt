package com.yama.marshal.tool

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun closeKeyboard() {
    val keyboardController = LocalSoftwareKeyboardController.current

    keyboardController?.hide()
}