package com.yama.marshal.repository

import com.yama.marshal.network.DNAService
import com.yama.marshal.network.model.UserAccountLoginRequest
import com.yama.marshal.tool.prefs
import com.yama.marshal.tool.secretKey
import com.yama.marshal.tool.userID
import com.yama.marshal.tool.userName

class UserRepository {
    private val dnaService = DNAService()

    suspend fun login(userName: String, password: String): Boolean {
        UserAccountLoginRequest(userName, password).let { body ->
            try {
                dnaService.userLogin(body)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }.also {
            if (it == null)
                return false

            prefs.userName = userName
            prefs.userID = it.idUser
            prefs.secretKey = it.secretKey

            return true
        }
    }

    fun isUserLogin(): Boolean = !prefs.userName.isNullOrEmpty()
}