package com.yama.marshal.screen.fleet_list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.yama.marshal.tool.PaceValueFormatter
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.format
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation
import com.yama.marshal.ui.view.MarshalList
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.MutableStateFlow

internal class FleetListScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "fleet_list"

    override val viewModel: FleetListViewModel = FleetListViewModel()

    private val onSelectCourseState = MutableStateFlow(false)

    @Composable
    override fun titleContent() {
        val selectedCourse by remember { viewModel.selectedCourse }.collectAsState()

        if (selectedCourse == null)
            return

        Text(
            modifier = Modifier.padding(Sizes.screenPadding)
                .clickable { onSelectCourseState.value = true },
            text = selectedCourse!!.courseName,
            fontSize = Sizes.title,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    override fun content(args: List<NavArg>) = Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TableRow()

            val fleets by remember { viewModel.fleetList }.collectAsState()

            MarshalList(modifier = Modifier.fillMaxWidth(), list = fleets) {
                FleetViewHolder(fleet = it)
            }

            LaunchedEffect(viewModel) {
                viewModel.load()
            }
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
                            }, contentAlignment = Alignment.Center
                    ) {
                        Text(it.courseName, modifier = Modifier.padding(Sizes.screenPadding))
                    }
                }
            }
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
                        type.label.uppercase(),
                        color = MaterialTheme.colorScheme.background.copy(alpha = if (currentSortFleet == type) 1f else 0.6f),
                        textAlign = TextAlign.Center,
                    )
                }
                if (!isLast) NSpacer()
            }

        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.fleet_view_holder_height),
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
    private fun FleetViewHolder(fleet: CartFullDetail) = Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = fleet.cartName,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(SortFleet.CAR.weight)
            )

            NSpacer()

            Text(
                text = if ((fleet.state != CartFullDetail.State.inUse || fleet.isOnClubHouse) && fleet.returnAreaSts != 0) {
                    if (fleet.isOnClubHouse)
                        Strings.cart_not_in_use_ended_round
                    else
                        Strings.cart_not_in_use
                } else fleet.startTime.let {
                    it?.format("h:mm a") ?: Strings.fleet_view_holder_car_no_active
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(SortFleet.START_TIME.weight)
            )

            NSpacer()

            Text(
                text = if (fleet.startTime == null || fleet.totalNetPace == null) "---" else PaceValueFormatter.getString(
                    fleet.totalNetPace,
                    PaceValueFormatter.PaceType.Short
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(SortFleet.PLACE_OF_PLAY.weight),
                color = PaceValueFormatter.getColor(
                    fleet.totalNetPace ?: 0
                )
            )

            NSpacer()

            Text(
                text = fleet.returnAreaSts.let { returnAreaSts ->
                    if (returnAreaSts == 0)
                        if (fleet.startTime == null || fleet.currPosHole == null || fleet.currPosHole == -1) "---" else fleet.currPosHole.toString()
                    else
                        if (fleet.isOnClubHouse)
                            Strings.clubhouse
                        else
                            "---"
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(SortFleet.HOLE.weight)
            )

            NSpacer()
        }

        if (fleet.isCartInShutdownMode)
            Box(modifier = Modifier
                .fillMaxSize()
                .background(YamaColor.cart_shut_down_bg.copy(alpha = 0.5f))
            )
    }

    @Composable
    private fun NSpacer() {
        Spacer(
            modifier = Modifier.width(1.dp).fillMaxHeight().padding(vertical = Sizes.screenPadding / 2).background(Color.LightGray)
        )
    }
}