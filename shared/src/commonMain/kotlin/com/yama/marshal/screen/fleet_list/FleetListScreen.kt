package com.yama.marshal.screen.fleet_list

import androidx.compose.foundation.background
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
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.tool.stringResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.YamaScreen

internal class FleetListScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "fleet_list"

    @Composable
    override fun title(): String = stringResource("fleet_list_screen_title")

    override val viewModel: FleetListViewModel = FleetListViewModel()

    @Composable
    override fun content(args: List<NavArg>) = Column(modifier = Modifier.fillMaxSize()) {
        TableRow()
    }

    @Composable
    private fun TableRow() = Row(
        modifier = Modifier.fillMaxWidth()
            .height((Sizes.screenPadding.value * 2 + LocalAppDimens.current.bodyLarge.value).dp)
            .background(YamaColor.fleet_navigation_card_bg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val currentSortFleet by remember { viewModel.currentFleetSort }.collectAsState()

        val textLabel: @Composable RowScope.(type: SortFleet, weight: Float, isLast: Boolean) -> Unit =
            { type, weight, isLast ->
                Box(modifier = Modifier.weight(weight).fillMaxHeight().clickable {
                    viewModel.updateSort(type)
                },
                    contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(type.label).uppercase(),
                        color = MaterialTheme.colorScheme.background.copy(alpha = if (currentSortFleet == type) 1f else 0.6f),
                        textAlign = TextAlign.Center,
                    )
                }
                if (!isLast) Spacer(
                    modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.LightGray)
                )
            }

        textLabel(
            SortFleet.CAR,
            0.8f,
            false,
        )

        textLabel(
            SortFleet.START_TIME,
            0.7f,
            false,
        )

        textLabel(
            SortFleet.PLACE_OF_PLAY,
            1f,
            false,
        )

        textLabel(
            SortFleet.HOLE, 0.5f, true
        )
    }
}