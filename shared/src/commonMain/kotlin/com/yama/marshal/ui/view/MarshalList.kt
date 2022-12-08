package com.yama.marshal.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.theme.YamaColor
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation

@Composable
internal fun <E> MarshalList(modifier: Modifier = Modifier,
                    list: List<E>,
                    itemContent: @Composable (item: E) -> Unit)
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
        itemsIndexed(list, itemContent = { position, item ->
            Box(modifier = Modifier
                .height(Sizes.fleet_view_holder_height)
                .fillMaxWidth()
                .background(YamaColor.itemColor(position))) {
                itemContent(item)
            }
        })
    }
}