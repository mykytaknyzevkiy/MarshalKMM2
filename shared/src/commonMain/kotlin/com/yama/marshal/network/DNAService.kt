package com.yama.marshal.network

import com.yama.marshal.network.model.UserAccountLoginRequest
import com.yama.marshal.network.model.UserAccountLoginResponse

class DNAService : YamaNetworkService("https://api-dna.igolf.com/rest/action/") {

    suspend fun userLogin(body: UserAccountLoginRequest): UserAccountLoginResponse {
        post<UserAccountLoginRequest, UserAccountLoginResponse>(
            action = Action.UserLogin,
            payload = body
        ).also {
            if (it == null)
                throw Exception("Cannot make request")
            else
                return it
        }
    }

}