package com.yama.marshal.screen

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import com.yama.marshal.tool.painterResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.view.YamaScreen

internal class SplashScreen(navigationController: NavigationController) : YamaScreen(navigationController) {
    override val route: String = "splash"

    @Composable
    override fun title(): String = ""

    @Composable
    override fun content(args: List<NavArg>) {
        Image(
            painterResource("img_app_logo.png"),
            contentDescription = null
        )
    }

    override val isToolbarEnable: Boolean = false
}