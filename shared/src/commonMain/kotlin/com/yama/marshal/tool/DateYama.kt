package com.yama.marshal.tool

import io.ktor.util.date.*

expect fun GMTDate.format(pattern: String): String

expect fun parseDate(pattern: String, date: String): GMTDate

fun GMTDate.isBeforeDate(date: GMTDate): Boolean {
    return if (this.year < date.year)
        false
    else if (this.month < date.month)
        false
    else this.dayOfMonth >= date.dayOfMonth
}