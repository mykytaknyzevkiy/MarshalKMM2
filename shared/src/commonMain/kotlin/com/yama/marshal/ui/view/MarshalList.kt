package com.yama.marshal.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation

@Composable
internal inline fun <E> MarshalList(modifier: Modifier = Modifier,
                                    list: List<E>,
                                    noinline key: ((index: Int, item: E) -> Any)? = null,
                                    crossinline itemContent: @Composable BoxScope.(item: E, position: Int) -> Unit)
= Box(modifier = modifier) {
    val orientation = currentOrientation()

    Column(modifier = Modifier.fillMaxSize()) {
        repeat(orientation.let { if (it == Orientation.LANDSCAPE) 8 else 20 }) {
            Box(modifier = Modifier
                .height(Sizes.fleet_view_holder_height)
                .fillMaxWidth()
                .background(YamaColor.itemColor(it)))
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(list, key = key, itemContent = { position, item ->
            Box(modifier = Modifier
                .height(Sizes.fleet_view_holder_height)
                .fillMaxWidth()
                .background(YamaColor.itemColor(position))) {
                itemContent(item, position)
            }
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .padding(horizontal = Sizes.screenPadding / 2)
                    .background(Color.LightGray)
            )
        })
    }
}