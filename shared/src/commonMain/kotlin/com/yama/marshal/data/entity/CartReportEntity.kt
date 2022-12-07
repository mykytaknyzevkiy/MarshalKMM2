package com.yama.marshal.data.entity

data class CartReportEntity(
    val holeNumber: Int,
    val idCourse: String,
    val defaultPace: Int,
    val averagePace: Double,
    val differentialPace: Double
)
