package com.yama.marshal.network.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CompanyCartsRoundDetailsRequest(@SerialName("id_company") val idCompany : Int)
