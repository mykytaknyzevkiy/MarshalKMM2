package com.yama.marshal.data.entity

internal data class HoleEntity(
    val id: Int,
    val idCourse: String,
    val defaultPace: Int,
    val averagePace: Double,
    val differentialPace: Double
)
