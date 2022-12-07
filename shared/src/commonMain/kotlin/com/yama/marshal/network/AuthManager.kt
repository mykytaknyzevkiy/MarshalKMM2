package com.yama.marshal.network

import co.touchlab.kermit.Logger
import com.appmattus.crypto.Algorithm
import com.yama.marshal.tool.prefs
import com.yama.marshal.tool.secretKey
import com.yama.marshal.tool.userName
import io.ktor.util.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*

object AuthManager {
    private const val slash = "/"
    private const val APIVersion = "1.0"
    private const val SignatureVersion = "2.0"
    private const val SignatureMethod = "HmacSHA256"
    private const val ResponseFormat = "JSON"
    private const val ApplicationAPIKey = "uUqnXUKU86kghJk"
    private const val ApplicationSecretKey = "3CA0LPbu3FlXRin0UCH05rM/ZzvCkK"
    private const val timeStampDateFormatPattern = "yyMMddHHmmssZZZZ"
    const val timeStampDateFormatDataPattern = "yyMMddHHmmss"

    fun getUrlForAction(action: Action): String {
        val urlBuilder = StringBuilder()
        val urlEnd = StringBuilder()

        urlBuilder
            .append(action.actionName)
            .append(slash)
            .append(ApplicationAPIKey)
            .append(slash)

        if (action.isPrivate) {
            urlBuilder.append(prefs.userName)
            urlBuilder.append(slash)
        }

        urlBuilder
            .append(APIVersion)
            .append(slash)
            .append(SignatureVersion)
            .append(slash)
            .append(SignatureMethod)
            .append(slash)

        urlEnd
            .append(getTimestamp())
            .append(slash)
            .append(ResponseFormat)

        val signature = makeSignature(
            urlBuilder.toString() + urlEnd.toString(),
            if (action.isPrivate) ApplicationSecretKey + prefs.secretKey else ApplicationSecretKey
        )

        urlBuilder
            .append(signature)
            .append(slash)
            .append(urlEnd)

        return urlBuilder.toString()
    }

    private fun getTimestamp(): String {
        return /*GMTDate().format(timeStampDateFormatPattern)*/ "221202200656GMT+02:00"
    }
}

internal fun makeSignature(src: String, secret: String): String {
    val charSet = Charset.forName("UTF-8")

    return Algorithm
        .SHA_256
        .createHmac(key = secret.toByteArray(charset = charSet))
        .digest(src.toByteArray(charSet))
        .let {
            toBase64Url(it)
        }
        .replace('+', '-').replace('/', '_')
}

internal expect fun toBase64Url(bt: ByteArray): String

enum class Action(val actionName: String, val isPrivate: Boolean) {
    NONE("", true),
    UserLogin("UserAccountLogin", false),
    UserAccountPasswordReset("UserAccountPasswordReset", false),
    UserDetails("UserAccountDetails", true),
    CompanyProfileDetails("CompanyProfileDetails", true),
    CourseRelationshipList("CourseRelationshipList", true),
    CartDetailsList("CartDetailsList", true),
    CompanyCartsRoundDetails("CompanyCartsRoundDetails", true),
    CartReportByType("CartReportByType", true),
    CourseGeofenceDetails("CourseGeofenceDetails", true),
    CartShutdown("CartShutdown", true),
    CartControlRestore("CartControlRestore", true),
    CartMessageList("CartMessageList", true),
    CartMessageSent("CartMessageSend", true),
    CartLastLocation("CartLastLocation", true),
    CourseGPSVectorDetails("CourseGPSVectorDetails", false),
    CartDetailsUpdate("CartDetailsUpdate", true)
}
