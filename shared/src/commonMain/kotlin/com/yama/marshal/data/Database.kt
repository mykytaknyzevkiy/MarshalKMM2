package com.yama.marshal.data

import com.yama.marshal.data.entity.CartItem
import com.yama.marshal.data.entity.CourseEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal object Database {
    private val _courseList = MutableStateFlow<List<CourseEntity>>(emptyList())
    val courseList: StateFlow<List<CourseEntity>>
        get() = _courseList

    private val _cartList = MutableStateFlow<List<CartItem>>(emptyList())
    val cartList: StateFlow<List<CartItem>>
        get() = _cartList

    suspend fun updateCourses(data: List<CourseEntity>) {
        _courseList.emit(data)
    }

    suspend fun updateCarts(data: List<CartItem>) {
        _cartList.emit(data)
    }
}