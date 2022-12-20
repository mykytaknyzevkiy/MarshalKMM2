package com.yama.marshal.data.model

import com.yama.marshal.data.entity.GeofenceItem
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow

sealed class AlertModel(
    open val date: GMTDate,
    open val cart: Flow<CartFullDetail>,
    open val course: Flow<CourseFullDetail>
) {
    data class Pace(
        override val date: GMTDate,
        override val cart: Flow<CartFullDetail>,
        override val course: Flow<CourseFullDetail>,
        val netPace: Int,
    ) : AlertModel(date, cart, course)

    data class Battery(
        override val date: GMTDate,
        override val cart: Flow<CartFullDetail>,
        override val course: Flow<CourseFullDetail>,
    ) : AlertModel(date, cart, course)

    data class Fence(
        override val date: GMTDate,
        override val cart: Flow<CartFullDetail>,
        override val course: Flow<CourseFullDetail>,
        val geofence: Flow<GeofenceItem>,
    ) : AlertModel(date, cart, course)
}
