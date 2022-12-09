package com.yama.marshal.data.model

data class CourseFullDetail(
    val id: String?,
    val courseName: String,
    val defaultCourse: Int = 0,
    val playersNumber: Int = 0,
    val layoutHoles: Int? = null,
    val holes: List<HoleData> = emptyList()
) {
    data class HoleData(
        val holeNumber: Int,
        val defaultPace: Int,
        val averagePace: Double,
        val differentialPace: Double
    )
}
