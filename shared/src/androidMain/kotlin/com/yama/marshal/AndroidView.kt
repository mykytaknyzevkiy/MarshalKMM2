package com.yama.marshal

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController

@OptIn(ExperimentalComposeUiApi::class)
internal var keyboardController: SoftwareKeyboardController? = null

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AndroidView() {
    keyboardController = LocalSoftwareKeyboardController.current

    App()
}