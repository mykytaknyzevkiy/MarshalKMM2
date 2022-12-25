package com.yama.marshal.network

import cocoapods.Ios.IGolfSocket
import cocoapods.Ios.SocketManagerDelegateProtocol
import com.yama.marshal.network.unit.AuthManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.darwin.NSObject
import kotlin.coroutines.CoroutineContext

actual class MarshalSocket: CoroutineScope, MarshalSocketIO() {
    companion object {
        private const val TAG = "MarshalSocket"
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        onError(throwable.message ?: "Unknown")
    }

    private val socketDelegate = object : SocketManagerDelegateProtocol, NSObject() {
        override fun didConnected() {
            this@MarshalSocket.onConnected()
        }

        override fun onErrorWithError(error: String) {
            this@MarshalSocket.onError(error)
        }

        override fun onMessageWithMessage(message: String) {
            this@MarshalSocket.onMessage(message)
        }
    }

    private val iosSocket = IGolfSocket().apply {
        this.setDelegateWithDelegate(socketDelegate)
    }

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