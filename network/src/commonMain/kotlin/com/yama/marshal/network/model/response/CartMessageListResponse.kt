package com.yama.marshal.network.model.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CartMessageListResponse(
    @SerialName("resultList") val list: List<CartMessageListItem>
) {
    @kotlinx.serialization.Serializable
    data class CartMessageListItem(
        @SerialName("id_message")val id: Int,
        @SerialName("message") val message: String
    )
}
