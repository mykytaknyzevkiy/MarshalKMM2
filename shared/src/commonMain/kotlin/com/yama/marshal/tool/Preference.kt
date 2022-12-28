package com.yama.marshal.tool

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.ktor.util.date.*

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

internal var Settings.companyID: Int
    get() = this.getInt("companyID", 0)
    set(value) = this.set("companyID", value)

internal fun Settings.isCartFlag(cartID: Int): Boolean = this
    .getLong("cart_flag_time_$cartID", 0)
    .let {
        GMTDate().timestamp - it < 30 * 60 * 1000
    }

internal fun Settings.setCartFlag(cartID: Int) {
    this["cart_flag_time_$cartID"] = GMTDate().timestamp
}

internal fun Settings.disCartFlag(cartID: Int) {
    this["cart_flag_time_$cartID"] = 0
}