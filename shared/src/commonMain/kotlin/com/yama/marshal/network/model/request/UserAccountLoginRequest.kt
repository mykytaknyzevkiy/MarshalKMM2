package com.yama.marshal.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UserAccountLoginRequest(@SerialName("username") val userName: String?,
                              @SerialName("password") val password: String?)
