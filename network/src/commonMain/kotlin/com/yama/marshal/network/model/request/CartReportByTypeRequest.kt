package com.yama.marshal.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartReportByTypeRequest(
    @SerialName("id_company") val idCompany: Int,
    @SerialName("id_reporttype") val reportTypeID: Int,
    @SerialName("active") val active: Int
)
