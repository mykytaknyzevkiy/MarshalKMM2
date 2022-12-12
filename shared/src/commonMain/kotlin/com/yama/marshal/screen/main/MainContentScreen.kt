package com.yama.marshal.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.MarshalList
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.StateFlow

internal abstract class MainContentScreen<SORT_TYPE : SortType, ITEM>(
    navigationController: NavigationController,
    override val viewModel: MainViewModel
) : YamaScreen(navigationController) {

    protected abstract val currentSortState: StateFlow<SORT_TYPE>
    protected abstract val sortList: Array<SORT_TYPE>

    protected abstract val itemList: List<ITEM>

    private fun updateSort(type: SORT_TYPE)  {
        when (type) {
            is SortType.SortFleet -> viewModel.updateFleetSort(type)
            is SortType.SortHole -> viewModel.updateHoleSort(type)
        }
    }

    @Composable
    protected abstract fun ItemViewHolder(item: ITEM, position: Int)

    @Composable
    private fun TableRow() {
        val currentSort by currentSortState.collectAsState()

        val textLabel: @Composable RowScope.(type: SORT_TYPE, isLast: Boolean) -> Unit =
            { type, isLast ->
                Box(
                    modifier = Modifier.weight(type.weight).fillMaxHeight().clickable {
                        updateSort(type)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        type.label.uppercase(),
                        color = MaterialTheme.colorScheme.background.copy(alpha = if (currentSort == type) 1f else 0.6f),
                        textAlign = TextAlign.Center,
                    )
                }
                if (!isLast) NSpacer()
            }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.fleet_view_holder_height)
                .background(MaterialTheme.colorScheme.primary)
                .border(width = 1.dp, color = Color.LightGray),
            verticalAlignment = Alignment.CenterVertically
        ) {
            sortList.forEachIndexed { index, sortType ->
                textLabel(sortType, sortList.lastIndex == index)
            }
        }
    }

    @Composable
    override fun content(args: List<NavArg>) = Column(modifier = Modifier.fillMaxSize()) {
        TableRow()

        val selectedCourse by remember(viewModel) {
            viewModel.selectedCourse
        }.collectAsState()

        if (selectedCourse != null)
            MarshalList(
                modifier = Modifier.fillMaxWidth().weight(1f),
                list = itemList.filter { filterByCourse(selectedCourse!!, it) },
                key = {index, item -> keyItem(index, item)},
                itemContent = { item, position ->
                    ItemViewHolder(item, position)
                }
            )
    }

    abstract fun filterByCourse(courseFullDetail: CourseFullDetail, item: ITEM): Boolean

    abstract fun keyItem(index: Int, item: ITEM): Any
}

@Composable
fun NSpacer() {
    Spacer(
        modifier = Modifier.width(1.dp).fillMaxHeight().padding(vertical = Sizes.screenPadding / 2).background(Color.LightGray)
    )
}