package com.yama.marshal.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAccountDetailsResponse(@SerialName("id_company") val idCompany: Int)
