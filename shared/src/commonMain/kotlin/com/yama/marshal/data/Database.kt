package com.yama.marshal.data

import co.touchlab.kermit.Logger
import com.yama.marshal.data.entity.*
import com.yama.marshal.data.model.AlertModel
import com.yama.marshal.tool.indexOfFirst
import com.yama.marshal.tool.set
import io.ktor.util.date.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlin.jvm.Synchronized

internal object Database {
    private const val TAG = "Database"

    private val _courseList = MutableStateFlow<List<CourseEntity>>(emptyList())
    val courseList: StateFlow<List<CourseEntity>>
        get() = _courseList

    private val _cartList = MutableStateFlow<List<CartItem>>(emptyList())
    val cartList: StateFlow<List<CartItem>>
        get() = _cartList

    private val _cartRoundList = MutableStateFlow<List<CartRoundItem>>(emptyList())
    val cartRoundList: StateFlow<List<CartRoundItem>>
        get() = _cartRoundList

    private val _cartReport = MutableStateFlow<List<HoleEntity>>(emptyList())
    val cartReport: StateFlow<List<HoleEntity>>
        get() = _cartReport

    private val _companyMessages = MutableStateFlow<List<CompanyMessage>>(emptyList())
    val companyMessages: StateFlow<List<CompanyMessage>>
        get() = _companyMessages

    private val _geofenceList = MutableStateFlow<List<GeofenceItem>>(emptyList())
    val geofenceList: StateFlow<List<GeofenceItem>>
        get() = _geofenceList

    private val _alerts = MutableStateFlow<List<AlertEntity>>(emptyList())
    val alerts: StateFlow<List<AlertEntity>>
        get() = _alerts

    @Synchronized
    fun addAlert(data: AlertEntity) {
        _alerts.value.toMutableList().apply {
            add(data.copy(id = this.size, date = GMTDate(timestamp = data.date.timestamp + this.size)))
        }.also {
            _alerts.value = it
        }
    }

    suspend fun updateCourses(data: List<CourseEntity>) {
        _courseList.emit(data)

        Logger.i(tag = TAG, message = {
            "courses success saved"
        })
    }

    suspend fun updateCarts(data: List<CartItem>) {
        _cartList.emit(data)

        Logger.i(tag = TAG, message = {
            "carts success saved"
        })
    }

    suspend fun updateCartsRound(data: List<CartRoundItem>) {
        _cartRoundList.emit(data)
    }

    suspend fun addCartRound(data: CartRoundItem) {
        _cartRoundList.value.toMutableList().apply {
            add(data)
        }.also {
            updateCartsRound(it)
        }
    }

    suspend fun updateCartReport(data: List<HoleEntity>) {
        _cartReport.emit(data)
    }

    suspend fun updateCompanyMessages(data: List<CompanyMessage>) {
        _companyMessages.emit(data)
    }

    suspend fun updateGeofenceList(data: List<GeofenceItem>) {
        _geofenceList.emit(data)
    }

    fun updateCart(data: CartItem) {
        val index = _cartList.indexOfFirst { it.id == data.id }

        if (index < 0) {
            Logger.e(TAG, message = {
                "Cannot find cart to update in database"
            })
        }

        _cartList[index] = data
    }

    fun cartBy(id: Int) = cartList
        .map { l ->
            l.find { it.id == id }
        }
}