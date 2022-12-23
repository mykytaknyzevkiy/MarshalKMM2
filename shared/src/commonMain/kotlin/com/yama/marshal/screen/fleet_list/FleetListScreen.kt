package com.yama.marshal.screen.fleet_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.screen.main.*
import com.yama.marshal.screen.main.MainContentScreen
import com.yama.marshal.screen.map.MapScreen
import com.yama.marshal.screen.send_message.SendMessageScreen
import com.yama.marshal.tool.*
import com.yama.marshal.tool.PaceValueFormatter
import com.yama.marshal.tool.Strings
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.MarshalList

internal class FleetListScreen(
    navigationController: NavigationController,
    override val viewModel: MainViewModel
) : MainContentScreen(navigationController, viewModel) {
    companion object {
        const val ROUTE = "fleet_list"
    }

    override val route: String = ROUTE

    @Composable
    override fun content(args: List<NavArg>) = Column(modifier = Modifier.fillMaxSize()) {
        val selectedCourse by remember(viewModel) {
            viewModel.selectedCourse
        }.collectAsState()

        val currentSort by remember(viewModel) {
            viewModel.currentFleetSort
        }.collectAsState()

        TableRow(
            sortList = remember {
                SortType.SortFleet.values()
            },
            currentSort = currentSort
        ) { viewModel.updateSort(it) }

        val itemList by remember(viewModel) {
            viewModel.fleetList
        }.collectAsState(emptyList())

        MarshalList(
            modifier = Modifier.fillMaxSize(),
            list = itemList,
            key = { _, item -> item.id },
            itemContent = {
                ItemViewHolder(it)
            },
            itemActions = { item ->
                item {
                    IconButton(
                        modifier = Modifier.size(Sizes.fleet_view_holder_height).background(YamaColor.view_cart_btn_bg_color),
                        onClick = {
                            if (item.currPosLat != null && item.currPosLon != null)
                                navigationController.navigateTo(
                                    MapScreen.route,
                                    listOf(
                                        NavArg(key = MapScreen.ARG_CART_ID, value = item.id),
                                        NavArg(key = MapScreen.ARG_COURSE_ID, value = item.course?.id)
                                    )
                                )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background,
                        )
                    }
                }

                item {
                    IconButton(
                        modifier = Modifier.size(Sizes.fleet_view_holder_height).background(YamaColor.flag_cart_btn_bg_color),
                        onClick = {
                            viewModel.flagCart(item)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background,
                        )
                    }
                }

                if (item.isMessagingAvailable)
                    item {
                        IconButton(
                            modifier = Modifier.size(Sizes.fleet_view_holder_height).background(YamaColor.message_cart_btn_bg_color),
                            onClick = {
                                navigationController.navigateTo(
                                    SendMessageScreen.ROUTE,
                                    listOf(
                                        NavArg(key = SendMessageScreen.ARG_CART_ID, value = item.id),
                                    )
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.background,
                            )
                        }
                    }
            },
            customItemBgColor = {
                if (it.isCartInShutdownMode)
                    YamaColor.cart_shut_down_bg
                else if (it.isFlag)
                    YamaColor.item_cart_flag_container_bg
                else
                    null
            }
        )
    }

    @Composable
    fun RowScope.ItemViewHolder(item: CartFullDetail) {
        Text(
            text = item.cartName,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(SortType.SortFleet.CAR.weight)
        )

        NSpacer()

        Text(
            text = if ((item.state != CartFullDetail.State.inUse || item.isOnClubHouse) && item.returnAreaSts != 0) {
                if (item.isOnClubHouse)
                    Strings.cart_not_in_use_ended_round
                else
                    Strings.cart_not_in_use
            } else item.startTime.let {
                it?.format("h:mm a") ?: Strings.cart_not_in_use
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(SortType.SortFleet.START_TIME.weight)
        )

        NSpacer()

        Text(
            text = if (item.startTime == null || item.totalNetPace == null) "---" else PaceValueFormatter.getString(
                item.totalNetPace,
                PaceValueFormatter.PaceType.Short
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(SortType.SortFleet.PLACE_OF_PLAY.weight),
            color = PaceValueFormatter.getColor(
                item.totalNetPace ?: 0
            )
        )

        NSpacer()

        Text(
            text = item.returnAreaSts.let { returnAreaSts ->
                if (returnAreaSts == 0)
                    if (item.startTime == null || item.currPosHole == null || item.currPosHole == -1) "---" else item.currPosHole.toString()
                else
                    if (item.isOnClubHouse)
                        Strings.clubhouse
                    else
                        "---"
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(SortType.SortFleet.HOLE.weight)
        )

        NSpacer()
    }
}