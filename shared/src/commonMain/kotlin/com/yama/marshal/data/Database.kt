package com.yama.marshal.data

import com.yama.marshal.data.entity.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal object Database {
    private val _courseList = MutableStateFlow<List<CourseEntity>>(emptyList())
    val courseList: StateFlow<List<CourseEntity>>
        get() = _courseList

    private val _cartList = MutableStateFlow<List<CartItem>>(emptyList())
    val cartList: StateFlow<List<CartItem>>
        get() = _cartList

    private val _cartRoundList = MutableStateFlow<List<CartRoundItem>>(emptyList())
    val cartRoundList: StateFlow<List<CartRoundItem>>
        get() = _cartRoundList

    private val _cartReport = MutableStateFlow<List<CartReportEntity>>(emptyList())
    val cartReport: StateFlow<List<CartReportEntity>>
        get() = _cartReport

    private val _companyMessages = MutableStateFlow<List<CompanyMessage>>(emptyList())
    val companyMessages: StateFlow<List<CompanyMessage>>
        get() = _companyMessages

    suspend fun updateCourses(data: List<CourseEntity>) {
        _courseList.emit(data)
    }

    suspend fun updateCarts(data: List<CartItem>) {
        _cartList.emit(data)
    }

    suspend fun updateCartsRound(data: List<CartRoundItem>) {
        _cartRoundList.emit(data)
    }

    suspend fun updateCartReport(data: List<CartReportEntity>) {
        _cartReport.emit(data)
    }

    suspend fun updateCompanyMessages(data: List<CompanyMessage>) {
        _companyMessages.emit(data)
    }
}