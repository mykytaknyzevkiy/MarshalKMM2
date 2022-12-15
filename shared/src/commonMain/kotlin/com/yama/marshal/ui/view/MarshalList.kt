package com.yama.marshal.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toOffset
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation
import kotlin.math.roundToInt

internal data class MarshalListItemAction(
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
internal inline fun <E> MarshalList(
    modifier: Modifier = Modifier,
    list: List<E>,
    orientation: Orientation = currentOrientation(),
    holderHeight: Dp = Sizes.fleet_view_holder_height,
    bgPositive: Color = YamaColor.itemColor(0),
    bgNegative: Color = YamaColor.itemColor(1),
    noinline key: ((position: Int, item: E) -> Any)? = null,
    crossinline customItemBgColor: @DisallowComposableCalls (item: E) -> Color? = { null },
    crossinline itemAction: @DisallowComposableCalls (item: E) -> List<MarshalListItemAction> = { emptyList() },
    crossinline itemContent: @Composable RowScope.(item: E) -> Unit
) {
    val maxItemCount = remember(orientation) {
        if (orientation == Orientation.LANDSCAPE) 8 else 20
    }

    val isDrawBg = remember {
        derivedStateOf {
            list.size < maxItemCount
        }
    }

    LazyColumn(modifier = modifier.let {
        if (isDrawBg.value)
            it.drawBehind {
                repeat(maxItemCount) {
                    drawRect(
                        color = if (it % 2 == 0) bgPositive else bgNegative,
                        topLeft = Offset(x = 0f, y = it * holderHeight.value)
                    )
                }
            }
        else
            it
    }) {
        itemsIndexed(list, key = key) { position, item ->
            val bgColor = remember(position) {
                if (position % 2 == 0)
                    bgPositive
                else
                    bgNegative
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(holderHeight)
                .drawBehind {
                    drawRect(bgColor)
                }
            ) {
                val itemBgColor by remember {
                    derivedStateOf {
                        customItemBgColor(item)
                    }
                }

                val actions by remember {
                    derivedStateOf {
                        itemAction(item)
                    }
                }

                val maxOffset by remember {
                    derivedStateOf {
                        (holderHeight * actions.size).value.toDouble()
                    }
                }

                var offsetX by remember { mutableStateOf(0f) }

                Actions(actions) {
                    offsetX = 0f
                }

                val itemOffset = remember(offsetX) {
                    IntOffset(offsetX.roundToInt(), 0)
                }

                Row(modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            color = itemBgColor ?: bgColor,
                            topLeft = itemOffset.toOffset()
                        )
                    }
                    .offset { itemOffset }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, x ->
                                val original = Offset(offsetX, 0f)
                                val summed = original + Offset(x = x, y = 0f)

                                if (summed.x in 0.0..maxOffset)
                                    offsetX = summed.x
                            },
                            onDragEnd = {
                                if (offsetX < maxOffset / 2f)
                                    offsetX = 0f
                                else if (offsetX > maxOffset / 2f)
                                    offsetX = maxOffset.toFloat()
                            }
                        )
                    }
                    .clickable {
                        offsetX = if (offsetX >= maxOffset)
                            0f
                        else
                            maxOffset.toFloat()
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemContent(item)
                }
            }
        }
    }
}

@Composable
private fun Actions(actions: List<MarshalListItemAction>, onClick: () -> Unit) = Row {
    actions.forEach {
        ActionBtn(color = it.color, icon = it.icon, click = {
            it.onClick()
            onClick()
        })
    }
}

@Composable
private fun ActionBtn(color: Color, icon: ImageVector, click: () -> Unit) = IconButton(
    modifier = Modifier.size(Sizes.fleet_view_holder_height).background(color),
    onClick = click
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.background,
    )
}