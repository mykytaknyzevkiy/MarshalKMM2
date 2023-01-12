package com.yama.marshal.screen.fleet_list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.screen.main.MainContentScreen
import com.yama.marshal.screen.main.MainViewModel
import com.yama.marshal.screen.main.SortType
import com.yama.marshal.screen.map.MapScreen
import com.yama.marshal.screen.send_message.SendMessageScreen
import com.yama.marshal.tool.PaceValueFormatter
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.format
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.MarshalItemDivider
import com.yama.marshal.ui.view.MarshalItemText
import com.yama.marshal.ui.view.PlatformList

internal class FleetListScreen(
    navigationController: NavigationController, override val viewModel: MainViewModel
) : MainContentScreen(navigationController, viewModel) {
    companion object {
        const val ROUTE = "fleet_list"
    }

    override val route: String = ROUTE

    override val toolbarColor = YamaColor.fleet_navigation_card_bg

    @Composable
    override fun content(args: List<NavArg>) = Column(modifier = Modifier.fillMaxSize()) {
        Row()

        PlatformList(
            listItem = remember(viewModel) {
                viewModel.fleetList
            }.collect(),
            itemContent = {
                ItemViewHolder(it)
            },
            itemActions = { item ->
                if (item.currPosLat != null
                    && item.currPosLon != null
                    && item.currPosHole != null
                    && item.currPosHole > 0
                )
                    IconButton(modifier = Modifier.size(Sizes.fleet_view_holder_height)
                        .background(YamaColor.view_cart_btn_bg_color), onClick = {
                        navigationController.navigateTo(
                            MapScreen.route, listOf(
                                NavArg(key = MapScreen.ARG_CART_ID, value = item.id),
                                NavArg(key = MapScreen.ARG_COURSE_ID, value = item.course?.id)
                            )
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }

                if (item.isFlag)
                    IconButton(modifier = Modifier.size(Sizes.fleet_view_holder_height)
                        .background(YamaColor.flag_cart_btn_bg_color), onClick = {
                        viewModel.unFlagCart(item)
                    }) {
                        val screenPadding = Sizes.screenPadding
                        val tintColor = MaterialTheme.colorScheme.onPrimary
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = tintColor,
                        )
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val padding = screenPadding.toPx()
                            drawLine(
                                color = tintColor, start = Offset(padding, padding), end = Offset(
                                    this.size.width - padding, this.size.height - padding
                                ), strokeWidth = 8f
                            )
                        }
                    }
                else
                    IconButton(modifier = Modifier.size(Sizes.fleet_view_holder_height)
                        .background(YamaColor.flag_cart_btn_bg_color), onClick = {
                        viewModel.flagCart(item)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }

                if (item.isMessagingAvailable)
                    IconButton(modifier = Modifier.size(Sizes.fleet_view_holder_height)
                        .background(YamaColor.message_cart_btn_bg_color), onClick = {
                        navigationController.navigateTo(
                            SendMessageScreen.ROUTE, listOf(
                                NavArg(
                                    key = SendMessageScreen.ARG_CART_ID, value = item.id
                                ),
                            )
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
            },
            customItemBgColor = {
                if (it.isCartInShutdownMode) YamaColor.cart_shut_down_bg
                else if (it.isFlag) YamaColor.item_cart_flag_container_bg
                else null
            },
            key = {
                it.id
            }
        )
    }

    @Composable
    private fun Row() {
        val currentSort by remember(viewModel) {
            viewModel.currentFleetSort
        }.collectAsState()

        TableRow(
            sortList = remember {
                SortType.SortFleet.values()
            },
            currentSort = currentSort.first,
            currentDesc = currentSort.second
        ) { viewModel.updateSort(it) }
    }

    @Composable
    fun RowScope.ItemViewHolder(item: CartFullDetail) {
        MarshalItemText(
            text = item.cartName, weight = SortType.SortFleet.CAR.weight
        )

        MarshalItemDivider()

        MarshalItemText(
            text = remember {
                derivedStateOf {
                    if ((item.state != CartFullDetail.State.inUse || item.isOnClubHouse)
                        && item.returnAreaSts != 0
                    ) {
                        if (item.isOnClubHouse) Strings.cart_not_in_use_ended_round
                        else Strings.cart_not_in_use
                    } else item.startTime.let {
                        it?.format("h:mm a") ?: Strings.cart_not_in_use
                    }
                }
            }.value,
            weight = SortType.SortFleet.START_TIME.weight
        )

        MarshalItemDivider()

        MarshalItemText(
            text = remember {
                derivedStateOf {
                    if (item.startTime == null || item.totalNetPace == null) "---"
                    else PaceValueFormatter.getString(
                        item.totalNetPace, PaceValueFormatter.PaceType.Short
                    )
                }
            }.value,
            weight = SortType.SortFleet.PLACE_OF_PLAY.weight,
            color = remember {
                derivedStateOf {
                    PaceValueFormatter.getColor(
                        item.totalNetPace ?: 0
                    )
                }
            }.value
        )

        MarshalItemDivider()

        MarshalItemText(
            text = remember {
                derivedStateOf {
                    item.returnAreaSts.let { returnAreaSts ->
                        if (returnAreaSts == 0) if (item.startTime == null || item.currPosHole == null || item.currPosHole == -1) "---" else item.currPosHole.toString()
                        else if (item.isOnClubHouse) Strings.clubhouse
                        else "---"
                    }
                }
            }.value,
            weight = SortType.SortFleet.HOLE.weight
        )
    }
}