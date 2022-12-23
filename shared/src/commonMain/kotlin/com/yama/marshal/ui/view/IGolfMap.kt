package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.yama.marshal.data.model.CourseFullDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class RenderData(
    val idCourse: String,
    val vectors: String
)

@Composable
internal expect fun IGoldMap(modifier: Modifier,
                             renderData: RenderData,
                             hole: Flow<Int>
)