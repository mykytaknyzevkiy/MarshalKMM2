package com.yama.marshal.network.model.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CompanyCartsRoundDetailsResponse(
    @SerialName("resultList") val list: List<CompanyCartsRoundDetailsItem>
) {
    @kotlinx.serialization.Serializable
    data class CompanyCartsRoundDetailsItem(
        @SerialName("id_course") val idCourse: String? = null,
        @SerialName("id_asset") val idAsset: Int? = null,
        @SerialName("cartName") val cartName: String? = null,
        @SerialName("id_device") val idDevice: String? = null,
        @SerialName("id_trip") val idTrip: Int? = null,
        @SerialName("roundStartTime") val roundStartTime: String? = null,
        @SerialName("currPosTime") val currPosTime: String? = null,
        @SerialName("currPosLon") val currPosLon: Double? = null,
        @SerialName("currPosLat") val currPosLat: Double? = null,
        @SerialName("currPosHole") val currPosHole: Int? = null,
        @SerialName("lastValidHole") val lastValidHole: Int? = null,
        @SerialName("totalNetPace") val totalNetPace: Int? = null,
        @SerialName("totalElapsedTime") val totalElapsedTime: Int? = null,
        @SerialName("holesPlayed") val holesPlayed: Int? = null,
        @SerialName("assetControlOverride") val assetControlOverride: Int? = null,
        @SerialName("onDest") val onDest: Int? = null
    )
}
