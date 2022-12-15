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
import com.yama.marshal.data.entity.CartItem
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.MarshalList
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.StateFlow

internal abstract class MainContentScreen(
    navigationController: NavigationController,
    override val viewModel: MainViewModel
) : YamaScreen(navigationController) {
    @Composable
    protected fun <SORT_TYPE : SortType> TableRow(sortList: Array<SORT_TYPE>,
                                                  currentSort: SORT_TYPE,
                                                  updateSort: (type: SORT_TYPE) -> Unit
    ) {
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
}

@Composable
fun NSpacer() {
    Spacer(
        modifier = Modifier.width(1.dp).fillMaxHeight().padding(vertical = Sizes.screenPadding / 2).background(Color.LightGray)
    )
}