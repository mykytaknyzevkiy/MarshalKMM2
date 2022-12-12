package com.yama.marshal.screen.hole_list

import androidx.compose.runtime.Composable
import com.yama.marshal.screen.main.MainViewModel
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.view.YamaScreen

internal class HoleScreen(navigationController: NavigationController, override val viewModel: MainViewModel) :
    YamaScreen(navigationController) {
    companion object {
        const val ROUTE = "HoleScreen"
    }

    override val route: String = ROUTE

    @Composable
    override fun content(args: List<NavArg>) {
        TODO("Not yet implemented")
    }
}