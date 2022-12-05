package com.yama.marshal.screen.fleet_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.tool.stringResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.view.YamaScreen

internal class FleetListScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "fleet_list"

    @Composable
    override fun title(): String = stringResource("fleet_list_screen_title")

    override val viewModel: FleetListViewModel = FleetListViewModel()

    @Composable
    override fun content(args: List<NavArg>) = Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .height((Sizes.screenPadding.value * 2 + LocalAppDimens.current.bodyLarge.value).dp)
                    .background(YamaColor.fleet_navigation_card_bg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val textLabel: @Composable RowScope.(label: String, weight: Float, isLast: Boolean) -> Unit =
                    { label, weight, isLast ->
                        Text(
                            label.uppercase(),
                            modifier = Modifier.weight(weight),
                            color = MaterialTheme.colorScheme.background,
                            textAlign = TextAlign.Center
                        )
                        if (!isLast)
                            Spacer(
                                modifier = Modifier.width(1.dp).fillMaxHeight()
                                    .background(Color.LightGray)
                            )
                    }

                textLabel("Car", 0.8f, false)

                textLabel("Start time", 0.7f, false)

                textLabel("Place of play", 1f, false)

                textLabel("Hole", 0.5f, true)
            }
        }
}