package com.yama.marshal.network

import co.touchlab.kermit.Logger
import com.yama.marshal.network.model.MarshalNotification
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.*
import kotlin.coroutines.CoroutineContext

abstract class MarshalSocketIO: CoroutineScope {
    companion object {
        private const val TAG = "MarshalSocket"
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        onError(throwable.message ?: "Unknown")
    }

    private val _onMessage = MutableSharedFlow<String>()
    val onMessage: Flow<String>
        get() = _onMessage

    protected fun onError(message: String) {
        Logger.e(TAG, message = { "onError $message" })
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
}

expect class MarshalSocket() : MarshalSocketIO {

    fun connect(): Job

    fun disconnect()

    fun sendMessage(topic: String, json: String)
}