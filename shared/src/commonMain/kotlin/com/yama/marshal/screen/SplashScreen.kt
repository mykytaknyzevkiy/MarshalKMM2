package com.yama.marshal.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yama.marshal.tool.painterResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.YamaScreen

internal class SplashScreen(navigationController: NavigationController) : YamaScreen(navigationController) {
    override val route: String = "splash"

    @Composable
    override fun title(): String = ""

    @Composable
    override fun content(args: List<NavArg>) {
        Column(modifier = Modifier.fillMaxSize().padding(Sizes.screenPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painterResource("img_app_logo.png"),
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(Sizes.screenPadding))

            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }

    override val isToolbarEnable: Boolean = false
}