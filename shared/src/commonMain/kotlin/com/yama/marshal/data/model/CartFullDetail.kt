package com.yama.marshal.data.model

import io.ktor.util.date.*

internal data class CartFullDetail(
    val id: Int,
    val course: CourseFullDetail?,
    val currentCourseID: String?,
    val cartName: String,
    val startTime: GMTDate? = null,
    val currPosTime: String? = null,
    val currPosLon: Double? = null,
    val currPosLat: Double? = null,
    val currPosHole: Int? = null,
    val totalNetPace: Int? = null,
    val totalElapsedTime: Int? = null,
    val returnAreaSts: Int,
    val holesPlayed: Int,
    val idTrip: Int,
    val hasControlAccess: Boolean,
    val idDeviceModel: Int,
    val assetControlOverride: Int?,
    val lastActivity: GMTDate?,
    val controllerAccess: Int,
    val isFlag: Boolean,
    val hole: CourseFullDetail.HoleData?
) {
    enum class State {
        inUse,
        notInUse
    }

    val state: State
        get() {
            return if (idTrip != -1 && startTime != null)
                State.inUse
            else
                State.notInUse
        }

    val isOnClubHouse: Boolean
        get() {
            course?.holes?.size?.also { totalHoles ->
                holesPlayed.also { holesPlayed ->
                    if (totalHoles != 0) {
                        val progress = holesPlayed.toFloat() / totalHoles.toFloat()
                        return progress < 0.65
                    }
                }
            }
            return false
        }

    val isCartInShutdownMode: Boolean
        get() = listOf(1, 2).contains(assetControlOverride)

    val isMessagingAvailable: Boolean
        get() = listOf(3, 6, 8).contains(idDeviceModel)

    val isShutdownEnable: Boolean
        get() = controllerAccess > 0
}
