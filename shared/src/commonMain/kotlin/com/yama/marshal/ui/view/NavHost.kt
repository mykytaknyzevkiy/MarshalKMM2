package com.yama.marshal.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.YamaTheme

@Composable
internal fun NavHost(
    modifier: Modifier = Modifier.fillMaxSize(),
    navigationController: NavigationController,
    screens: Array<YamaScreen>,
) {
    val currentRoute by navigationController.currentRoute.collectAsState()

    YamaTheme {
        Surface(modifier = modifier) {
            Column(modifier = Modifier.fillMaxSize()) {
                val screen = screens.find { it.route == currentRoute.route }

                if (screen != null) {
                    if (screen.isToolbarEnable) {
                        YamaToolbar(
                            color = screen.toolbarColor(),
                            title = {
                             screen.titleContent()
                            },
                            actions = {
                                screen.actions()
                            },
                            onBack = if (navigationController.isBackStackEmpty())
                                null
                            else {
                                { navigationController.popBack() }
                            }
                        )
                    }
                    screen.contentReal(currentRoute.args)
                } else {
                    Text("No screen for route ${currentRoute.route}")
                }
            }
        }
    }
}