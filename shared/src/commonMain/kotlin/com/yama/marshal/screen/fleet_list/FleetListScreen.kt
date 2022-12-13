package com.yama.marshal.screen.fleet_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
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
import io.ktor.util.date.*
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt

internal class FleetListScreen(navigationController: NavigationController,
                               override val viewModel: MainViewModel) :
    MainContentScreen<SortType.SortFleet, CartFullDetail>(navigationController, viewModel) {
    companion object {
        const val ROUTE = "fleet_list"
    }

    override val route: String = ROUTE

    override val currentSortState: StateFlow<SortType.SortFleet>
        get() = viewModel.currentFleetSort

    override val sortList: Array<SortType.SortFleet> = SortType.SortFleet.values()

    override val itemList: List<CartFullDetail>
        get() = viewModel.fleetList

    @Composable
    override fun ItemViewHolder(item: CartFullDetail, position: Int) {
        var maxOffset = 0f

        var offsetX by remember { mutableStateOf(0f) }

        Row {
            maxOffset = 0f

            val btn: @Composable (Color, String, ImageVector, () -> Unit) -> Unit = { color, label, icon, onCLick ->
                IconButton(
                    modifier = Modifier
                        .size(Sizes.fleet_view_holder_height)
                        .background(color),
                    onClick = {
                        offsetX = 0f
                        onCLick()
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background,
                    )
                    /*Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background,
                        )
                        if (orientation == Orientation.LANDSCAPE)
                            Text(
                                label.uppercase(),
                                modifier = Modifier.padding(horizontal = Sizes.screenPadding / 2),
                                color = MaterialTheme.colorScheme.background,
                                textAlign = TextAlign.Center,
                                fontSize = LocalAppDimens.current.bodySmall
                            )
                    }*/
                }

                maxOffset += Sizes.fleet_view_holder_height.value
            }

            btn(
                YamaColor.view_cart_btn_bg_color,
                Strings.fleet_view_holder_action_view_cart_btn_label,
                Icons.Default.Place
            ) {
                navigationController.navigateTo(
                    MapScreen.route, listOf(NavArg(key = MapScreen.ARG_CART_ID, value = item.id))
                )
            }

            if (!item.isFlag)
                btn(
                    YamaColor.flag_cart_btn_bg_color,
                    Strings.fleet_view_holder_action_flag_cart_btn_label,
                    Icons.Default.Flag
                ) {
                    viewModel.flagCart(item)
                }

            if (item.isMessagingAvailable)
                btn(
                    YamaColor.message_cart_btn_bg_color,
                    Strings.fleet_view_holder_action_message_btn_label,
                    Icons.Default.Email
                ) {
                    navigationController.navigateTo(SendMessageScreen.ROUTE, arrayListOf(
                        NavArg(key = SendMessageScreen.ARG_CART_ID, item.id)
                    ).apply {
                        item.course?.id?.also {
                            this.add(NavArg(key = SendMessageScreen.ARG_COURSE_ID, it))
                        }
                    })
                }

            if (item.isShutdownEnable)
                btn(
                    YamaColor.shutdown_cart_btn_bg_color,
                    Strings.fleet_view_holder_action_shutdown_btn_label,
                    Icons.Default.PowerSettingsNew
                ) {}

            if (item.isCartInShutdownMode && item.isShutdownEnable)
                btn(
                    YamaColor.restore_cart_btn_bg_color,
                    Strings.fleet_view_holder_action_restore_btn_label,
                    Icons.Default.PowerSettingsNew
                ) {}
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, x ->
                        val original = Offset(offsetX, 0f)
                        val summed = original + Offset(x = x, y = 0f)

                        if (summed.x in 0.0..maxOffset.toDouble())
                            offsetX = summed.x
                    },
                    onDragEnd = {
                        if (offsetX < maxOffset / 2f)
                            offsetX = 0f
                        else if (offsetX > maxOffset / 2f)
                            offsetX = maxOffset
                    }
                )
            }
        ) {
            Row(modifier = Modifier
                .background(
                    if (item.isCartInShutdownMode)
                        YamaColor.cart_shut_down_bg
                    else if (item.isFlag)
                        YamaColor.item_cart_flag_container_bg
                    else
                        YamaColor.itemColor(position)
                )
                .clickable { offsetX = if (offsetX < maxOffset) maxOffset else 0f },
                verticalAlignment = Alignment.CenterVertically
            ) {
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
    }

    override fun filterByCourse(courseFullDetail: CourseFullDetail, item: CartFullDetail): Boolean =
        courseFullDetail.id.isNullOrBlank() || item.course?.id == courseFullDetail.id

    override fun keyItem(index: Int, item: CartFullDetail): Any = item.id

    override fun sorter(type: SortType.SortFleet): Comparator<CartFullDetail> = FleetSorter(type)

    override fun nFilter(it: CartFullDetail): Boolean = it.lastActivity != null
            && !it.lastActivity.isBeforeDate(GMTDate())
}