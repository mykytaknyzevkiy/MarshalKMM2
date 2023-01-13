package com.yama.marshal.network.model.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CartLastLocationRequest(
    @SerialName("cartList")
    val ids: List<Int>
)
