package com.yama.marshal.data.entity

data class CourseEntity internal constructor(
    val id: String,
    val courseName: String,
    val defaultCourse: Int = 0,
    val playersNumber: Int = 0,
    val layoutHoles: Int? = null,
    var vectors: String = ""
)
