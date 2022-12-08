package com.yama.marshal.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.screen.fleet_list.FleetListScreen
import com.yama.marshal.tool.Strings
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation
import com.yama.marshal.ui.view.NavHost
import com.yama.marshal.ui.view.YamaScreen

internal class MainScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "main"

    @Composable
    override fun titleContent() {
        val currentRoute by mainNavigationController.currentRoute.collectAsState()

        if (currentRoute.route == fleetListScreen.route)
            fleetListScreen.titleContent()
        else
            super.titleContent()
    }

    @Composable
    override fun actions() {
        IconButton(
            modifier = Modifier.size(Sizes.buttonSize),
            onClick = {
                //TODO(Open search)
            }) {
            Icon(
                imageVector = Icons.Default.Search, contentDescription = null
            )
        }

        if (currentOrientation() == Orientation.LANDSCAPE) {
            val clock by remember { viewModel.clock }.collectAsState("")

            Text(clock, fontSize = Sizes.title)
        }
    }

    override val viewModel: MainViewModel = MainViewModel()

    private val mainNavigationController = NavigationController("fleet_list")

    private val fleetListScreen = FleetListScreen(mainNavigationController)

    @Composable
    override fun content(args: List<NavArg>) {
        val mContext = @Composable { modifier: Modifier ->
            NavHost(
                modifier = modifier,
                navigationController = mainNavigationController,
                screens = arrayOf(fleetListScreen)
            )
        }

        if (currentOrientation() == Orientation.LANDSCAPE)
            Box(modifier = Modifier.fillMaxSize()) {
                mContext(Modifier.fillMaxSize())

                //Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(Color.LightGray))

                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                    NavigationBar()
                }
            }
        else
            Column(modifier = Modifier.fillMaxSize()) {
                mContext(Modifier.weight(1f).fillMaxWidth())

                NavigationBar()
            }
    }

    @Composable
    private fun NavigationBar() {
        val menuContext = @Composable { modifier: Modifier ->
            var currentIem by remember {
                mutableStateOf(1)
            }

            NavigationItem(
                modifier,
                YamaColor.fleet_navigation_card_bg,
                Strings.main_screen_navigation_item_fleet_label,
                Icons.Default.DirectionsCar,
                currentIem == 1
            ) {
                currentIem = 1
            }

            NavigationItem(
                modifier,
                YamaColor.hole_navigation_card_bg,
                Strings.main_screen_navigation_item_hole_label,
                Icons.Default.GolfCourse,
                currentIem == 2
            ) {
                currentIem = 2
            }

            NavigationItem(
                modifier,
                YamaColor.alert_navigation_card_bg,
                Strings.main_screen_navigation_item_alert_label,
                Icons.Default.Warning,
                currentIem == 3
            ) {
                currentIem = 3
            }
        }

        if (currentOrientation() == Orientation.LANDSCAPE)
            Column(
                //modifier = Modifier.fillMaxHeight(),
                //verticalArrangement = Arrangement.SpaceAround,
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
        icon: ImageVector,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        if (currentOrientation() == Orientation.LANDSCAPE)
            Card(
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                onClick = if (isSelected) {{}} else onClick
            ) {
                Row {
                    AnimatedVisibility(
                        visible = isSelected,
                    ) {
                        Spacer(modifier = Modifier.width(Sizes.screenPadding / 3))
                    }
                    Column(
                        modifier = Modifier.width(Sizes.tablet_main_screen_navigation_item_width),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(Sizes.screenPadding / 2))

                        Icon(
                            modifier = Modifier.size(35.dp),
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background
                        )

                         Spacer(modifier = Modifier.height(Sizes.screenPadding / 2))

                        Text(
                            label.uppercase(),
                            modifier = Modifier.vertical().rotate(90f),
                            color = MaterialTheme.colorScheme.background,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(Sizes.screenPadding / 2))
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
                    Spacer(modifier = Modifier.height(Sizes.screenPadding / 2))
                }
                Text(
                    label.uppercase(),
                    modifier = Modifier.padding(Sizes.screenPadding),
                    color = MaterialTheme.colorScheme.background
                )
            }
    }

    override val isToolbarEnable: Boolean = true

    private fun Modifier.vertical() = layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }
}