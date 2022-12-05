package com.yama.marshal

import androidx.compose.ui.window.Application

internal val currentRootView by lazy {
    Application {
        App()
    }
}

fun getRootController() = currentRootView