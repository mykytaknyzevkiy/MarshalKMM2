package com.yama.marshal.tool

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

internal expect val prefs: Settings

internal var Settings.userName: String?
    get() = this.getStringOrNull("userName")
    set(value) = this.set("userName", value)

internal var Settings.secretKey: String?
    get() = this.getStringOrNull("secretKey")
    set(value) = this.set("secretKey", value)

internal var Settings.userID: Int
    get() = this.getInt("userID", 0)
    set(value) = this.set("userID", value)