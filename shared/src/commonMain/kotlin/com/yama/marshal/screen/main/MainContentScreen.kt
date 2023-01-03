package com.yama.marshal.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.YamaScreen

internal abstract class MainContentScreen(
    navigationController: NavigationController,
    override val viewModel: MainViewModel
) : YamaScreen(navigationController) {
    @Composable
    protected fun <SORT_TYPE : SortType> TableRow(sortList: Array<SORT_TYPE>,
                                                  currentSort: SORT_TYPE?,
                                                  updateSort: (type: SORT_TYPE) -> Unit
    ) = Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.fleet_view_holder_height)
                .background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            sortList.forEach { sortType ->
                textLabel(sortType, currentSort, updateSort)
            }
        }

        Spacer(modifier = Modifier.fillMaxWidth().background(Color.LightGray).height(1.dp))
    }

    @Composable
    private fun <SORT_TYPE : SortType>  RowScope.textLabel(type: SORT_TYPE,
                                                           currentSort: SORT_TYPE?,
                                                           updateSort: (type: SORT_TYPE) -> Unit
    ) {
        Box(modifier = Modifier
            .weight(type.weight)
            .fillMaxHeight()
            .padding(Sizes.screenPadding / 2)
            .clickable { updateSort(type) }, contentAlignment = Alignment.Center) {
            Text(
                text = type.label.uppercase(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (currentSort == type) 1f else 0.6f),
                textAlign = TextAlign.Center,
            )
        }

        NSpacer()
    }

    abstract val toolbarColor: Color
}

@Composable
internal fun NSpacer() {
    Spacer(
        modifier = Modifier.width(1.dp).fillMaxHeight().padding(vertical = Sizes.screenPadding / 2).background(Color.LightGray)
    )
}