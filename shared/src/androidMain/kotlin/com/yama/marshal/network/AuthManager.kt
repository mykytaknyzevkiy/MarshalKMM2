package com.yama.marshal.network

import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@RequiresApi(Build.VERSION_CODES.FROYO)
internal actual fun makeSignature(src: String, secret: String): String {
    val CHARACTER_ENCODING = "UTF-8"
    var res = ""
    try {
        val mac = Mac.getInstance("HMACSHA256")
        val sc = secret.toByteArray(charset(CHARACTER_ENCODING))
        mac.init(SecretKeySpec(sc, mac.algorithm))
        val bt = mac.doFinal(src.toByteArray(charset(CHARACTER_ENCODING)))
        res = Base64.encodeToString(bt, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        res = res.replace('+', '-').replace('/', '_')
    } catch (e: NoSuchAlgorithmException) {
    } catch (e: InvalidKeyException) {
    } catch (e: UnsupportedEncodingException) {
    }

    return res
}