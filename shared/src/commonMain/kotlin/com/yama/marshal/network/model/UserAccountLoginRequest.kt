package com.yama.marshal.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UserAccountLoginRequest(@SerialName("username") val userName: String?,
                              @SerialName("password") val password: String?)
