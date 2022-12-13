package com.yama.marshal.network.model

import com.yama.marshal.tool.parseDate
import io.ktor.util.date.*
import kotlinx.serialization.json.*

sealed class MarshalNotification(
    open val date: GMTDate,
    open val idCart: Int,
    open val idCourse: String
) {
    companion object {
        internal fun parse(jsonArray: JsonArray): List<MarshalNotification> {
            if (jsonArray.size <= 1)
                return emptyList()

            val notifications = arrayListOf<MarshalNotification>()

            for (jsonElement in jsonArray) {
                parse(jsonElement.jsonObject)?.also {
                    notifications.add(it)
                }
            }

            return notifications
        }

        private fun parse(json: JsonObject): MarshalNotification? {
            val parseTime = { time: String ->
                parseDate("HHmmss", time)
            }

            val date = json["T"]?.jsonPrimitive?.content?.let(parseTime) ?: return null
            val idCart = json["A"]?.jsonPrimitive?.int ?: return null
            val idCourse = json["Cr"]?.jsonPrimitive?.content ?: return null

            return when (json["C"]?.jsonPrimitive?.int) {
                1 -> PaceNotification(
                    currentHole = json["H"]!!.jsonPrimitive.int,
                    currentPace = json["CP"]!!.jsonPrimitive.int,
                    holePace = json["HP"]!!.jsonPrimitive.int,
                    holesPlayed = json["TH"]!!.jsonPrimitive.int,
                    totalPace = json["TP"]!!.jsonPrimitive.int,
                    roundDate = json["RS"]!!.jsonPrimitive.content.let {
                        parseTime(it)
                    },
                    date = date,
                    idCart = idCart,
                    idCourse = idCourse
                )

                2 -> FenceNotification(
                    idFence = json["F"]!!.jsonPrimitive.int,
                    date = date,
                    idCart = idCart,
                    idCourse = idCourse
                )

                3 -> OffPathNotification(
                    date = date,
                    idCart = idCart,
                    idCourse = idCourse,
                    status = json["S"]!!.jsonPrimitive.int
                )

                4 -> BatteryAlertNotification(
                    date = date,
                    idCart = idCart,
                    idCourse = idCourse,
                    level = json["L"]!!.jsonPrimitive.int,
                    threshold = json["L"]!!.jsonPrimitive.int
                )

                5 -> EndTripNotification(
                    date = date,
                    idCart = idCart,
                    idCourse = idCourse
                )

                6 -> MarshalMessageNotification(
                    date = date,
                    idCart = idCart,
                    idCourse = idCourse,
                    message = json["M"]!!.jsonPrimitive.content
                )

                7 -> ReturnAreaNotification(
                    date = date,
                    idCart = idCart,
                    idCourse = idCourse,
                    status = json["S"]!!.jsonPrimitive.int
                )

                else -> null
            }
        }
    }

    data class PaceNotification(
        override val date: GMTDate,
        override val idCart: Int,
        override val idCourse: String,
        val currentHole: Int,
        val currentPace: Int,
        val holePace: Int,
        val holesPlayed: Int,
        val totalPace: Int,
        val roundDate: GMTDate
    ) : MarshalNotification(date, idCart, idCourse)

    data class FenceNotification(
        override val date: GMTDate,
        override val idCart: Int,
        override val idCourse: String,
        val idFence: Int,
    ) : MarshalNotification(date, idCart, idCourse)

    data class OffPathNotification(
        override val date: GMTDate,
        override val idCart: Int,
        override val idCourse: String,
        val status: Int,
    ) : MarshalNotification(date, idCart, idCourse)

    data class BatteryAlertNotification(
        override val date: GMTDate,
        override val idCart: Int,
        override val idCourse: String,
        val level: Int,
        val threshold: Int
    ) : MarshalNotification(date, idCart, idCourse)

    data class EndTripNotification(
        override val date: GMTDate,
        override val idCart: Int,
        override val idCourse: String,
    ) : MarshalNotification(date, idCart, idCourse)

    data class MarshalMessageNotification(
        override val date: GMTDate,
        override val idCart: Int,
        override val idCourse: String,
        val message: String
    ) : MarshalNotification(date, idCart, idCourse)

    data class ReturnAreaNotification(
        override val date: GMTDate,
        override val idCart: Int,
        override val idCourse: String,
        val status: Int
    ) : MarshalNotification(date, idCart, idCourse)
}
