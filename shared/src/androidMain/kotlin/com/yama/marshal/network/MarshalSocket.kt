package com.yama.marshal.network

import android.os.Build
import androidx.annotation.RequiresApi
import co.touchlab.kermit.Logger
import com.yama.marshal.network.unit.AuthManager.MARSHAL_NOTIFICATION_ENDPOINT
import com.yama.marshal.network.unit.AuthManager.MARSHAL_NOTIFICATION_PORT
import io.ktor.utils.io.core.*
import io.socket.IOAcknowledge
import io.socket.IOCallback
import io.socket.SocketIO
import io.socket.SocketIOException
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.net.ssl.SSLContext

@RequiresApi(Build.VERSION_CODES.GINGERBREAD)
actual class MarshalSocket:MarshalSocketIO(), IOCallback {
    companion object {
        private const val TAG = "MarshalSocket"
    }

    override fun onDisconnect() {
        Logger.i(TAG, message = {"onDisconnect"})
        connect()
    }

    override fun onConnect() {
        Logger.i(TAG, message = {"onConnect"})
        this.onConnected()
    }

    override fun onMessage(p0: String?, p1: IOAcknowledge?) {
        onMessage(p0 ?: return)
    }

    override fun onMessage(p0: JSONObject?, p1: IOAcknowledge?) {}

    override fun on(p0: String?, p1: IOAcknowledge?, vararg p2: Any?) {}

    override fun onError(p0: SocketIOException?) {
        onError(p0?.localizedMessage ?: "Unknown")
    }

    private var socketIO: SocketIO? = null

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    actual fun connect() = launch {
        Logger.i(TAG, message = {"connect"})

        SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault())

        socketIO = try {
            SocketIO("https://$MARSHAL_NOTIFICATION_ENDPOINT:$MARSHAL_NOTIFICATION_PORT")
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Unknown")
            return@launch
        }

        socketIO?.connect(this@MarshalSocket)
    }

    actual override fun disconnect() {
        socketIO?.disconnect()
    }

    actual override fun sendMessage(topic: String, json: String) {
        Logger.i(TAG, message = {
            "Send message to $topic with body $json"
        })
        socketIO?.emit(topic, JSONObject(json))
    }
}