package com.yama.marshal.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.YamaScreen

internal abstract class MainContentScreen(
    navigationController: NavigationController,
    override val viewModel: MainViewModel
) : YamaScreen(navigationController) {
    @Composable
    protected fun <SORT_TYPE : SortType> TableRow(
        sortList: Array<SORT_TYPE>,
        currentSort: SORT_TYPE,
        currentDesc: Boolean,
        updateSort: (type: SORT_TYPE) -> Unit
    ) = Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Sizes.fleet_view_holder_height)
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, Color.LightGray),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dimensions = LocalAppDimens.current

        sortList.forEach { type ->
            Box(
                modifier = Modifier
                    .weight(type.weight)
                    .fillMaxHeight()
                    .padding(Sizes.screenPadding / 2)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            updateSort(type)
                        }
                    }
            ) {
                Text(
                    text = type.label.uppercase(),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = Sizes.screenPadding / 2),
                    color = MaterialTheme
                        .colorScheme
                        .primary
                        .copy(alpha = if (currentSort == type) 1f else 0.6f),
                    textAlign = TextAlign.Center,
                    fontSize = dimensions.bodyMedium
                )

                if (type == currentSort)
                    Icon(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        imageVector = if (currentDesc)
                            Icons.Default.ArrowDropUp
                        else
                            Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
            }

            NSpacer()
        }
    }

    abstract val toolbarColor: Color
}

@Composable
private fun NSpacer() {
    Spacer(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .padding(vertical = Sizes.screenPadding / 2)
            .background(Color.LightGray)
    )
}