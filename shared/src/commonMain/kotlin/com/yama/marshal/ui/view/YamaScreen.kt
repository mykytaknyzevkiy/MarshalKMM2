package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController

internal abstract class YamaScreen(private val navigationController: NavigationController) {
    abstract val route: String

    @Composable
    abstract fun title(): String

    @Composable
    open fun actions() {

    }

    open val isToolbarEnable: Boolean = true

    @Composable
    abstract fun content(args: List<NavArg>)
}