package com.yama.marshal.ui.view

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PanToolAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.yama.marshal.screen.main.SortType
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
    crossinline itemActionsCount: @DisallowComposableCalls (item: E) -> Int = { 0 },
    crossinline itemActions: LazyListScope.(item: E) -> Unit = { },
    crossinline itemContent: @Composable RowScope.(item: E) -> Unit,
    crossinline onTapItem: (item: E) -> Unit = {}
) = Box(modifier = modifier.drawBehind {
    repeat(this.size.height.roundToInt() / holderHeight.roundToPx()) {
        val y = (it * holderHeight).toPx()

        if (y < this.size.height)
            drawRect(
                color = if (it % 2 == 0) bgPositive else bgNegative,
                topLeft = Offset(x = 0f, y = (it * holderHeight).toPx()),
            )
    }
}) {
    val scope = rememberCoroutineScope()

    LazyColumn(state = state) {
        itemsIndexed(list, key = key) { position, item ->
            val itemBgColor by remember(item, position) {
                derivedStateOf {
                    customItemBgColor(item)
                        ?: if (position % 2 == 0)
                            bgPositive
                        else
                            bgNegative
                }
            }

            val maxOffset by remember {
                derivedStateOf {
                    itemActionsCount(item) * holderHeight
                }
            }

            var itemOffset by remember(item) {
                mutableStateOf(IntOffset(0, 0))
            }

            Box(modifier = Modifier.pointerInput(item) {
                val maxOffsetPx = maxOffset.roundToPx()

                detectTapGestures {
                    onTapItem(item)

                    itemOffset = if (itemOffset.x >= maxOffsetPx)
                        IntOffset(x = 0, y = 0)
                    else
                        IntOffset(x = maxOffsetPx, y = 0)
                }
            }) {
                if (itemOffset.x > 0)
                    LazyRow {
                        itemActions(item)
                    }

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = holderHeight)
                    .drawBehind {
                        drawRect(
                            color = itemBgColor,
                            topLeft = itemOffset.toOffset()
                        )
                    }
                    .offset { itemOffset }
                    .pointerInput(item) {
                        val maxOffsetPx = maxOffset.roundToPx()

                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, x ->
                                val original = itemOffset
                                val summed = original + IntOffset(x = x.roundToInt(), y = 0)

                                if (summed.x in 0..maxOffsetPx)
                                    itemOffset = summed
                            },
                            onDragEnd = {
                                if (itemOffset.x < maxOffsetPx / 2f)
                                    itemOffset = IntOffset(0, 0)
                                else if (itemOffset.x > maxOffsetPx / 2f)
                                    itemOffset = IntOffset(maxOffsetPx, 0)
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

    val showToTopBtn by remember {
        derivedStateOf {
            state.firstVisibleItemIndex > 0
        }
    }

    AnimatedVisibility(
        showToTopBtn,
        enter = slideInHorizontally(
            initialOffsetX = { +300 }
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { +300 }
        ),
        modifier = Modifier.align(Alignment.BottomEnd).padding(Sizes.screenPadding)
    ) {
        IconButton(
            modifier = Modifier
                .size(Sizes.screenPadding * 3)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                ),
            onClick = {
                scope.launch {
                    if (!state.isScrollInProgress)
                        state.animateScrollToItem(0)
                }
            }
        ) {
            Icon(
                Icons.Default.PanToolAlt,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = null
            )
        }
    }
}


@Composable
internal fun RowScope.MarshalItemText(
    text: String,
    weight: Float,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Center
) = Text(
    text = text,
    textAlign = textAlign,
    modifier = Modifier.weight(weight).padding(Sizes.screenPadding / 2),
    color = color
)

@Composable
internal fun MarshalItemDivider() = Spacer(
    modifier = Modifier
        .width(1.dp)
        .height(Sizes.fleet_view_holder_height - Sizes.screenPadding)
        .background(Color.LightGray)
)