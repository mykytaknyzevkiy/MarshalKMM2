package com.yama.marshal.screen.hole_list

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.yama.marshal.data.entity.HoleEntity
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.screen.main.MainContentScreen
import com.yama.marshal.screen.main.MainViewModel
import com.yama.marshal.screen.main.NSpacer
import com.yama.marshal.screen.main.SortType
import com.yama.marshal.tool.HoleSorter
import com.yama.marshal.tool.PaceValueFormatter
import com.yama.marshal.ui.navigation.NavigationController
import kotlinx.coroutines.flow.StateFlow

internal class HoleListScreen(navigationController: NavigationController, viewModel: MainViewModel) :
    MainContentScreen<SortType.SortHole, HoleEntity>(navigationController, viewModel) {
    companion object {
        const val ROUTE = "hole_list"
    }

    override val currentSortState: StateFlow<SortType.SortHole>
        get() = viewModel.currentHoleSort

    override val sortList: Array<SortType.SortHole> = SortType.SortHole.values()

    override val itemList: List<HoleEntity>
        get() = viewModel.holeList

    @Composable
    override fun ItemViewHolder(item: HoleEntity, position: Int) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.id.toString(),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(SortType.SortHole.HOLE.weight)
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

    override fun filterByCourse(courseFullDetail: CourseFullDetail, item: HoleEntity): Boolean =
        courseFullDetail.id.isNullOrBlank() || item.idCourse == courseFullDetail.id

    override fun keyItem(index: Int, item: HoleEntity): Any =  item.id

    override val route: String = ROUTE

    override fun sorter(type: SortType.SortHole): Comparator<HoleEntity> = HoleSorter(type)
}