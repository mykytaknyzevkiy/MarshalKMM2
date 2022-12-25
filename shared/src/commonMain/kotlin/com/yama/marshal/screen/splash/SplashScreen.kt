package com.yama.marshal.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yama.marshal.tool.painterResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class SplashScreen(navigationController: NavigationController) : YamaScreen(navigationController) {
    override val route: String = "splash"

    override val viewModel: SplashViewModel = SplashViewModel()

    @Composable
    override fun title(): String = ""

    @Composable
    override fun content(args: List<NavArg>) {
        Column(modifier = Modifier.fillMaxSize().padding(Sizes.screenPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painterResource("img_app_logo.png"),
                modifier = Modifier.padding(Sizes.screenPadding),
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(Sizes.screenPadding))

            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LaunchedEffect(0) {
            viewModel.currentViewState
                .onEach {
                    if (it is SplashViewState.RequestLogin)
                        navigationController.navigateToAndFinish("login")
                    else if (it is SplashViewState.OK)
                        navigationController.navigateToAndFinish("main")
                }
                .launchIn(this)

            viewModel.startData()
        }
    }

    override val isToolbarEnable: Boolean = false
}