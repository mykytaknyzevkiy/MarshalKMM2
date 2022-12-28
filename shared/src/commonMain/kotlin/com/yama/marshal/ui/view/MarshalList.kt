package com.yama.marshal.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlignVerticalTop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.toOffset
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal inline fun <E> MarshalList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    list: List<E>,
    holderHeight: Dp = Sizes.fleet_view_holder_height,
    bgPositive: Color = YamaColor.itemColor(0),
    bgNegative: Color = YamaColor.itemColor(1),
    noinline key: ((position: Int, item: E) -> Any)? = null,
    crossinline customItemBgColor: @DisallowComposableCalls (item: E) -> Color? = { null },
    crossinline itemActions: LazyListScope.(item: E) -> Unit = { },
    crossinline itemContent: @Composable RowScope.(item: E) -> Unit
) = Box(modifier = modifier.drawBehind {
    repeat(30) {
        try {
            drawRect(
                color = if (it % 2 == 0) bgPositive else bgNegative,
                topLeft = Offset(x = 0f, y = (it * holderHeight).toPx()),
            )
        } catch (e: Exception) {
            return@repeat
        }
    }
}) {
    val scope = rememberCoroutineScope()

    LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
        itemsIndexed(list, key = key) { position, item ->
            Box(modifier = Modifier.fillMaxWidth().height(holderHeight)) {
                val itemBgColor = remember(position, item) {
                    customItemBgColor(item)
                        ?: if (position % 2 == 0)
                            bgPositive
                        else
                            bgNegative
                }

                var maxOffset by remember {
                    mutableStateOf(0)
                }

                var itemOffset by remember {
                    mutableStateOf(IntOffset(0, 0))
                }

                if (itemOffset.x > 0) {
                    LazyRow(modifier = Modifier.onSizeChanged {
                        maxOffset = it.width
                    }) {
                        itemActions(item)
                    }
                }

                Row(modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            color = itemBgColor,
                            topLeft = itemOffset.toOffset()
                        )
                    }
                    .offset { itemOffset }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, x ->
                                val original = itemOffset
                                val summed = original + IntOffset(x = x.roundToInt(), y = 0)

                                if (summed.x in 0..maxOffset)
                                    itemOffset = summed
                            },
                            onDragEnd = {
                                if (itemOffset.x < maxOffset / 2f)
                                    itemOffset = IntOffset(0, 0)
                                else if (itemOffset.x > maxOffset / 2f)
                                    itemOffset = IntOffset(maxOffset, 0)
                            }
                        )
                    }
                    .clickable {
                        itemOffset = IntOffset(x = maxOffset, y = 0)
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemContent(item)
                }
            }
        }
    }

    val isOnTop = remember(state.firstVisibleItemIndex) {
        state.firstVisibleItemIndex <= 0
    }

    if (isOnTop)
        IconButton(
            modifier = Modifier
                .size(Sizes.screenPadding * 3)
                .align(Alignment.BottomEnd)
                .padding(Sizes.screenPadding),
            onClick = {
                scope.launch {
                    state.scrollToItem(0)
                }
            }
        ) {
            Icon(
                Icons.Default.AlignVerticalTop,
                contentDescription = null
            )
        }
}