package com.yama.marshal.data.entity

import com.yama.marshal.tool.isCartFlag
import com.yama.marshal.tool.prefs
import io.ktor.util.date.*

internal data class CartItem(
    val id: Int,
    val cartName: String,
    val idDevice: String?,
    val idDeviceModel: Int?,
    val cartStatus: String?,
    val controllerAccess: Int?,
    val assetControlOverride: Int?,
    val lastActivity: GMTDate?,
    val isFlag: Boolean = prefs.isCartFlag(id)
)

internal data class CartRoundItem(
    val id: Int? = null,
    val idCourse: String? = null,
    val cartName: String? = null,
    val idDevice: String? = null,
    val idTrip: Int? = null,
    val roundStartTime: GMTDate? = null,
    val currPosTime: String? = null,
    val currPosLon: Double? = null,
    val currPosLat: Double? = null,
    val currPosHole: Int? = null,
    val lastValidHole: Int? = null,
    val totalNetPace: Int? = null,
    val totalElapsedTime: Int? = null,
    val holesPlayed: Int? = null,
    val assetControlOverride: Int? = null,
    val onDest: Int? = null
)