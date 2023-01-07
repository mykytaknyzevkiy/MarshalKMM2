package com.yama.marshal.screen.hole_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.screen.main.MainContentScreen
import com.yama.marshal.screen.main.MainViewModel
import com.yama.marshal.screen.main.SortType
import com.yama.marshal.screen.map.MapScreen
import com.yama.marshal.tool.PaceValueFormatter
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.MarshalItemDivider
import com.yama.marshal.ui.view.MarshalItemText
import com.yama.marshal.ui.view.PlatformList

internal class HoleListScreen(navigationController: NavigationController, viewModel: MainViewModel) :
    MainContentScreen(navigationController, viewModel)  {
    companion object {
        const val ROUTE = "hole_list"
    }

    override val route: String = ROUTE

    override val toolbarColor = YamaColor.hole_navigation_card_bg

    @Composable
    override fun content(args: List<NavArg>) = Column(modifier = Modifier.fillMaxSize()) {
        val currentSort by remember(viewModel) { viewModel.currentHoleSort }.collectAsState()

        TableRow(
            sortList = SortType.SortHole.values(),
            currentSort = currentSort.first,
            currentDesc = currentSort.second
        ) {
            viewModel.updateSort(it)
        }

        val itemList = remember(viewModel) {
            viewModel.holeList
        }.collectAsState(emptyList())

        PlatformList(
            listItem = itemList,
            itemContent = {
                ItemViewHolder(it)
            },
            itemActionsCount = {
                1
            },
            itemActions = { item ->
                IconButton(
                    modifier = Modifier.size(Sizes.fleet_view_holder_height).background(
                        YamaColor.view_cart_btn_bg_color),
                    onClick = {
                        navigationController.navigateTo(
                            MapScreen.route,
                            listOf(
                                NavArg(key = MapScreen.ARG_HOLE_ID, value = item.holeNumber),
                                NavArg(key = MapScreen.ARG_COURSE_ID, value = item.idCourse)
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            },
            key = { _, item ->
                "${item.idCourse}_${item.holeNumber}"
            }
        )
    }

    @Composable
    private fun RowScope.ItemViewHolder(item: CourseFullDetail.HoleData) {
        MarshalItemText(
            text = item.holeNumber.toString(),
            weight = SortType.SortHole.HOLE.weight
        )

        MarshalItemDivider()

        MarshalItemText(
            text = PaceValueFormatter.getString(item.differentialPace, PaceValueFormatter.PaceType.Full),
            weight = SortType.SortHole.PACE_OF_PLAY.weight / 2,
            color = PaceValueFormatter.getColor(item.differentialPace)
        )

        MarshalItemDivider()

        MarshalItemText(
            text = PaceValueFormatter.getStringForCurrentPace(item.averagePace),
            weight = SortType.SortHole.PACE_OF_PLAY.weight / 2
        )
    }
}