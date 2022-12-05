package com.yama.marshal.data

import com.yama.marshal.data.entity.CourseEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal object Database {
    private val _courseList = MutableStateFlow<List<CourseEntity>>(emptyList())
    val courseList: StateFlow<List<CourseEntity>>
        get() = _courseList

    suspend fun updateCourses(data: List<CourseEntity>) {
        _courseList.emit(data)
    }
}