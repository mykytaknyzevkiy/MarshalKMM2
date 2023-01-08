package com.yama.marshal.network.unit

import co.touchlab.kermit.Logger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

abstract class YamaNetworkService(val host: String) {
    companion object {
        const val TAG = "YamaNetworkService"
    }

    val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 100 * 1000
        }
    }

    suspend inline fun <reified P, reified R> post(action: Action, payload: P): R? {
        val url = Url(host + AuthManager.getUrlForAction(action))

        Logger.i(tag = TAG, message = {
            "on post request with url $url and data ${Json.encodeToString(payload)}"
        })

        val response = try {
            client.post(url = url) {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(payload))
            }
        } catch (e: Exception) {
            Logger.e(TAG, message = {
                "Make request $url"
            }, throwable = e)
            return null
        }

        return if (response.status == HttpStatusCode.OK) {
            val resBody = response.bodyAsText()

            Logger.d(TAG, message = {
                "response for action $action response is $resBody"
            })

            try {
                Json {
                    this.allowStructuredMapKeys
                    ignoreUnknownKeys = true
                }.decodeFromString(resBody)
            } catch (e: Exception) {
                Logger.e(tag = TAG, message = {
                    "Error to parse response $resBody"
                }, throwable = e)
                null
            }
        } else {
            Logger.e(tag = TAG, message = {
                "Error to make post request for action: $action, url: $url. HttpStatus ${response.status}"
            })
            null
        }
    }

    suspend inline fun <reified P> postStr(action: Action, payload: P): String? {
        val url = Url(host + AuthManager.getUrlForAction(action))

        Logger.i(tag = TAG, message = {
            "on post str request with url $url and data ${Json.encodeToString(payload)}"
        })

        val response = try {
            client.post(url = url) {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(payload))
            }
        } catch (e: Exception) {
            Logger.e(TAG, message = {
                "Make request $url"
            }, throwable = e)
            return null
        }

        return if (response.status == HttpStatusCode.OK) {
            val resBody = response.bodyAsText()

            Logger.d(TAG, message = {
                "response for action $action response is $resBody"
            })

            resBody
        } else {
            Logger.e(tag = TAG, message = {
                "Error to make post request for action: $action, url: $url. HttpStatus ${response.status}"
            })
            null
        }
    }
}