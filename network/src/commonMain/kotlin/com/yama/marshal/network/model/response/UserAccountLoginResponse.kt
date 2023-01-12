package com.yama.marshal.network.model.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class UserAccountLoginResponse(@SerialName("id_user")   val idUser      : Int,
                                    @SerialName("secretKey") val secretKey   : String)