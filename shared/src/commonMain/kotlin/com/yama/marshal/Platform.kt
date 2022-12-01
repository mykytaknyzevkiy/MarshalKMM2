package com.yama.marshal

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform