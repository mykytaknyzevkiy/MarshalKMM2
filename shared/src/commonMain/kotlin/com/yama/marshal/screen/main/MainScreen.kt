package com.yama.marshal.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yama.marshal.tool.stringResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation
import com.yama.marshal.ui.view.YamaScreen

internal class MainScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "main"

    @Composable
    override fun title(): String = stringResource("app_name")

    @Composable
    override fun actions() {
        IconButton(
            modifier = Modifier.size(Sizes.buttonSize),
            onClick = {
                //TODO(Open search)
            }) {
            Icon(
                modifier = Modifier.size(Sizes.button_icon_size),
                imageVector = Icons.Default.Search, contentDescription = null
            )
        }

        if (currentOrientation() == Orientation.LANDSCAPE) {
            Spacer(modifier = Modifier.fillMaxHeight().width(0.5.dp).background(Color.LightGray))

            Box(
                modifier = Modifier.width(Sizes.tablet_main_screen_navigation_item_width),
                contentAlignment = Alignment.TopEnd
            ) {
                val clock by remember { viewModel.clock }.collectAsState("")

                Text(clock, fontSize = Sizes.title)
            }
        }
    }

    override val viewModel: MainViewModel = MainViewModel()

    @Composable
    override fun content(args: List<NavArg>) {
        val mContext = @Composable { modifier: Modifier ->
            Box(modifier = modifier)

            NavigationBar()
        }

        if (currentOrientation() == Orientation.LANDSCAPE)
            Row(modifier = Modifier.fillMaxSize()) {
                mContext(Modifier.weight(1f))
            }
        else
            Column(modifier = Modifier.fillMaxSize()) {
                mContext(Modifier.weight(1f))
            }
    }

    @Composable
    private fun NavigationBar() {
        val menuContext = @Composable { modifier: Modifier ->
            var currentIem by remember {
                mutableStateOf(1)
            }

            NavigationItem(modifier, YamaColor.fleet_navigation_card_bg, "fleet", currentIem == 1) {
                currentIem = 1
            }
            NavigationItem(modifier, YamaColor.hole_navigation_card_bg, "holes", currentIem == 2) {
                currentIem = 2
            }
            NavigationItem(
                modifier,
                YamaColor.alert_navigation_card_bg,
                "alerts",
                currentIem == 3
            ) {
                currentIem = 3
            }
        }

        if (currentOrientation() == Orientation.LANDSCAPE)
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.End
            ) {
                menuContext(
                    Modifier
                        .height(Sizes.tablet_main_screen_navigation_item_width)
                        .defaultMinSize(minWidth = Sizes.tablet_main_screen_navigation_item_width)
                )
            }
        else
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                menuContext(Modifier.weight(1f))
            }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun NavigationItem(
        modifier: Modifier,
        backgroundColor: Color,
        label: String,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        if (currentOrientation() == Orientation.LANDSCAPE)
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                onClick = onClick
            ) {
                Row {
                    AnimatedVisibility(
                        visible = isSelected,
                    ) {
                        Spacer(modifier = Modifier.width(Sizes.screenPadding))
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(Sizes.tablet_main_screen_navigation_item_width),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(50.dp),
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background
                        )

                        Spacer(modifier = Modifier.height(Sizes.screenPadding / 2))

                        Text(label.uppercase(), color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        else
            Column(
                modifier = modifier.background(backgroundColor).clickable(true, onClick = onClick),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                AnimatedVisibility(
                    visible = isSelected,
                ) {
                    Spacer(modifier = Modifier.height(Sizes.screenPadding))
                }
                Text(
                    label.uppercase(),
                    modifier = Modifier.padding(Sizes.screenPadding),
                    color = MaterialTheme.colorScheme.background
                )
            }
    }

    override val isToolbarEnable: Boolean = true
}