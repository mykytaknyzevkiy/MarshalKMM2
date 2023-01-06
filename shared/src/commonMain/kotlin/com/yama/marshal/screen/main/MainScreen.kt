package com.yama.marshal.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.screen.alert_list.AlertsScreen
import com.yama.marshal.screen.fleet_list.FleetListScreen
import com.yama.marshal.screen.hole_list.HoleListScreen
import com.yama.marshal.screen.map.MapScreen
import com.yama.marshal.tool.Strings
import com.yama.marshal.ui.alert.CartMessagesAlert
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Dimensions
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation
import com.yama.marshal.ui.view.NavHost
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.MutableStateFlow

internal class MainScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "main"

    private val onSelectCourseState = MutableStateFlow(false)

    override val viewModel: MainViewModel = MainViewModel()

    private val mainNavigationController = NavigationController(FleetListScreen.ROUTE)

    private val fleetListScreen = FleetListScreen(navigationController, viewModel)
    private val holeListScreen = HoleListScreen(navigationController, viewModel)
    private val alertListScreen = AlertsScreen(navigationController, viewModel)

    @Composable
    override fun titleContent() {
        val selectedCourse by remember { viewModel.selectedCourse }.collectAsState()

        if (selectedCourse == null)
            return

        Text(
            modifier = Modifier.clickable { onSelectCourseState.value = true },
            text = selectedCourse!!.courseName,
            fontSize = Sizes.title,
            textAlign = TextAlign.Center,
        )
    }

    @Composable
    override fun actions() {
        if (currentOrientation() == Orientation.LANDSCAPE) {
            val clock by remember { viewModel.clock }.collectAsState("")

            Text(
                modifier = Modifier.padding(horizontal = Sizes.screenPadding),
                text = clock,
                fontSize = Sizes.title
            )
        }

        val fullReloadState by remember(viewModel) {
            viewModel.fullReloadState
        }.collectAsState()

        if (fullReloadState is MainFullReloadState.Empty)
            IconButton(
                modifier = Modifier.size(Sizes.buttonSize),
                onClick = {
                    viewModel.forceReload()
                }) {
                Icon(
                    modifier = Modifier.size(Sizes.button_icon_size),
                    imageVector = Icons.Default.Refresh, contentDescription = null
                )
            }
        else
            CircularProgressIndicator(
                modifier = Modifier.size(Sizes.button_icon_size),
                color = MaterialTheme.colorScheme.onPrimary
            )

        IconButton(
            modifier = Modifier.size(Sizes.buttonSize),
            onClick = {
                viewModel.logOut()
                navigationController.navigateToAndFinish("login")
            }) {
            Icon(
                modifier = Modifier.size(Sizes.button_icon_size),
                imageVector = Icons.Default.Logout, contentDescription = null
            )
        }
    }

    @Composable
    override fun content(args: List<NavArg>) = Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navigationController = mainNavigationController,
            screens = arrayOf(fleetListScreen, holeListScreen, alertListScreen)
        )

        val courses by viewModel.courseList.collectAsState(emptyList())

        val onSelectCourse by onSelectCourseState.collectAsState()

        if (onSelectCourse) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = Sizes.screenPadding)
                    .background(MaterialTheme.colorScheme.background).align(Alignment.TopStart)
            ) {
                items(courses) {
                    Box(
                        modifier = Modifier.width(300.dp)
                            .border(width = 1.dp, color = Color.LightGray).clickable {
                                onSelectCourseState.value = false
                                viewModel.selectCourse(it)
                            }, contentAlignment = Alignment.CenterStart
                    ) {
                        Text(it.courseName, modifier = Modifier.padding(Sizes.screenPadding))
                    }
                }
            }
        }

        val isAnyCartMessage by remember(viewModel.cartMessages) {
            derivedStateOf {
                viewModel.cartMessages.isNotEmpty()
            }
        }

        if (isAnyCartMessage)
            CartMessagesAlert { item ->
                navigationController.navigateTo(
                    MapScreen.route, listOf(
                        NavArg(key = MapScreen.ARG_CART_ID, value = item.id),
                        NavArg(key = MapScreen.ARG_COURSE_ID, value = item.course?.id)
                    )
                )
            }

        LaunchedEffect(Unit) {
            viewModel.load()
        }
    }

    @Composable
    override fun bottomBar() {
        val dimensions = LocalAppDimens.current

        val navColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = Color.White,
            unselectedIconColor = Color.White,
            unselectedTextColor = Color.White
        )

        BottomAppBar(
            modifier = Modifier.let {
                if (dimensions !is Dimensions.Tablet)
                    it.height(60.dp)
                else
                    it
            },
            contentPadding = PaddingValues(0.dp),
        ) {
            val currentRoute by mainNavigationController.currentRoute.collectAsState()

            NavigationBarItem(
                selected = currentRoute.route == FleetListScreen.ROUTE,
                modifier = Modifier.background(YamaColor.fleet_navigation_card_bg),
                icon = {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null
                    )
                },
                label = if (dimensions is Dimensions.Tablet) {
                    {
                        Text(
                            Strings.main_screen_navigation_item_fleet_label.uppercase()
                        )
                    }
                } else null,
                onClick = {
                    mainNavigationController.navigateTo(FleetListScreen.ROUTE)
                },
                colors = navColors,
                alwaysShowLabel = true
            )

            NavigationBarItem(
                selected = currentRoute.route == HoleListScreen.ROUTE,
                modifier = Modifier.background(YamaColor.hole_navigation_card_bg),
                icon = {
                    Icon(
                        imageVector = Icons.Default.GolfCourse,
                        contentDescription = null
                    )
                },
                label = if (dimensions is Dimensions.Tablet) {
                    {
                        Text(
                            Strings.main_screen_navigation_item_hole_label.uppercase()
                        )
                    }
                } else null,
                onClick = {
                    mainNavigationController.navigateTo(HoleListScreen.ROUTE)
                },
                colors = navColors,
                alwaysShowLabel = true
            )

            NavigationBarItem(
                selected = currentRoute.route == AlertsScreen.ROUTE,
                modifier = Modifier.background(YamaColor.alert_navigation_card_bg),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null
                    )
                },
                label = if (dimensions is Dimensions.Tablet) {
                    {
                        Text(
                            Strings.main_screen_navigation_item_alert_label.uppercase()
                        )
                    }
                } else null,
                onClick = {
                    mainNavigationController.navigateTo(AlertsScreen.ROUTE)
                },
                colors = navColors,
                alwaysShowLabel = true
            )
        }
    }

    override val isToolbarEnable: Boolean = true
}