package com.yama.marshal.network

import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import co.touchlab.kermit.Logger
import com.yama.marshal.network.AuthManager.ApplicationAPIKey
import com.yama.marshal.network.AuthManager.ApplicationSecretKey
import com.yama.marshal.network.AuthManager.MARSHAL_NOTIFICATION_ENDPOINT
import com.yama.marshal.tool.prefs
import com.yama.marshal.tool.secretKey
import com.yama.marshal.tool.userName
import io.socket.IOAcknowledge
import io.socket.IOCallback
import io.socket.SocketIO
import io.socket.SocketIOException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.nio.charset.Charset
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.SSLContext
import kotlin.coroutines.CoroutineContext

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
        login()
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
            SocketIO(MARSHAL_NOTIFICATION_ENDPOINT)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Unknown")
            return@launch
        }

        socketIO?.connect(this@MarshalSocket)
    }

    actual fun disconnect() {
        socketIO?.disconnect()
    }

    actual fun sendMessage(topic: String, json: String) {
        Logger.i(TAG, message = {
            "Send message to $topic with body $json"
        })
        socketIO?.emit(topic, JSONObject(json))
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun login() = launch {
        val userName = prefs.userName
        val userSecretKey = prefs.secretKey

        if (userName == null || userSecretKey == null) {
            onError("userName is null")
            disconnect()
            return@launch
        }

        val signature = makePaddedSignature(
            userName,
            ApplicationSecretKey + userSecretKey
        )

        val payload = NotificationLoginRequest(
            userName,
            ApplicationAPIKey,
            signature
        )

        sendMessage(
            "signAuth",
            Json.encodeToString(payload)
        )
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun makePaddedSignature(stringToSign: String, secret: String): String {
        val mac: Mac = Mac.getInstance("HMACSHA256")
        val sc = secret.toByteArray(Charset.forName("UTF-8"))
        mac.init(SecretKeySpec(sc, mac.algorithm))
        val bt = mac.doFinal(stringToSign.toByteArray(Charset.forName("UTF-8")))
        return net.iharder.Base64.encodeBytes(bt, Base64.URL_SAFE)
    }

}