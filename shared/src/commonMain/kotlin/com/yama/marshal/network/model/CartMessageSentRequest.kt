package com.yama.marshal.network.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CartMessageSentRequest(
    @SerialName("cartArray") val cartIds: List<Int>,
    @SerialName("customMessage") val customMessage: String?,
    @SerialName("id_message") val idMessage: Int?
)
