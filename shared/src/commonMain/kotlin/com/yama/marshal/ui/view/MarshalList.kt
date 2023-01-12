package com.yama.marshal.ui.view

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.yama.marshal.MPlatform
import com.yama.marshal.mPlatform
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
internal fun <E> PlatformList(
    listItem: List<E>,
    key: ((item: E) -> Any)? = null,
    customItemBgColor: (item: E) -> Color? = { null },
    itemActions: @Composable RowScope.(item: E) -> Unit = { },
    itemContent: @Composable RowScope.(item: E) -> Unit,
    onTapItem: (item: E) -> Unit = {}
) {
    val bgPositive: Color = YamaColor.itemColor(0)
    val bgNegative: Color = YamaColor.itemColor(1)

    val scrollConnection: ScrollState = rememberScrollState()
    val lazyScroll: LazyListState = rememberLazyListState()

    Box {
        if (mPlatform == MPlatform.ANDROID)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        var bgItemColor =
                            if (lazyScroll.firstVisibleItemIndex % 2 == 0) bgPositive else bgNegative

                        lazyScroll.layoutInfo.visibleItemsInfo.forEachIndexed { index, lazyListItemInfo ->
                            drawRect(
                                color = bgItemColor,
                                topLeft = Offset(
                                    x = 0f,
                                    y = index * lazyListItemInfo.size.toFloat() - if (index > 0) lazyScroll.firstVisibleItemScrollOffset else 0
                                ),
                                size = Size(
                                    width = this.size.width,
                                    lazyListItemInfo.size.toFloat()
                                )
                            )

                            bgItemColor = if (bgItemColor == bgPositive)
                                bgNegative
                            else
                                bgPositive
                        }
                    },
                state = lazyScroll
            ) {
                items(listItem, key = key) { item ->
                    MarshallListItemLogic(
                        item,
                        customItemBgColor,
                        itemActions,
                        itemContent,
                        onTapItem
                    )
                }
            }
        else {
            val holderHeight = Sizes.fleet_view_holder_height

            Column(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollConnection)
                .drawBehind {
                    repeat(listItem.size) { index ->
                        drawRect(
                            color = if (index % 2 == 0) bgPositive else bgNegative,
                            topLeft = Offset(
                                x = 0f,
                                y = index * holderHeight.toPx()
                            ),
                            size = Size(
                                width = this.size.width,
                                holderHeight.toPx()
                            )
                        )
                    }
                }
            ) {
                listItem.forEach { item ->
                    MarshallListItemLogic(
                        item,
                        customItemBgColor,
                        itemActions,
                        itemContent,
                        onTapItem
                    )
                }
            }
        }


        scrollTopBtn(scrollConnection, lazyScroll)
    }
}

@Composable
internal fun <E> PlatformList(
    listState: Flow<List<E>>,
    key: ((item: E) -> Any)? = null,
    customItemBgColor: (item: E) -> Color? = { null },
    itemActions: @Composable RowScope.(item: E) -> Unit = { },
    itemContent: @Composable RowScope.(item: E) -> Unit,
    onTapItem: (item: E) -> Unit = {}
) {
    val bgPositive: Color = YamaColor.itemColor(0)
    val bgNegative: Color = YamaColor.itemColor(1)

    val scrollConnection: ScrollState = rememberScrollState()
    val lazyScroll: LazyListState = rememberLazyListState()

    val listItem = remember {
        listState
    }.collectAsState(emptyList())

    NList(
        listItem = listItem,
        bgPositive = bgPositive,
        bgNegative = bgNegative,
        scrollConnection = scrollConnection,
        lazyScroll = lazyScroll,
        key = key,
        customItemBgColor = customItemBgColor,
        itemActions = itemActions,
        itemContent = itemContent,
        onTapItem = onTapItem
    )

    /*LaunchedEffect(Unit) {
        var firstItem: E? = null

        listState
            .onEach {
                if (it.isNotEmpty() && firstItem != it.first()) {
                    lazyScroll.scrollToItem(0)
                    firstItem = it.first()
                }
            }
            .launchIn(this)
    }*/
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

    if (isScrolled)
        Box(
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
    scrollConnection: ScrollState,
    lazyScroll: LazyListState,
    key: ((item: E) -> Any)? = null,
    customItemBgColor: (item: E) -> Color? = { null },
    itemActions: @Composable RowScope.(item: E) -> Unit = { },
    itemContent: @Composable RowScope.(item: E) -> Unit,
    onTapItem: (item: E) -> Unit = {}
) = if (mPlatform == MPlatform.IOS)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollConnection)
    ) {
        val iterator = listItem.value.listIterator()

        var bg: Color = bgPositive

        while (iterator.hasNext()) {
            val item = iterator.next()

            MarshallListItemLogic(
                item,
                customItemBgColor,
                itemActions,
                itemContent,
                onTapItem
            )

            bg = if (bg == bgPositive) bgNegative else bgPositive
        }
    }
else
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                var bgItemColor =
                    if (lazyScroll.firstVisibleItemIndex % 2 == 0) bgPositive else bgNegative

                lazyScroll.layoutInfo.visibleItemsInfo.forEachIndexed { index, lazyListItemInfo ->
                    drawRect(
                        color = bgItemColor,
                        topLeft = Offset(
                            x = 0f,
                            y = index * lazyListItemInfo.size.toFloat() - if (index > 0) lazyScroll.firstVisibleItemScrollOffset else 0
                        ),
                        size = Size(width = this.size.width, lazyListItemInfo.size.toFloat())
                    )

                    bgItemColor = if (bgItemColor == bgPositive)
                        bgNegative
                    else
                        bgPositive
                }

                if (lazyScroll.layoutInfo.visibleItemsInfo.size >= 7)
                    drawContent()
            },
        state = lazyScroll
    ) {
        items(listItem.value, key = key) { item ->
            MarshallListItemLogic(
                item,
                customItemBgColor,
                itemActions,
                itemContent,
                onTapItem
            )
        }
    }

@Composable
private fun <E> MarshallListItemLogic(
    item: E,
    customItemBgColor: (item: E) -> Color? = { null },
    itemActions: @Composable RowScope.(item: E) -> Unit = { },
    itemContent: @Composable RowScope.(item: E) -> Unit,
    onTapItem: (item: E) -> Unit = {}
) {
    val bgColor by remember(item) {
        derivedStateOf {
            customItemBgColor(item)
        }
    }

    var showActions by remember {
        mutableStateOf(false)
    }

    Box(
        contentAlignment = Alignment.CenterStart
    ) {
        var actionsWidth by remember {
            mutableStateOf(0)
        }

        if (showActions)
            Row(modifier = Modifier.onSizeChanged {
                actionsWidth = it.width
            }) { itemActions(item) }
        else
            actionsWidth = 0

        Row(
            modifier = Modifier
                .let {
                    if (mPlatform == MPlatform.ANDROID)
                        it.defaultMinSize(minHeight = Sizes.fleet_view_holder_height)
                    else
                        it.height(Sizes.fleet_view_holder_height)
                }
                .background(bgColor ?: Color.Transparent)
                .offset { IntOffset(x = actionsWidth, y = 0) }
                .clickable(
                    enabled = true,
                    onClickLabel = null,
                    role = null,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        onTapItem(item)
                        showActions = !showActions
                    }
                ),
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
        softWrap = if (mPlatform == MPlatform.ANDROID) text.contains(" ") else false,
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