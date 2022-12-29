package com.yama.marshal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import com.yama.marshal.screen.login.LoginScreen
import com.yama.marshal.screen.main.MainScreen
import com.yama.marshal.screen.map.MapScreen
import com.yama.marshal.screen.send_message.SendMessageScreen
import com.yama.marshal.screen.splash.SplashScreen
import com.yama.marshal.ui.navigation.rememberNavController
import com.yama.marshal.ui.theme.Dimensions
import com.yama.marshal.ui.tool.screenSize
import com.yama.marshal.ui.view.NavHost

internal val LocalAppDimens = compositionLocalOf<Dimensions> { Dimensions.Phone }

@Composable
internal fun ProvideDimens(
    dimensions: Dimensions,
    content: @Composable () -> Unit
) {
    val dimensionSet = remember { dimensions }
    CompositionLocalProvider(LocalAppDimens provides dimensionSet, content = content)
}

@Composable
internal fun App() {
    val navigationController = rememberNavController("splash")

    val screenSize = screenSize()

    ProvideDimens(
        dimensions = if (screenSize.width <= 400)
            Dimensions.PhoneSmall
        else if (screenSize.width <= 480)
            Dimensions.Phone
        else
            Dimensions.Tablet
    ) {
        NavHost(
            navigationController = navigationController,
            screens = arrayOf(
                SplashScreen(navigationController),
                LoginScreen(navigationController),
                MainScreen(navigationController),
                MapScreen(navigationController),
                SendMessageScreen(navigationController)
            )
        )
    }
}