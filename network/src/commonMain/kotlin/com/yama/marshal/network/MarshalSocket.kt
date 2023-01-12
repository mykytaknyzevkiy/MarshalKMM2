package com.yama.marshal.network

import co.touchlab.kermit.Logger
import com.appmattus.crypto.Algorithm
import com.yama.marshal.network.model.request.MarshalNotification
import com.yama.marshal.network.unit.AuthManager
import com.yama.marshal.network.unit.Base64
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.coroutines.CoroutineContext

@kotlinx.serialization.Serializable
internal class NotificationLoginRequest(@SerialName("username") val userName: String,
                               @SerialName("apikey") val apiKey: String,
                               @SerialName("signature") val signature: String)

abstract class MarshalSocketIO: CoroutineScope {
    companion object {
        private const val TAG = "MarshalSocket"
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        try {
            onError(throwable.message ?: "Unknown")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _onMessage = MutableSharedFlow<String>()
    val onMessage: Flow<List<MarshalNotification>>
        get() = _onMessage
            .map {
                val responseJson = try {
                    Json.parseToJsonElement(it)
                } catch (e: Exception) {
                    Logger.e(TAG, message = { "Parse responseJson to jsonElement" }, throwable = e)
                    throw Exception("Parse json")
                }

                val jsonArray = try {
                    responseJson.jsonArray
                } catch (e: Exception) {
                    throw Exception("Get json array")
                }

                MarshalNotification.parse(jsonArray)
            }

    protected fun onError(message: String) {
        Logger.e(TAG, message = { "onError $message" })

        disconnect()
    }

    protected fun onMessage(json: String) = launch {
        Logger.i(TAG, message = { "onMessage $json" })

        val responseJson = try {
            Json.parseToJsonElement(json)
        } catch (e: Exception) {
            onError(message = "Parse responseJson to jsonElement ${e.message}")
            return@launch
        }

        val jsonObject = try {
            responseJson.jsonObject
        } catch (e: Exception) {
            null
        }
        val jsonArray = try {
            responseJson.jsonArray
        } catch (e: Exception) {
            null
        }

        if (jsonObject != null) {
            Logger.i(TAG, message = { "Parse json object" })
            val sts = jsonObject.getValue("sts").jsonPrimitive.int
            if (sts == 1)
                Logger.i(TAG, message = { "Login success" })
            else
                Logger.e(TAG, message = { "Login fault" })
        }
        else if (jsonArray != null)
            _onMessage.emit(json)
        else
            Logger.e(TAG, message = { "Cannot parse json" })
    }

    abstract fun disconnect()

    abstract fun sendMessage(topic: String, json: String)

    protected fun onConnected() {
        Logger.i(TAG, message = {
            "onConnected"
        })

        login()
    }

    private fun login() = launch {
        val userName = AuthManager.userName
        val userSecretKey = AuthManager.userSecret

        if (userName == null || userSecretKey == null) {
            onError("userName is null")
            disconnect()
            return@launch
        }

        val charSet = Charset.forName("UTF-8")

        val signature = Algorithm
            .SHA_256
            .createHmac(key = (AuthManager.ApplicationSecretKey + userSecretKey).toByteArray(charset = charSet))
            .digest(userName.toByteArray(charSet))
            .let {
                io.ktor.utils.io.core.String(Base64.encoderPadding.encode(it))
            }
            .replace('+', '-').replace('/', '_')

        val payload = NotificationLoginRequest(
            userName,
            AuthManager.ApplicationAPIKey,
            signature
        )

        sendMessage(
            "signAuth",
            Json.encodeToString(payload)
        )
    }
}

expect class MarshalSocket() : MarshalSocketIO {

    fun connect(): Job

    override fun disconnect()

    override fun sendMessage(topic: String, json: String)
}