package com.yama.marshal.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

    val screen: YamaScreen = screens.find { it.route == currentRoute.route } ?: return

    Column(modifier = modifier) {
        if (screen.isToolbarEnable) {
            YamaToolbar(
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

        Box(modifier = Modifier.weight(1f)
            .background(MaterialTheme.colorScheme.background)
        ) {
            screen.contentReal(currentRoute.args)
        }

        screen.bottomBar()
    }
}