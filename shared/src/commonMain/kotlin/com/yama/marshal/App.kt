package com.yama.marshal

import androidx.compose.runtime.Composable
import com.yama.marshal.screen.SplashScreen
import com.yama.marshal.ui.navigation.rememberNavController
import com.yama.marshal.ui.view.NavHost

@Composable
internal fun App() {
    val navigationController = rememberNavController("splash")

    NavHost(
        navigationController = navigationController,
        screens = arrayOf(SplashScreen(navigationController))
    )
}