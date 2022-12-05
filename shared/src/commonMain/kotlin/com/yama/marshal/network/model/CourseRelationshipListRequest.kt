package com.yama.marshal.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseRelationshipListRequest(
    @SerialName("id_company") val idCompany: Int
)
