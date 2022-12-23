package com.yama.marshal.ui.view

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.toOffset
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation
import kotlin.math.roundToInt

@Composable
internal inline fun <E> MarshalList(
    modifier: Modifier = Modifier,
    list: List<E>,
    orientation: Orientation = currentOrientation(),
    holderHeight: Dp = Sizes.fleet_view_holder_height,
    bgPositive: Color = YamaColor.itemColor(0),
    bgNegative: Color = YamaColor.itemColor(1),
    noinline key: ((position: Int, item: E) -> Any)? = null,
    crossinline customItemBgColor: @Composable (item: E) -> Color? = { null },
    crossinline itemActions: LazyListScope.(item: E) -> Unit = { },
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

    LazyColumn(modifier = modifier.let { m ->
        if (isDrawBg.value)
            m.drawBehind {
                repeat(maxItemCount) {
                    drawRect(
                        color = if (it % 2 == 0) bgPositive else bgNegative,
                        topLeft = Offset(x = 0f, y = (it * holderHeight).toPx()),
                    )
                }
            }
        else
            m
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
                val itemBgColor = customItemBgColor(item)

                val maxOffset = remember {
                    holderHeight * 3
                }

                var offsetX by remember { mutableStateOf(0f) }

                if (offsetX > 0)
                    LazyRow {
                        itemActions(item)
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

                                if (summed.x in 0.0f..maxOffset.toPx())
                                    offsetX = summed.x
                            },
                            onDragEnd = {
                                if (offsetX < (maxOffset.toPx()) / 2f)
                                    offsetX = 0f
                                else if (offsetX > (maxOffset.toPx()) / 2f)
                                    offsetX = maxOffset.toPx()
                            }
                        )
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemContent(item)
                }
            }
        }
    }
}