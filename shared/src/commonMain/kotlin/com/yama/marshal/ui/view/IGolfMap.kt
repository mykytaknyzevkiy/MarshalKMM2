package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow

data class RenderData(
    val idCourse: String,
    val vectors: String
)

data class Cart(
    val id: Int,
    val name: String,
    val location: Pair<Double, Double>
)

@Composable
internal expect fun IGoldMap(modifier: Modifier,
                             renderData: RenderData,
                             hole: Flow<Int>,
                             carts: Flow<List<Cart>>
)