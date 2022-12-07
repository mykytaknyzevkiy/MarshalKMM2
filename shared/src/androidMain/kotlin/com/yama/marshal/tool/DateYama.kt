package com.yama.marshal.tool

import io.ktor.util.date.*
import java.text.SimpleDateFormat
import java.util.*

actual fun GMTDate.format(pattern: String): String {
    val date = Date(this.timestamp)

    return SimpleDateFormat(pattern, Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }.format(date)
}

actual fun parseDate(pattern: String, date: String): GMTDate {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val dateN = formatter.parse(date) ?: Date()

    return  GMTDate(dateN.time)
}