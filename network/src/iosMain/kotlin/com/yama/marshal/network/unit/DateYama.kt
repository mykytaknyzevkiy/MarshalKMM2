package com.yama.marshal.network.unit

import io.ktor.util.date.*
import platform.Foundation.*

actual fun GMTDate.format(pattern: String): String {
    val date = NSDate(
        timestamp / 1000.0 - 978307200
    )

    val dateFormatter = NSDateFormatter().apply {
        dateFormat = pattern
    }

    return dateFormatter.stringFromDate(date)
}

actual fun parseDate(pattern: String, date: String): GMTDate {
    return NSDateFormatter().apply {
        dateFormat = pattern
        this.timeZone = NSTimeZone.create("UTC")!!
    }.dateFromString(date).let {
        if (it == null)
            GMTDate()
        else {
            GMTDate(it.timeIntervalSince1970.toLong() * 1000)
        }
    }
}