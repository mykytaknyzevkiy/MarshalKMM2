package com.yama.marshal.network

import co.touchlab.kermit.Logger
import com.yama.marshal.network.unit.AuthManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.Foundation.*
import platform.darwin.NSObject
import kotlin.coroutines.CoroutineContext
import socket_IO.SocketIO
import socket_IO.SocketIODelegateProtocol
import socket_IO.SocketIOPacket

actual class MarshalSocket: CoroutineScope, MarshalSocketIO() {
    companion object {
        private const val TAG = "MarshalSocket"
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        onError(throwable.message ?: "Unknown")
    }

    private var socketIO: SocketIO? = null

    private val socketDelegate = object : SocketIODelegateProtocol, NSObject() {
        override fun socketIODidConnect(socket: SocketIO?) {
            this@MarshalSocket.onConnected()
        }

        override fun socketIO(socket: SocketIO?, didReceiveMessage: SocketIOPacket?) {
            if (didReceiveMessage == null) {
                Logger.e(TAG, message = {
                    "Received message packet is null"
                })
                return
            }

            launch {
                didReceiveMessage.packetData?.let {
                    NSJSONSerialization.JSONObjectWithData(it, NSJSONReadingMutableContainers, error = null)
                }?.let {
                    NSJSONSerialization.dataWithJSONObject(
                        it,
                        NSJSONWritingPrettyPrinted,
                        null
                    )
                }?.let {
                    NSString.create(data = it, NSUTF8StringEncoding)
                }?.let {
                    onMessage(it.substringFromIndex(0))
                }
            }
        }

        override fun socketIO(socket: SocketIO?, onError: NSError?) {
            this@MarshalSocket.onError(onError?.localizedDescription ?: "Unknown")
        }
    }

    actual fun connect() = launch {
        socketIO = null

        socketIO = SocketIO(socketDelegate).apply {
            useSecure = true
        }

        socketIO?.connectToHost(
           AuthManager.MARSHAL_NOTIFICATION_ENDPOINT,
           AuthManager.MARSHAL_NOTIFICATION_PORT.toLong()
        )
    }

    actual override fun disconnect() {
        try {
            socketIO?.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        socketIO = null
    }

    actual override fun sendMessage(topic: String, json: String) {
        launch {
            val data = NSString.create(string = json).dataUsingEncoding(
                NSUTF8StringEncoding
            )

            if (data == null) {
                Logger.e(TAG, message = {
                    "Cannot convert json message to NSData"
                })
                return@launch
            }

            val nsDir = NSJSONSerialization.JSONObjectWithData(data, NSJSONReadingMutableContainers, error = null)

            if (nsDir == null) {
                Logger.e(TAG, message = {
                    "Cannot convert data to NSDIR via NSJSONSerialization"
                })
                return@launch
            }

            socketIO?.sendEvent(
                topic,
                nsDir
            )
        }
    }
}