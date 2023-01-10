package com.yama.marshal.ui.view

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import co.touchlab.kermit.Logger
import com.yama.marshal.MPlatform
import com.yama.marshal.mPlatform
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun <E> PlatformList(
    listItem: State<List<E>>,
    key: ((position: Int, item: E) -> Any)? = null,
    customItemBgColor: (item: E) -> Color? = { null },
    itemActionsCount: (item: E) -> Int = { 0 },
    itemActions: @Composable RowScope.(item: E) -> Unit = { },
    itemContent: @Composable RowScope.(item: E) -> Unit,
    onTapItem: (item: E) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val bgPositive: Color = YamaColor.itemColor(0)
    val bgNegative: Color = YamaColor.itemColor(1)
    val holderHeight = Sizes.fleet_view_holder_height

    Box(modifier = Modifier.fillMaxSize()) {
        val scrollConnection = rememberScrollState()
        val lazyScroll = rememberLazyListState()

        if (mPlatform == MPlatform.IOS)
            Column(modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val itemHeightPX = holderHeight.roundToPx()
                    val maxHeightPX = this.size.height.roundToInt()

                    repeat(maxHeightPX / itemHeightPX) {
                        val y = it * itemHeightPX

                        if (y < maxHeightPX)
                            drawRect(
                                color = if (it % 2 == 0) bgPositive else bgNegative,
                                topLeft = Offset(x = 0f, y = y.toFloat()),
                            )
                    }
                }
                .verticalScroll(scrollConnection)
            ) {
                val iterator = listItem.value.listIterator()
                var position = 0

                while (iterator.hasNext()) {
                    val item = iterator.next()

                    MarshallListItemLogic(
                        position,
                        item,
                        holderHeight,
                        bgPositive,
                        bgNegative,
                        customItemBgColor,
                        itemActionsCount,
                        itemActions,
                        itemContent,
                        onTapItem
                    )

                    position++
                }
            }
        else
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val itemHeightPX = holderHeight.roundToPx()
                        val maxHeightPX = this.size.height.roundToInt()

                        repeat(maxHeightPX / itemHeightPX) {
                            val y = it * itemHeightPX

                            if (y < maxHeightPX)
                                drawRect(
                                    color = if (it % 2 == 0) bgPositive else bgNegative,
                                    topLeft = Offset(x = 0f, y = y.toFloat()),
                                )
                        }
                    },
                state = lazyScroll
            ) {
                itemsIndexed(listItem.value, key) { position, item ->
                    MarshallListItemLogic(
                        position,
                        item,
                        holderHeight,
                        bgPositive,
                        bgNegative,
                        customItemBgColor,
                        itemActionsCount,
                        itemActions,
                        itemContent,
                        onTapItem
                    )
                }
            }

        val firstItem = remember(mPlatform) {
            mutableStateOf<E?>(null)
        }

        val scrollToTop by remember {
            derivedStateOf {
                !lazyScroll.isScrollInProgress && !scrollConnection.isScrollInProgress &&
                        listItem.value.isNotEmpty() && listItem.value.first() != firstItem.value
            }
        }

        if (scrollToTop) {
            scope.launch {
                scrollConnection.scrollTo(0)
                lazyScroll.scrollToItem(0)
            }

            firstItem.value = listItem.value.first()
        }

        val isScrolled by remember {
            derivedStateOf {
                scrollConnection.value > 0
                        || lazyScroll.firstVisibleItemIndex > 0
            }
        }

        AnimatedVisibility(
            isScrolled,
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
                        scrollConnection.scrollTo(0)
                        lazyScroll.scrollToItem(0)
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
}

