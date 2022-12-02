package com.yama.marshal.tool

import io.ktor.util.date.*
import java.text.SimpleDateFormat
import java.util.*

actual fun GMTDate.format(pattern: String): String {
    val date = Date(this.timestamp)

    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}