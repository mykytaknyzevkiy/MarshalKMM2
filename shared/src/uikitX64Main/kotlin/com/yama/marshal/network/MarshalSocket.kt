package com.yama.marshal.network

import com.yama.marshal.tool.prefs
import com.yama.marshal.tool.secretKey
import com.yama.marshal.tool.userName
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import cocoapods.Marshall.SocketF
actual class MarshalSocket: CoroutineScope, MarshalSocketIO() {
    companion object {
        private const val TAG = "MarshalSocket"
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        onError(throwable.message ?: "Unknown")
    }

    actual fun connect() = launch {
    }

    actual fun disconnect() {
    }

    actual fun sendMessage(topic: String, json: String) {
    }

    private fun login() = launch {
        val userName = prefs.userName
        val userSecretKey = prefs.secretKey

        if (userName == null || userSecretKey == null) {
            onError("userName is null")
            disconnect()
            return@launch
        }

        val signature = /*inCallback?.createSignature(
            userName,
            userSecretKey,
            AuthManager.ApplicationSecretKey
        )*/  ""

        if (signature == null) {
            onError("signature is null")
            disconnect()
            return@launch
        }

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