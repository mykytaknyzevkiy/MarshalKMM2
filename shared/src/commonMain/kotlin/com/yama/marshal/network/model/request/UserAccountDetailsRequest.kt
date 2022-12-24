package com.yama.marshal.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAccountDetailsRequest(@SerialName("id_user") val idUser: Int)
