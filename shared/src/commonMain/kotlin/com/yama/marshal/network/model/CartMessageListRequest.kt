package com.yama.marshal.network.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CartMessageListRequest(
    @SerialName("id_company") val idCompany: Int,
    @SerialName("active") val active: Int
)
