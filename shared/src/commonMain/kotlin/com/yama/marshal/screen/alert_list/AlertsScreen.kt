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
    override fun toolbarColor(): Color {
        return YamaColor.alert_navigation_card_bg
    }

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
            customItemBgColor = {
                val cart = it.cart

                if (cart.isCartInShutdownMode)
                    YamaColor.cart_shut_down_bg
                else if (cart.isFlag)
                    YamaColor.item_cart_flag_container_bg
                else
                    null
            }
        )
    }

    @Composable
    fun RowScope.ItemViewHolder(item: AlertModel) {
        Text(
            text = item.cart.cartName,
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
                Text(
                    text = item.geofence.name ?: "---",
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
                text = item.cart.currPosHole.let {
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