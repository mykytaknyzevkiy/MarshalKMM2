package com.yama.marshal.network.model.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CartReportByTypeResponse(@SerialName("resultList") val list: List<CartReportDataItem>) {
    @kotlinx.serialization.Serializable
    data class CartReportDataItem(
        @SerialName("holeNumber") val holeNumber: Int,
        @SerialName("id_course") val idCourse: String,
        @SerialName("defaultPace") val defaultPace: Int,
        @SerialName("averagePace") val averagePace: Double,
        @SerialName("differentialPace") val differentialPace: Double
    )
}
