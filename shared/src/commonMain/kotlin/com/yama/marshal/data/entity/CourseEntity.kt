package com.yama.marshal.data.entity

internal data class CourseEntity constructor(
    val id: String,
    val courseName: String,
    val defaultCourse: Int = 0,
    val playersNumber: Int = 0,
    val layoutHoles: Int? = null,
    var vectors: String = ""
)
