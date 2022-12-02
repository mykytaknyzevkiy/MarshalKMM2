package com.yama.marshal.screen.main

import androidx.compose.runtime.Composable
import com.yama.marshal.tool.stringResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.view.YamaScreen

internal class MainScreen(navigationController: NavigationController) : YamaScreen(navigationController) {
    override val route: String = "main"

    @Composable
    override fun title(): String = stringResource("app_name")

    override val viewModel: MainViewModel = MainViewModel()

    @Composable
    override fun content(args: List<NavArg>) {

    }
}