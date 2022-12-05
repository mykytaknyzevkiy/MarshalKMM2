package com.yama.marshal.data.entity

data class CourseEntity internal constructor(
    val id: String,
    val courseName: String,
    val defaultCourse: Int,
    val playersNumber: Int,
    val layoutHoles: Int?
)
