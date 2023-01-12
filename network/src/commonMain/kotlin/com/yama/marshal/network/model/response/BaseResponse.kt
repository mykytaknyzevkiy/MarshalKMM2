package com.yama.marshal.network.model.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class BaseResponse(
    @SerialName("Status") val status: Int
)
