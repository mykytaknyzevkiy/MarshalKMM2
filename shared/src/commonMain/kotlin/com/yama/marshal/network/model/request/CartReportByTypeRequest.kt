package com.yama.marshal.network.model.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CartReportByTypeRequest(
    @SerialName("id_company") val idCompany: Int,
    @SerialName("id_reporttype") val reportTypeID: Int
)
