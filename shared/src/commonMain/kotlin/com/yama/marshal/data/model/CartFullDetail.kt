package com.yama.marshal.data.model

import com.yama.marshal.data.entity.CourseEntity
import io.ktor.util.date.*

data class CartFullDetail(
    val id: Int,
    val course: CourseEntity,
    val cartName: String,
    val startTime: GMTDate? = null,
    val currPosTime: String? = null,
    val currPosLon: Double? = null,
    val currPosLat: Double? = null,
    val currPosHole: Int? = null,
    val totalNetPace: Int? = null,
    val totalElapsedTime: Int? = null,
)
