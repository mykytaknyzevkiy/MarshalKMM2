package com.yama.marshal.network.model.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class GeofenceListRequest(
    @SerialName("id_company")
    val idCompany: Int,
    @SerialName("active")
    val isActive: Int
)
