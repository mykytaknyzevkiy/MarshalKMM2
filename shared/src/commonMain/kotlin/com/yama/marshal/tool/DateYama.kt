package com.yama.marshal.tool

import io.ktor.util.date.*

expect fun GMTDate.format(pattern: String): String

expect fun parseDate(pattern: String, date: String): GMTDate

fun GMTDate.isBeforeDate(date: GMTDate): Boolean {
    return if (this.year < date.year)
        true
    else if (this.month < date.month)
        true
    else this.dayOfMonth < date.dayOfMonth
}