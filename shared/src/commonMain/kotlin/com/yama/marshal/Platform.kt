package com.yama.marshal

internal enum class MPlatform {
    IOS,
    ANDROID
}

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

internal expect val mPlatform: MPlatform