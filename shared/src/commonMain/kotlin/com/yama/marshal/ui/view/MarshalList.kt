package com.yama.marshal.ui.view

import androidx.compose.animation.*
import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.yama.marshal.MPlatform
import com.yama.marshal.mPlatform
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun <E> PlatformList(
    listState: Flow<List<E>>,
    key: ((position: Int, item: E) -> Any)? = null,
    customItemBgColor: (item: E) -> Color? = { null },
    itemActionsCount: (item: E) -> Int = { 0 },
    itemActions: @Composable RowScope.(item: E) -> Unit = { },
    itemContent: @Composable RowScope.(item: E) -> Unit,
    onTapItem: (item: E) -> Unit = {}
) {
    val bgPositive: Color = YamaColor.itemColor(0)
    val bgNegative: Color = YamaColor.itemColor(1)
    val holderHeight: Dp = Sizes.fleet_view_holder_height

    val scrollConnection: ScrollState = rememberScrollState()
    val lazyScroll: LazyListState = rememberLazyListState()

    val listItem = remember {
        listState
    }
        .collectAsState(emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        NList(
            listItem = listItem,
            bgPositive = bgPositive,
            bgNegative = bgNegative,
            holderHeight = holderHeight,
            scrollConnection = scrollConnection,
            lazyScroll = lazyScroll,
            key = key,
            customItemBgColor = customItemBgColor,
            itemActionsCount = itemActionsCount,
            itemActions = itemActions,
            itemContent = itemContent,
            onTapItem = onTapItem
        )

        scrollTopBtn(scrollConnection, lazyScroll)
    }

    LaunchedEffect(Unit) {
        var firstItem: E? = null

        listState
            .onEach {
            if (it.isNotEmpty() && firstItem != it.first()) {
                lazyScroll.scrollToItem(0)
                firstItem = it.first()
            }
        }
            .launchIn(this)
    }
}

@Composable
private fun BoxScope.scrollTopBtn(
    scrollConnection: ScrollState,
    lazyScroll: LazyListState
) {
    val scope = rememberCoroutineScope()

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
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(Sizes.screenPadding)
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

@Composable
private fun <E> NList(
    listItem: State<List<E>>,
    bgPositive: Color,
    bgNegative: Color,
    holderHeight: Dp,
    scrollConnection: ScrollState,
    lazyScroll: LazyListState,
    key: ((position: Int, item: E) -> Any)? = null,
    customItemBgColor: (item: E) -> Color? = { null },
    itemActionsCount: (item: E) -> Int = { 0 },
    itemActions: @Composable RowScope.(item: E) -> Unit = { },
    itemContent: @Composable RowScope.(item: E) -> Unit,
    onTapItem: (item: E) -> Unit = {}
) = if (mPlatform == MPlatform.IOS)
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

        var bg: Color = bgPositive

        while (iterator.hasNext()) {
            val item = iterator.next()

            MarshallListItemLogic(
                item,
                holderHeight,
                bg,
                customItemBgColor,
                itemActionsCount,
                itemActions,
                itemContent,
                onTapItem
            )

            bg = if (bg == bgPositive) bgNegative else bgPositive
        }
    }
else {
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
        itemsIndexed(listItem.value, key = key) { position, item ->
            MarshallListItemLogic(
                item,
                holderHeight,
                if (position % 2 == 0) bgPositive else bgNegative,
                customItemBgColor,
                itemActionsCount,
                itemActions,
                itemContent,
                onTapItem
            )
        }
    }
}

@Composable
private fun <E> MarshallListItemLogic(
    item: E,
    holderHeight: Dp = Sizes.fleet_view_holder_height,
    bgColor: Color,
    customItemBgColor: (item: E) -> Color? = { null },
    itemActionsCount: (item: E) -> Int = { 0 },
    itemActions: @Composable RowScope.(item: E) -> Unit = { },
    itemContent: @Composable RowScope.(item: E) -> Unit,
    onTapItem: (item: E) -> Unit = {}
) {
    val maxOffset by remember {
        derivedStateOf {
            itemActionsCount(item) * holderHeight
        }
    }

    var itemOffset by remember(item) {
        mutableStateOf(IntOffset(0, 0))
    }

    Box(
        modifier = Modifier.pointerInput(item) {
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
                    color = customItemBgColor(item) ?: bgColor,
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