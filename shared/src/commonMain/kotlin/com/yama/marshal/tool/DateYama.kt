package com.yama.marshal.tool

import io.ktor.util.date.*

expect fun GMTDate.format(pattern: String): String

expect fun parseDate(pattern: String, date: String): GMTDate