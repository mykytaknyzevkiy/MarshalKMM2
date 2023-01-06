package com.yama.marshal.data.model

import com.yama.marshal.data.entity.GeofenceItem
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow

sealed class AlertModel(
    open val id: Int,
    open val courseID: String,
    open val date: GMTDate,
    open val cart: CartFullDetail,
    open val course: CourseFullDetail
) {
    data class Pace(
        override val id: Int,
        override val courseID: String,
        override val date: GMTDate,
        override val cart: CartFullDetail,
        override val course: CourseFullDetail,
        val netPace: Int,
    ) : AlertModel(id, courseID, date, cart, course)

    data class Battery(
        override val id: Int,
        override val courseID: String,
        override val date: GMTDate,
        override val cart: CartFullDetail,
        override val course: CourseFullDetail,
    ) : AlertModel(id, courseID, date, cart, course)

    data class Fence(
        override val id: Int,
        override val courseID: String,
        override val date: GMTDate,
        override val cart: CartFullDetail,
        override val course: CourseFullDetail,
        val geofence: GeofenceItem,
    ) : AlertModel(id, courseID, date, cart, course)
}
