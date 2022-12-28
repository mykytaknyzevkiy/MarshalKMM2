package com.yama.marshal.screen.fleet_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.screen.main.MainContentScreen
import com.yama.marshal.screen.main.MainViewModel
import com.yama.marshal.screen.main.NSpacer
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
import com.yama.marshal.ui.view.MarshalList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class FleetListScreen(
    navigationController: NavigationController,
    override val viewModel: MainViewModel
) : MainContentScreen(navigationController, viewModel) {
    companion object {
        const val ROUTE = "fleet_list"
    }

    override val route: String = ROUTE

    @Composable
    override fun toolbarColor(): Color {
        return YamaColor.fleet_navigation_card_bg
    }

    @Composable
    override fun content(args: List<NavArg>) = Column(modifier = Modifier.fillMaxSize()) {
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

        val listState = rememberLazyListState()

        MarshalList(
            modifier = Modifier.fillMaxSize(),
            list = itemList,
            key = { _, item -> item.id },
            itemContent = {
                ItemViewHolder(it)
            },
            itemActions = { item ->
                if (item.currPosLat != null
                    && item.currPosLon != null
                    && item.currPosHole != null
                    && item.currPosHole > 0
                ) item {
                        IconButton(
                            modifier = Modifier.size(Sizes.fleet_view_holder_height).background(YamaColor.view_cart_btn_bg_color),
                            onClick = {
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
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                if (item.isFlag)
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
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                else
                    item {
                        IconButton(
                            modifier = Modifier.size(Sizes.fleet_view_holder_height).background(YamaColor.item_cart_flag_container_bg),
                            onClick = {
                                viewModel.unFlagCart(item)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
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
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                if (item.isShutdownEnable)
                    item {
                        IconButton(
                            modifier = Modifier
                                .size(Sizes.fleet_view_holder_height)
                                .background(YamaColor.shutdown_cart_btn_bg_color),
                            onClick = {
                                viewModel.shutDown(item.id)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                if (item.isCartInShutdownMode)
                    item {
                        IconButton(
                            modifier = Modifier
                                .size(Sizes.fleet_view_holder_height)
                                .background(YamaColor.restore_cart_btn_bg_color),
                            onClick = {
                                viewModel.restore(item.id)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Power,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
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

        LaunchedEffect(viewModel) {
            viewModel.currentFleetSort
                .onEach {
                    listState.scrollToItem(0)
                }
                .launchIn(this)
        }
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