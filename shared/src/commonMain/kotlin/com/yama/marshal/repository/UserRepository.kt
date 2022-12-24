package com.yama.marshal.repository

import co.touchlab.kermit.Logger
import com.yama.marshal.network.model.request.UserAccountDetailsRequest
import com.yama.marshal.network.model.request.UserAccountLoginRequest
import com.yama.marshal.network.service.DNAService
import com.yama.marshal.tool.*

class UserRepository {
    companion object {
        private const val TAG = "UserRepository"
    }

    private val dnaService = DNAService()

    suspend fun login(userName: String, password: String): Boolean {
        Logger.i(tag = TAG, message = {
            "onLogin with data: userName = $userName and password $password"
        })

        UserAccountLoginRequest(userName, password).let { body ->
            try {
                dnaService.userLogin(body)
            } catch (e: Exception) {
                e.printStackTrace()
                Logger.e(tag = TAG, throwable = e, message = {
                    "login error"
                })
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

    suspend fun userData(): Boolean {
        Logger.i(tag = TAG, message = {
            "userData with userID ${prefs.userID}"
        })

        UserAccountDetailsRequest(
            idUser = prefs.userID
        ).let {
            dnaService.userData(it)
        }.also {
            if (it == null)
                return false

            prefs.companyID = it.idCompany

            return true
        }
    }

    fun isUserLogin(): Boolean = !prefs.userName.isNullOrEmpty()
}