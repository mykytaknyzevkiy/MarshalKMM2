package com.yama.marshal.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.screen.fleet_list.FleetListScreen
import com.yama.marshal.screen.hole_list.HoleListScreen
import com.yama.marshal.tool.Strings
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
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

    @Composable
    override fun titleContent() {
        val selectedCourse by remember { viewModel.selectedCourse }.collectAsState()

        if (selectedCourse == null)
            return

        Text(
            modifier = Modifier.clickable { onSelectCourseState.value = true },
            text = selectedCourse!!.courseName,
            fontSize = Sizes.title,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    override fun actions() {
        if (currentOrientation() == Orientation.LANDSCAPE) {
            val clock by remember { viewModel.clock }.collectAsState("")

            Text(clock, fontSize = Sizes.title)
        }
    }

    @Composable
    override fun content(args: List<NavArg>) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    navigationController = mainNavigationController,
                    screens = arrayOf(fleetListScreen, holeListScreen)
                )

                menuNavigation()
            }

            val onSelectCourse by onSelectCourseState.collectAsState()

            if (onSelectCourse) {
                val courses by viewModel.courseList.collectAsState()

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
        }

        LaunchedEffect(viewModel) {
            viewModel.load()
        }
    }

    @Composable
    private fun menuNavigation() = Row(modifier = Modifier.fillMaxWidth()) {
        NavigationItem(
            YamaColor.fleet_navigation_card_bg,
            Strings.main_screen_navigation_item_fleet_label,
            Icons.Default.DirectionsCar,
        ) {
            mainNavigationController.navigateTo(FleetListScreen.ROUTE)
        }

        NavigationItem(
            YamaColor.hole_navigation_card_bg,
            Strings.main_screen_navigation_item_hole_label,
            Icons.Default.GolfCourse,
        ) {
            mainNavigationController.navigateTo(HoleListScreen.ROUTE)
        }

        NavigationItem(
            YamaColor.alert_navigation_card_bg,
            Strings.main_screen_navigation_item_alert_label,
            Icons.Default.Warning,
        ) {}
    }

    @Composable
    private fun RowScope.NavigationItem(
        backgroundColor: Color,
        label: String,
        icon: ImageVector,
        onClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .background(backgroundColor)
                .weight(1f)
                .clickable(true, onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier  = Modifier.height(Sizes.screenPadding / 2))

            Icon(
                imageVector = icon,
                modifier = Modifier.size(Sizes.button_icon_size),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background
            )

            Text(
                label.uppercase(),
                fontSize = LocalAppDimens.current.bodySmall,
                color = MaterialTheme.colorScheme.background
            )

            Spacer(modifier  = Modifier.height(Sizes.screenPadding / 2))
        }
    }

    override val isToolbarEnable: Boolean = true
}