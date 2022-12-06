package com.yama.marshal.screen.fleet_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.tool.format
import com.yama.marshal.tool.stringResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.YamaScreen

internal class FleetListScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "fleet_list"

    override val viewModel: FleetListViewModel = FleetListViewModel()

    @Composable
    override fun titleContent() {
        val selectedCourse by remember { viewModel.selectedCourse }.collectAsState()

        if (selectedCourse == null)
            return

        Text(
            modifier = Modifier.padding(horizontal = Sizes.screenPadding),
            text = selectedCourse!!.courseName,
            fontSize = Sizes.title,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    override fun content(args: List<NavArg>) = Column(modifier = Modifier.fillMaxSize()) {
        TableRow()

        val fleets by remember { viewModel.fleetList }.collectAsState()

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(fleets) {
                FleetViewHolder(it)

                Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
            }
        }

        LaunchedEffect(viewModel) {
            viewModel.load()
        }
    }

    @Composable
    private fun TableRow() = Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        val currentSortFleet by remember { viewModel.currentFleetSort }.collectAsState()

        val textLabel: @Composable RowScope.(type: SortFleet, isLast: Boolean) -> Unit =
            { type, isLast ->
                Box(
                    modifier = Modifier.weight(type.weight).fillMaxHeight().clickable {
                        viewModel.updateSort(type)
                    },
                    contentAlignment = Alignment.Center
                ) {
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

        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.screenPadding + LocalAppDimens.current.bodyLarge.value.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            textLabel(SortFleet.CAR, false)

            textLabel(SortFleet.START_TIME, false)

            textLabel(SortFleet.PLACE_OF_PLAY, false)

            textLabel(SortFleet.HOLE, true)
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
    }

    @Composable
    private fun FleetViewHolder(fleet: CartFullDetail) = Row(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = fleet.cartName,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxHeight().weight(SortFleet.CAR.weight)
        )

        NSpacer()

        Text(
            text = fleet.startTime.let {
                it?.format("h:mm a") ?: stringResource("fleet_view_holder_car_no_active")
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxHeight().weight(SortFleet.START_TIME.weight)
        )

        NSpacer()

        Text(
            text = fleet.startTime.let {
                it?.format("h:mm a") ?: stringResource("fleet_view_holder_car_no_active")
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxHeight().weight(SortFleet.PLACE_OF_PLAY.weight)
        )

        NSpacer()

        Text(
            text = "2",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxHeight().weight(SortFleet.HOLE.weight)
        )

        NSpacer()
    }

    @Composable
    private fun NSpacer() {
        Spacer(
            modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.LightGray)
        )
    }
}