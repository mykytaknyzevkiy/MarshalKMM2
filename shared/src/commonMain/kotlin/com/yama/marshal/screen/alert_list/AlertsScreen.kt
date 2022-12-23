package com.yama.marshal.screen.alert_list

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.data.model.AlertModel
import com.yama.marshal.screen.main.MainContentScreen
import com.yama.marshal.screen.main.MainViewModel
import com.yama.marshal.screen.main.NSpacer
import com.yama.marshal.tool.PaceValueFormatter
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.format
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.MarshalList

internal class AlertsScreen(
    navigationController: NavigationController,
    viewModel: MainViewModel
) : MainContentScreen(navigationController, viewModel) {
    companion object {
        const val ROUTE = "alerts_list"
    }

    override val route: String = ROUTE

    @Composable
    override fun content(args: List<NavArg>) {
        val itemList by remember(viewModel) {
            viewModel.alertList
        }.collectAsState(emptyList())

        MarshalList(
            modifier = Modifier.fillMaxSize().border(width = 1.dp, color = Color.LightGray),
            list = itemList,
            itemContent = {
                ItemViewHolder(it)
            },
            /*itemActions = { alert ->
                val cart by remember(alert) { alert.cart }
                    .collectAsState(null)

                if (cart == null)
                    emptyList()
                else
                    ArrayList<MarshalListItemAction>().apply {
                        val item = cart ?: return@apply
                        add(
                            MarshalListItemAction(
                                icon = Icons.Default.Place,
                                color = YamaColor.view_cart_btn_bg_color,
                                onClick = {
                                    navigationController.navigateTo(
                                        MapScreen.route,
                                        listOf(NavArg(key = MapScreen.ARG_CART_ID, value = item.id))
                                    )
                                }
                            )
                        )

                        if (!item.isFlag)
                            add(
                                MarshalListItemAction(
                                    icon = Icons.Default.Flag,
                                    color = YamaColor.flag_cart_btn_bg_color,
                                    onClick = {
                                        viewModel.flagCart(item)
                                    }
                                )
                            )

                        if (item.isMessagingAvailable)
                            add(
                                MarshalListItemAction(
                                    icon = Icons.Default.Email,
                                    color = YamaColor.message_cart_btn_bg_color,
                                    onClick = {
                                        viewModel.flagCart(item)
                                    }
                                )
                            )
                    }
            },*/
            customItemBgColor = {
                val cart by remember(it) {
                    it.cart
                }.collectAsState(null)

                if (cart == null)
                    null
                else if (cart!!.isCartInShutdownMode)
                    YamaColor.cart_shut_down_bg
                else if (cart!!.isFlag)
                    YamaColor.item_cart_flag_container_bg
                else
                    null
            }
        )
    }

    @Composable
    fun RowScope.ItemViewHolder(item: AlertModel) {
        val cart by remember(item) {
            item.cart
        }.collectAsState(null)

        Text(
            text = cart?.cartName ?: "---",
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.4f)
        )

        NSpacer()

        Icon(
            imageVector = when (item) {
                is AlertModel.Fence -> Icons.Default.LocationDisabled
                is AlertModel.Pace -> Icons.Default.Schedule
                is AlertModel.Battery -> Icons.Default.BatteryAlert
            },
            contentDescription = null,
            modifier = Modifier
                .weight(0.1f)
        )

        NSpacer()

        Text(
            text = when (item) {
                is AlertModel.Fence -> Strings.alerts_item_type_fence_title
                is AlertModel.Pace -> Strings.alerts_item_type_pence_title
                is AlertModel.Battery -> Strings.alerts_item_type_battery_title
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.3f)
        )

        NSpacer()

        when (item) {
            is AlertModel.Fence -> {
                val geofence by remember(item) {
                    item.geofence
                }.collectAsState(null)

                Text(
                    text = geofence?.name ?: "---",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.6f)
                )
            }
            is AlertModel.Pace -> Text(
                text = PaceValueFormatter.getString(item.netPace, PaceValueFormatter.PaceType.Full),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.6f)
            )
            is AlertModel.Battery -> Text(
                text =cart?.currPosHole.let {
                    if ((it?:0) < 0)
                        "---"
                    else
                        "Hole: $it"
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.6f)
            )
        }

        NSpacer()

        Text(
            text = item.date.format("hh:mm a"),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.2f)
        )
    }
}