package com.yama.marshal.data.entity

import io.ktor.util.date.*

enum class AlertType {
    Pace, Battery, Fence
}

data class AlertEntity(
    val courseID: String,
    val date: GMTDate,
    val cartID: Int,
    val geofenceID: Int = -1,
    val type: AlertType,
    val netPace: Int = 0
)
