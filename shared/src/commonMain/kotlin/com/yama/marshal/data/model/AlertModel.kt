package com.yama.marshal.data.model

import com.yama.marshal.data.entity.GeofenceItem
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow

sealed class AlertModel(
    open val courseID: String,
    open val date: GMTDate,
    open val cart: Flow<CartFullDetail>,
    open val course: Flow<CourseFullDetail>
) {
    data class Pace(
        override val courseID: String,
        override val date: GMTDate,
        override val cart: Flow<CartFullDetail>,
        override val course: Flow<CourseFullDetail>,
        val netPace: Int,
    ) : AlertModel(courseID, date, cart, course)

    data class Battery(
        override val courseID: String,
        override val date: GMTDate,
        override val cart: Flow<CartFullDetail>,
        override val course: Flow<CourseFullDetail>,
    ) : AlertModel(courseID, date, cart, course)

    data class Fence(
        override val courseID: String,
        override val date: GMTDate,
        override val cart: Flow<CartFullDetail>,
        override val course: Flow<CourseFullDetail>,
        val geofence: Flow<GeofenceItem>,
    ) : AlertModel(courseID, date, cart, course)
}
