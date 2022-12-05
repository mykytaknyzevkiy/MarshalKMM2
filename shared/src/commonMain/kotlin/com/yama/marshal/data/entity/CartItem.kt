package com.yama.marshal.data.entity

import io.ktor.util.date.*

data class CartItem(
    val id: Int,
    val cartName: String,
    val idDevice: String?,
    val idDeviceModel: Int?,
    val cartStatus: String?,
    val controllerAccess: Int?,
    val assetControlOverride: Int?,
    val lastActivity: GMTDate?
)
