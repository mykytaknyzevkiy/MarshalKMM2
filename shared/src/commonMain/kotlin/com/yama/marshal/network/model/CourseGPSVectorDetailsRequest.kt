package com.yama.marshal.network.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CourseGPSVectorDetailsRequest(
    @SerialName("id_course") val idCourse: String
)
