package com.yama.marshal.screen.alert_list

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.data.model.AlertModel
import com.yama.marshal.screen.main.MainContentScreen
import com.yama.marshal.screen.main.MainViewModel
import com.yama.marshal.tool.PaceValueFormatter
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.format
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.MarshalItemDivider
import com.yama.marshal.ui.view.MarshalItemText
import com.yama.marshal.ui.view.MarshalList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class AlertsScreen(
    navigationController: NavigationController,
    viewModel: MainViewModel
) : MainContentScreen(navigationController, viewModel) {
    companion object {
        const val ROUTE = "alerts_list"
    }

    override val route: String = ROUTE

    override val toolbarColor = YamaColor.alert_navigation_card_bg

    @Composable
    override fun content(args: List<NavArg>) = Column {
        val itemList by remember(viewModel) {
            viewModel.alertList
        }.collectAsState(emptyList())

        val listState = rememberLazyListState()

        MarshalList(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 1.dp, color = Color.LightGray),
            state = listState,
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
            },
            key = { position, _ ->
                position
            }
        )
    }

    @Composable
    fun RowScope.ItemViewHolder(item: AlertModel) {
        MarshalItemText(
            text = item.cart.cartName,
            weight = 0.2f
        )

        MarshalItemDivider()

        MarshalItemText(
            text = when (item) {
                is AlertModel.Fence -> Strings.alerts_item_type_fence_title
                is AlertModel.Pace -> Strings.alerts_item_type_pence_title
                is AlertModel.Battery -> Strings.alerts_item_type_battery_title
            },
            weight = 0.5f
        )

        MarshalItemDivider()

        MarshalItemText(
            text = when (item) {
                is AlertModel.Fence -> item.geofence.name ?: "---"
                is AlertModel.Pace -> PaceValueFormatter.getString(item.netPace, PaceValueFormatter.PaceType.Full)
                is AlertModel.Battery -> item.cart.currPosHole.let {
                    if ((it ?: 0) < 0)
                        "---"
                    else
                        "Hole: $it"
                }
            },
            weight = 0.6f
        )

        MarshalItemDivider()

        MarshalItemText(
            text = item.date.format("hh:mm a"),
            weight = 0.4f
        )
    }
}