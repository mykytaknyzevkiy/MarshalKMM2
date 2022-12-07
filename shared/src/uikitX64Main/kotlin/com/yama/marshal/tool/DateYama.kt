package com.yama.marshal.tool

import io.ktor.util.date.*
import platform.Foundation.*

actual fun GMTDate.format(pattern: String): String {
    val date = NSDate(this.timestamp.toDouble())

    val dateFormatter = NSDateFormatter().apply {
        dateFormat = pattern
    }

    return dateFormatter.stringFromDate(date)
}

actual fun parseDate(pattern: String, date: String): GMTDate {
    return NSDateFormatter().apply {
        dateFormat = pattern
    }.dateFromString(date).let {
        if (it == null)
            GMTDate()
        else
            GMTDate(it.timeIntervalSince1970.toLong())
    }
}