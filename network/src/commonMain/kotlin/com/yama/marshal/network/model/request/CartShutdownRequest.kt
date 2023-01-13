package com.yama.marshal.network.model.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CartShutdownRequest(
    @SerialName("id_cart")
    val idCart: Int
)
