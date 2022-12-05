package com.yama.marshal.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseRelationshipListResponse(
    @SerialName("resultList") val resultList: List<CourseRelationshipItem> = emptyList()
) {
    @Serializable
    data class CourseRelationshipItem(
        @SerialName("courseName") val courseName: String,
        @SerialName("default") val defaultCourse: Int,
        @SerialName("id_course") val idCourse: String,
        @SerialName("numberPlayers") val playersNumber: Int,
        @SerialName("layoutHoles") val layoutHoles: Int?
    )
}
