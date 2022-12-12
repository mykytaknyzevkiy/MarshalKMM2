package com.yama.marshal.data.entity

data class HoleEntity(
    val id: Int,
    val idCourse: String,
    val defaultPace: Int,
    val averagePace: Double,
    val differentialPace: Double
)
