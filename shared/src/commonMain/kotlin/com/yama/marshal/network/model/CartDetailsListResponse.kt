package com.yama.marshal.network.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CartDetailsListResponse(
    @SerialName("resultList") val list: List<CartDetailsListItem>
) {
    @kotlinx.serialization.Serializable
    data class CartDetailsListItem(
        @SerialName("cartName") val cartName: String,
        @SerialName("id_device") val idDevice: String? = null,
        @SerialName("id_cart") val idCart: Int,
        @SerialName("id_deviceModel") val idDeviceModel: Int? = null,
        @SerialName("cartStatus") val cartStatus: String? = null,
        @SerialName("controllerAccess") val controllerAccess: Int?,
        @SerialName("assetControlOverride") val assetControlOverride: Int? = null,
        @SerialName("lastActivity") val lastActivity: String? = null
    )
}
