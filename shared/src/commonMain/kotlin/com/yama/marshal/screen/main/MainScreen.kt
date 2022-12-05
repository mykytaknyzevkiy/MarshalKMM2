package com.yama.marshal.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yama.marshal.tool.stringResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.YamaScreen

internal class MainScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "main"

    @Composable
    override fun title(): String = stringResource("app_name")

    override val viewModel: MainViewModel = MainViewModel()

    @Composable
    override fun content(args: List<NavArg>) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxHeight().weight(1f).padding(Sizes.screenPadding)
            )

            NavigationBar()
        }
    }

    @Composable
    private fun NavigationBar() = Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center
    ) {
        NavigationItem(YamaColor.fleet_navigation_card_bg, "fleet")
        NavigationItem(YamaColor.hole_navigation_card_bg, "holes")
        NavigationItem(YamaColor.alert_navigation_card_bg, "alerts")
    }

    @Composable
    private fun ColumnScope.NavigationItem(backgroundColor: Color, label: String) {
        Card(
            modifier = Modifier.size(200.dp).weight(1f),
            shape = RoundedCornerShape(0),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(50.dp),
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.height(Sizes.screenPadding / 2))

                Text(label.uppercase())
            }
        }
    }

    override val isToolbarEnable: Boolean = true
}