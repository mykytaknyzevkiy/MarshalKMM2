package com.yama.marshal.screen.hole_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
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
            list = holeList
        )
    }

    @Composable
    private fun RowScope.ItemViewHolder(item: CourseFullDetail.HoleData) {
        val selectedCourse by remember(viewModel) { viewModel.selectedCourse }.collectAsState()

        Text(
            text = item.holeNumber.toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(SortType.SortHole.HOLE.weight).clickable {
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
            modifier = Modifier.weight(SortType.SortHole.PACE_OF_PLAY.weight / 2),
            color = PaceValueFormatter.getColor(item.differentialPace)
        )

        NSpacer()

        Text(
            text = PaceValueFormatter.getStringForCurrentPace(item.averagePace),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(SortType.SortHole.PACE_OF_PLAY.weight / 2)
        )
    }
}