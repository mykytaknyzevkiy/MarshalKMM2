package com.yama.marshal.network.model.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CourseGPSVectorDetailsResponse(
    @SerialName("vectorGPSObject")
    val vectorData: String
)
