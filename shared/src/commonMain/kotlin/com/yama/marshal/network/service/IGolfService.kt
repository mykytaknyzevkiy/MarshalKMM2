package com.yama.marshal.network.service

import com.yama.marshal.network.model.request.CourseGPSVectorDetailsRequest
import com.yama.marshal.network.unit.Action
import com.yama.marshal.network.unit.YamaNetworkService

class IGolfService : YamaNetworkService("https://api-connect.igolf.com/rest/action/") {

    suspend fun courseVectors(payload: CourseGPSVectorDetailsRequest): String? = postStr(
        action = Action.CourseGPSVectorDetails,
        payload = payload
    )

}