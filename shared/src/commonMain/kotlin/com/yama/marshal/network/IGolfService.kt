package com.yama.marshal.network

import com.yama.marshal.network.model.CourseGPSVectorDetailsRequest
import com.yama.marshal.network.model.CourseGPSVectorDetailsResponse

class IGolfService : YamaNetworkService("https://api-connect.igolf.com/rest/action/") {

    suspend fun courseVectors(payload: CourseGPSVectorDetailsRequest): String? = postStr(
        action = Action.CourseGPSVectorDetails,
        payload = payload
    )

}