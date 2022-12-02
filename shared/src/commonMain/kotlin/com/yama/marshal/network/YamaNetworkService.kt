package com.yama.marshal.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

abstract class YamaNetworkService(val host: String) {
    val client = HttpClient()

    suspend inline fun <reified P, reified R> post(action: Action, payload: P): R? {
        val response = client.post(url = Url(host + AuthManager.getUrlForAction(action))) {
            contentType(ContentType.Application.Json)

            val payloadJson = Json.encodeToString(payload)

            setBody(payload)
        }

        return if (response.status == HttpStatusCode.OK) {
            response.body<R>()
        } else
            null
    }
}