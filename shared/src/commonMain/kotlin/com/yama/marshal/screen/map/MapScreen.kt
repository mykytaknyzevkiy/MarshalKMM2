package com.yama.marshal.screen.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.tool.PaceValueFormatter
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.mapList
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.navigation.findInt
import com.yama.marshal.ui.navigation.findString
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.Cart
import com.yama.marshal.ui.view.IGoldMap
import com.yama.marshal.ui.view.RenderData
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map

internal class MapScreen(navigationController: NavigationController) : YamaScreen(navigationController) {
    companion object {
        const val ARG_HOLE_ID = "hole_id"
        const val ARG_CART_ID = "cart_id"
        const val ARG_COURSE_ID = "course_id"

        const val route = "map"
    }

    override val route: String = MapScreen.route

    override val viewModel: MapViewModel = MapViewModel()

    @Composable
    override fun titleContent() {
        val hole by remember(viewModel) {
            viewModel.holeState
        }.collectAsState()

        val carts by remember(viewModel) {
            viewModel.cartsState
        }.collectAsState()

        Row(modifier = Modifier.fillMaxWidth()) {
            if (carts.size == 1) {
                val cart = carts.first()
                Text(
                    modifier = Modifier.padding(horizontal = Sizes.screenPadding).weight(1f),
                    text = "Cart: ${cart.cartName}",
                    fontSize = Sizes.title,
                    textAlign = TextAlign.Center
                )
            }

            if (hole != null)
                Text(
                    modifier = Modifier.padding(horizontal = Sizes.screenPadding).weight(1f),
                    text = "Hole: ${hole?.holeNumber}",
                    fontSize = Sizes.title,
                    textAlign = TextAlign.Center
                )
        }
    }

    @Composable
    override fun content(args: List<NavArg>) = Box(modifier = Modifier.fillMaxSize()) {
        val holeID = args.findInt(ARG_HOLE_ID)
        val courseID = args.findString(ARG_COURSE_ID) ?: return
        val cartID = args.findInt(ARG_CART_ID)

        val course by remember(viewModel) {
            viewModel.courseState
        }.collectAsState()

        val carts by remember(viewModel) {
            viewModel.cartsState
        }.collectAsState()

        val hole by remember(viewModel) {
            viewModel.holeState
        }.collectAsState()

        if (course == null)
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        else {
            IGoldMap(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                renderData = RenderData(
                    idCourse = courseID,
                    vectors = course!!.vectors
                ),
                hole = viewModel
                    .holeState
                    .map { hole ->
                        hole?.holeNumber ?: -1
                    },
                carts = viewModel
                    .cartsState
                    .mapList {
                        Cart(
                            id = it.id,
                            name = it.cartName,
                            location = Pair(it.currPosLat ?: 0.0, it.currPosLon ?: 0.0)
                        )
                    }
            )

            if (carts.size == 1 && cartID != null) {
                val cart = carts.first()
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(Sizes.screenPadding * 3)
                    .align(Alignment.BottomCenter)
                    .background(
                        if (cart.isCartInShutdownMode)
                            YamaColor.shutdown_cart_btn_bg_color
                        else
                            PaceValueFormatter.getColor(cart.totalNetPace ?: 0)
                    ),
                    contentAlignment = Alignment.Center)
                {
                    Text(if (cart.isCartInShutdownMode)
                        Strings.map_screen_cart_in_shut_down_label
                    else PaceValueFormatter.getString(
                        cart.totalNetPace ?: 0,
                        PaceValueFormatter.PaceType.Short
                    ), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            else if (hole != null) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(Sizes.screenPadding * 3)
                    .align(Alignment.BottomCenter)
                    .background(PaceValueFormatter.getColor(hole!!.differentialPace)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        text = PaceValueFormatter.getStringForCurrentPace(hole!!.averagePace),
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier
                        .fillMaxHeight()
                        .size(1.dp)
                        .background(Color.LightGray)
                        .padding(vertical = Sizes.screenPadding / 2)
                    )

                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        text = PaceValueFormatter.getString(
                            hole!!.differentialPace,
                            PaceValueFormatter.PaceType.Short
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        LaunchedEffect(viewModel) {
            viewModel.loadCourse(courseID)

            if (holeID != null)
                viewModel.loadHole(holeID, courseID)
            else if (cartID != null)
                viewModel.loadCart(cartID)

            viewModel
                .cartsLocationUpdater
                .launchIn(this)
        }
    }

    override val isToolbarEnable: Boolean = true
}