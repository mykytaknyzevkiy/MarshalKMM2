package com.yama.marshal.screen.hole_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.screen.main.MainContentScreen
import com.yama.marshal.screen.main.MainViewModel
import com.yama.marshal.screen.main.NSpacer
import com.yama.marshal.screen.main.SortType
import com.yama.marshal.screen.map.MapScreen
import com.yama.marshal.tool.PaceValueFormatter
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.MarshalList

internal class HoleListScreen(navigationController: NavigationController, viewModel: MainViewModel) :
    MainContentScreen(navigationController, viewModel)  {
    companion object {
        const val ROUTE = "hole_list"
    }

    override val route: String = ROUTE

    @Composable
    override fun content(args: List<NavArg>) = Column(modifier = Modifier.fillMaxSize()) {
        val currentSort by remember(viewModel) { viewModel.currentHoleSort }.collectAsState()

        TableRow(
            sortList = SortType.SortHole.values(),
            currentSort = currentSort
        ) {
            viewModel.updateSort(it)
        }

        val holeList by remember(viewModel) { viewModel.holeList }.collectAsState(emptyList())

        MarshalList(
            modifier = Modifier.fillMaxSize(),
            itemContent = {
                ItemViewHolder(it)
            },
            itemActions = { item ->
                item {
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
                }
            },
            list = holeList
        )
    }

    @Composable
    private fun RowScope.ItemViewHolder(item: CourseFullDetail.HoleData) {
        Text(
            text = item.holeNumber.toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(SortType.SortHole.HOLE.weight)
                .padding(Sizes.screenPadding / 2)
                .clickable {
                navigationController.navigateTo(
                    MapScreen.route,
                    listOf(
                        NavArg(key = MapScreen.ARG_HOLE_ID, value = item.holeNumber),
                        NavArg(key = MapScreen.ARG_COURSE_ID, value = item.idCourse)
                    )
                )
            }
        )

        NSpacer()

        Text(
            text = PaceValueFormatter.getString(item.differentialPace, PaceValueFormatter.PaceType.Full),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(SortType.SortHole.PACE_OF_PLAY.weight / 2)
                .padding(Sizes.screenPadding / 2),
            color = PaceValueFormatter.getColor(item.differentialPace)
        )

        NSpacer()

        Text(
            text = PaceValueFormatter.getStringForCurrentPace(item.averagePace),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(SortType.SortHole.PACE_OF_PLAY.weight / 2)
                .padding(Sizes.screenPadding / 2)
        )
    }
}