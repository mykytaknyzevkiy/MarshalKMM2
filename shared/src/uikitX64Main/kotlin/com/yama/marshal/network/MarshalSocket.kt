package com.yama.marshal.network

import cocoapods.Ios.IGolfSocket
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

actual class MarshalSocket: CoroutineScope, MarshalSocketIO() {
    companion object {
        private const val TAG = "MarshalSocket"
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        onError(throwable.message ?: "Unknown")
    }

    private val iosSocket = IGolfSocket()

    actual fun connect() = launch {
        iosSocket.connectWithUrl(
            url = AuthManager.MARSHAL_NOTIFICATION_ENDPOINT,
            port = AuthManager.MARSHAL_NOTIFICATION_PORT.toLong()
        )
    }

    actual override fun disconnect() {
    }

    actual override fun sendMessage(topic: String, json: String) {
        iosSocket.sendEventWithEvent(topic, json)
    }
}