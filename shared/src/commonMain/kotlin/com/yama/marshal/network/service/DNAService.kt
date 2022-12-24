package com.yama.marshal.network.service

import co.touchlab.kermit.Logger
import com.yama.marshal.network.model.request.*
import com.yama.marshal.network.model.response.*
import com.yama.marshal.network.unit.Action
import com.yama.marshal.network.unit.YamaNetworkService

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

    suspend fun cartDetailsList(body: CartDetailsListRequest): CartDetailsListResponse? =
        post<CartDetailsListRequest, CartDetailsListResponse>(
            action = Action.CartDetailsList,
            payload = body
        ).also {
            if (it == null) {
                Logger.e(tag = TAG, message = {
                    "Cannot make request: userData"
                })
            }
        }

    suspend fun companyCartsRoundDetails(body: CompanyCartsRoundDetailsRequest): CompanyCartsRoundDetailsResponse? =
        post<CompanyCartsRoundDetailsRequest, CompanyCartsRoundDetailsResponse>(
            action = Action.CompanyCartsRoundDetails,
            payload = body
        ).also {
            if (it == null) {
                Logger.e(tag = TAG, message = {
                    "Cannot make request: userData"
                })
            }
        }

    suspend fun cartReportByType(body: CartReportByTypeRequest): CartReportByTypeResponse? =
        post<CartReportByTypeRequest, CartReportByTypeResponse>(
            action = Action.CartReportByType,
            payload = body
        ).also {
            if (it == null) {
                Logger.e(tag = TAG, message = {
                    "Cannot make request: userData"
                })
            }
        }

    suspend fun messageList(body: CartMessageListRequest): CartMessageListResponse? = post(
        Action.CartMessageList,
        payload = body
    )

    suspend fun sendMessageToCarts(body: CartMessageSentRequest): BaseResponse? = post(
        action = Action.CartMessageSent,
        payload = body
    )

    suspend fun geofenceList(body: GeofenceListRequest): GeofenceListResponse? = post(
        action = Action.CourseGeofenceList,
        payload = body
    )
}