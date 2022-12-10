package com.yama.marshal.network.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CartDetailsListRequest(
    @SerialName("id_company") val idCompany : Int,
    val active: Int,
    val status: Int
)
