package com.yama.marshal.network

import co.touchlab.kermit.Logger
import com.yama.marshal.network.model.*

class DNAService : YamaNetworkService("https://api-dna.igolf.com/rest/action/") {
    companion object {
        private const val TAG = "DNAService"
    }

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

    suspend fun userData(body: UserAccountDetailsRequest): UserAccountDetailsResponse? {
        post<UserAccountDetailsRequest, UserAccountDetailsResponse>(
            action = Action.UserDetails,
            payload = body
        ).also {
            if (it == null) {
               Logger.e(tag = TAG, message = {
                   "Cannot make request: userData"
               })
            }
            return it
        }
    }

    suspend fun courseRelationshipList(body: CourseRelationshipListRequest): CourseRelationshipListResponse? {
        post<CourseRelationshipListRequest, CourseRelationshipListResponse>(
            action = Action.CourseRelationshipList,
            payload = body
        ).also {
            if (it == null) {
                Logger.e(tag = TAG, message = {
                    "Cannot make request: userData"
                })
            }
            return it
        }
    }

}