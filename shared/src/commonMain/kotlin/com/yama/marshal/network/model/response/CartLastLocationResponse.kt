package com.yama.marshal.network.model.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CartLastLocationResponse(
    @SerialName("resultList")
    val list: List<List<String?>>
)