@Composable
internal fun <E> MarshalList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    list: List<E>,
    holderHeight: Dp = Sizes.fleet_view_holder_height,
    bgPositive: Color = YamaColor.itemColor(0),
    bgNegative: Color = YamaColor.itemColor(1),
    key: ((position: Int, item: E) -> Any)? = null,
    customItemBgColor: @DisallowComposableCalls (item: E) -> Color? = { null },
    itemActionsCount: @DisallowComposableCalls (item: E) -> Int = { 0 },
    itemActions: LazyListScope.(item: E) -> Unit = { },
    itemContent: @Composable RowScope.(item: E) -> Unit,
    onTapItem: (item: E) -> Unit = {}
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

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        list.forEachIndexed { position, item ->
            MarshallListItemLogic(
                position,
                item,
                holderHeight,
                bgPositive,
                bgNegative,
                customItemBgColor,
                itemActionsCount,
                itemActions = {},
                itemContent,
                onTapItem
            )
        }
    }

    val showToTopBtn by remember {
        if (mPlatform == MPlatform.ANDROID)
            derivedStateOf {
                state.firstVisibleItemIndex > 0
            }
        else
            derivedStateOf {
                scrollState.value > 0
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
                    if (mPlatform == MPlatform.ANDROID) {
                        if (!state.isScrollInProgress)
                            state.scrollToItem(0)
                    } else {
                        if (!scrollState.isScrollInProgress)
                            scrollState.scrollTo(0)
                    }
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
private fun <E> MarshallListItemLogic(
    position: Int,
    item: E,
    holderHeight: Dp = Sizes.fleet_view_holder_height,
    bgPositive: Color = YamaColor.itemColor(0),
    bgNegative: Color = YamaColor.itemColor(1),
    customItemBgColor: (item: E) -> Color? = { null },
    itemActionsCount: (item: E) -> Int = { 0 },
    itemActions: @Composable RowScope.(item: E) -> Unit = { },
    itemContent: @Composable RowScope.(item: E) -> Unit,
    onTapItem: (item: E) -> Unit = {}
) {
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

    Box(
        modifier = Modifier
            .pointerInput(item) {
                val maxOffsetPx = maxOffset.roundToPx()

                detectTapGestures {
                    onTapItem(item)

                    itemOffset = if (itemOffset.x >= maxOffsetPx)
                        IntOffset(x = 0, y = 0)
                    else
                        IntOffset(x = maxOffsetPx, y = 0)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        if (itemOffset.x > 0)
            Row {
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
                forEachGesture {
                    awaitPointerEventScope {
                        val down = awaitFirstDown(requireUnconsumed = false)

                        val maxOffsetPx = maxOffset.roundToPx()

                        this.horizontalDrag(
                            pointerId = down.id
                        ) {
                            val original = itemOffset
                            val summed = original + IntOffset(
                                x = it.positionChange().x.roundToInt(),
                                y = 0
                            )

                            if (abs(it.positionChange().x) < 20 && summed.x < maxOffsetPx / 4)
                                itemOffset = IntOffset(0, 0)
                            /*else if (summed.x < maxOffsetPx / 2f)
                                itemOffset = IntOffset(0, 0)
                            else if (summed.x > maxOffsetPx / 2f)
                                itemOffset = IntOffset(maxOffsetPx, 0)*/
                            else if (summed.x in 0..maxOffsetPx)
                                itemOffset = summed
                        }

                        if (itemOffset.x < maxOffsetPx / 2f)
                            itemOffset = IntOffset(0, 0)
                        else if (itemOffset.x > maxOffsetPx / 2f)
                            itemOffset = IntOffset(maxOffsetPx, 0)
                    }
                }
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemContent(item)
        }
    }
}

@Composable
internal fun RowScope.MarshalItemText(
    text: String,
    weight: Float,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Center
) {
    val style: TextStyle = MaterialTheme.typography.bodyMedium

    var textRealStyle by remember {
        mutableStateOf(style)
    }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        textAlign = textAlign,
        modifier = Modifier.weight(weight)
            .padding(Sizes.screenPadding / 2)
            .drawWithContent {
                if (readyToDraw)
                    drawContent()
            },
        color = color,
        softWrap = text.contains(" "),
        style = textRealStyle,
        onTextLayout = {
            if (it.didOverflowWidth)
                textRealStyle = textRealStyle.copy(
                    fontSize = textRealStyle.fontSize * 0.9
                )
            else
                readyToDraw = true
        }
    )
}

@Composable
internal fun MarshalItemDivider() = Spacer(
    modifier = Modifier
        .width(1.dp)
        .height(Sizes.fleet_view_holder_height - Sizes.screenPadding)
        .background(Color.LightGray)
)