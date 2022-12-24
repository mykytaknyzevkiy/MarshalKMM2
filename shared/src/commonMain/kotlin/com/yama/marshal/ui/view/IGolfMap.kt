package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow

data class RenderData(
    val idCourse: String,
    val vectors: String
)

@Composable
internal expect fun IGoldMap(modifier: Modifier,
                             renderData: RenderData,
                             hole: Flow<Int>
)