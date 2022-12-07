package com.yama.marshal.network

import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.FROYO)
internal actual fun toBase64Url(bt: ByteArray): String = Base64.encodeToString(bt, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)