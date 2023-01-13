package com.yama.marshal.data.entity

import io.ktor.util.date.*

internal enum class AlertType {
    Pace, Battery, Fence
}

internal data class AlertEntity(
    val id: Int = 0,
    val courseID: String,
    val date: GMTDate,
    val cartID: Int,
    val geofenceID: Int = -1,
    val type: AlertType,
    val netPace: Int = 0
)
