package com.yama.marshal

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

internal actual val mPlatform: MPlatform = MPlatform.ANDROID