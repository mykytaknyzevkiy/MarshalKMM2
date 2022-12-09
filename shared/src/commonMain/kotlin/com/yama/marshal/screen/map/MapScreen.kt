package com.yama.marshal.screen.map

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.navigation.findInt
import com.yama.marshal.ui.navigation.findString
import com.yama.marshal.ui.view.YamaScreen

internal class MapScreen(navigationController: NavigationController) : YamaScreen(navigationController) {
    companion object {
        const val ARG_HOLE_ID = "hole_id"
        const val ARG_CART_ID = "cart_id"

        const val route = "map"
    }

    override val route: String = MapScreen.route

    override val viewModel: MapViewModel = MapViewModel()

    @Composable
    override fun content(args: List<NavArg>) {
        val holeID = args.findInt(ARG_HOLE_ID)
        val cartID = args.findInt(ARG_CART_ID)

        Text("IN PROGRESS")
    }

    override val isToolbarEnable: Boolean = true
}