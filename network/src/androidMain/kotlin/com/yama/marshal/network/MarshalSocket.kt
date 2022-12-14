package com.yama.marshal.network

import co.touchlab.kermit.Logger
import com.yama.marshal.network.MarshalSocketIO
import com.yama.marshal.network.unit.AuthManager.MARSHAL_NOTIFICATION_ENDPOINT
import com.yama.marshal.network.unit.AuthManager.MARSHAL_NOTIFICATION_PORT
import io.socket.IOAcknowledge
import io.socket.IOCallback
import io.socket.SocketIO
import io.socket.SocketIOException
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.net.ssl.SSLContext

actual class MarshalSocket: MarshalSocketIO() {
    companion object {
        private const val TAG = "MarshalSocket"
    }

    private val ioCallback = object : IOCallback {
        override fun onDisconnect() {
            Logger.i(TAG, message = {"onDisconnect"})
            //connect()
        }

        override fun onConnect() {
            Logger.i(TAG, message = {"onConnect"})
            this@MarshalSocket.onConnected()
        }

        override fun onMessage(p0: String?, p1: IOAcknowledge?) {
            onMessage(p0 ?: return)
        }

        override fun onMessage(p0: JSONObject?, p1: IOAcknowledge?) {}

        override fun on(p0: String?, p1: IOAcknowledge?, vararg p2: Any?) {}

        override fun onError(p0: SocketIOException?) {
            try {
                onError(p0?.localizedMessage ?: "Unknown")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var socketIO: SocketIO? = null

    actual fun connect() = launch {
        Logger.i(TAG, message = {"connect"})

        SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault())

        socketIO = try {
            SocketIO("https://$MARSHAL_NOTIFICATION_ENDPOINT:$MARSHAL_NOTIFICATION_PORT")
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Unknown")
            return@launch
        }

        socketIO?.connect(ioCallback)
    }

    actual override fun disconnect() {
        try {
            socketIO?.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual override fun sendMessage(topic: String, json: String) {
        Logger.i(TAG, message = {
            "Send message to $topic with body $json"
        })
        socketIO?.emit(topic, JSONObject(json))
    }
}