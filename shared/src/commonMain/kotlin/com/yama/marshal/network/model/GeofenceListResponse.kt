package com.yama.marshal.network.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class GeofenceListResponse(
    @SerialName("resultList")
    val list: List<GeofenceItem>
)

@kotlinx.serialization.Serializable
data class GeofenceItem(
    @SerialName("id_geofence") val id: Int,
    @SerialName("name") val name: String?,
    @SerialName("id_geofenceType") val type: Int
)
