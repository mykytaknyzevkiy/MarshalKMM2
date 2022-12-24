package com.yama.marshal.network.service

import com.yama.marshal.network.unit.Action
import com.yama.marshal.network.unit.YamaNetworkService
import com.yama.marshal.network.model.request.CourseGPSVectorDetailsRequest

class IGolfService : YamaNetworkService("https://api-connect.igolf.com/rest/action/") {

    suspend fun courseVectors(payload: CourseGPSVectorDetailsRequest): String? = postStr(
        action = Action.CourseGPSVectorDetails,
        payload = payload
    )

}