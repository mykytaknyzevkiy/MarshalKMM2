package com.yama.marshal.repository

import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.CartItem
import com.yama.marshal.data.entity.CartReportEntity
import com.yama.marshal.data.entity.CartRoundItem
import com.yama.marshal.data.entity.CourseEntity
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.network.AuthManager
import com.yama.marshal.network.DNAService
import com.yama.marshal.network.model.CartDetailsListRequest
import com.yama.marshal.network.model.CartReportByTypeRequest
import com.yama.marshal.network.model.CompanyCartsRoundDetailsRequest
import com.yama.marshal.network.model.CourseRelationshipListRequest
import com.yama.marshal.tool.companyID
import com.yama.marshal.tool.parseDate
import com.yama.marshal.tool.prefs
import io.ktor.util.date.*
import kotlinx.coroutines.flow.*

class CompanyRepository {
    companion object {
        private const val TAG = "CompanyRepository"
    }

    data class CartFullFlow(
        val carts: List<CartItem>,
        val cartRounds: List<CartRoundItem> ,
        var courseList: List<CourseFullDetail>
    )

    private val dnaService = DNAService()

    suspend fun loadCourses(): Boolean {
        Logger.i(tag = TAG, message = {
            "loadCourses"
        })

        dnaService
            .courseRelationshipList(CourseRelationshipListRequest(prefs.companyID))
            .let {
                if (it == null)
                    return false
                it
            }
            .resultList
            .map {
                CourseEntity(
                    id = it.idCourse,
                    courseName = it.courseName,
                    defaultCourse = it.defaultCourse,
                    playersNumber = it.playersNumber,
                    layoutHoles = it.layoutHoles
                )
            }
            .also {
                Database.updateCourses(it)

                Logger.i(tag = TAG, message = {
                    "courses success saved"
                })
            }

        return true
    }

    suspend fun loadCarts(): Boolean {
        Logger.i(tag = TAG, message = {
            "loadCarts"
        })

        dnaService
            .cartDetailsList(CartDetailsListRequest(prefs.companyID))
            .let {
                if (it == null)
                    return false
                it
            }
            .list
            .map {
                CartItem(
                    id = it.idCart,
                    cartName = it.cartName,
                    idDevice = it.idDevice,
                    idDeviceModel = it.idDeviceModel,
                    cartStatus = it.cartStatus,
                    controllerAccess = it.controllerAccess,
                    assetControlOverride = it.assetControlOverride,
                    lastActivity = it.lastActivity.let { d ->
                        if (d == null)
                            null
                        else
                            parseDate(AuthManager.timeStampDateFormatDataPattern, d)
                    }
                )
            }
            .also {
                Database.updateCarts(it)

                Logger.i(tag = TAG, message = {
                    "carts success saved"
                })
            }

        return true
    }

    suspend fun loadCartsRound(): Boolean {
        dnaService
            .companyCartsRoundDetails(body = CompanyCartsRoundDetailsRequest(idCompany = prefs.companyID))
            .let {
                if (it == null) {
                    Logger.e(TAG, message = {
                        "Error to loadCartsRoundForCourse"
                    })
                    return false
                }
                it
            }
            .list
            .map {
                CartRoundItem(
                    id = it.idAsset,
                    idCourse = it.idCourse,
                    cartName = it.cartName,
                    idDevice = it.idDevice,
                    idTrip = it.idTrip,
                    roundStartTime = it.roundStartTime?.let { t ->
                        parseDate(AuthManager.timeStampDateFormatDataPattern, t)
                    },
                    currPosTime = it.currPosTime,
                    currPosLon = it.currPosLon,
                    currPosLat = it.currPosLat,
                    currPosHole = it.currPosHole,
                    lastValidHole = it.lastValidHole,
                    totalNetPace = it.totalNetPace,
                    totalElapsedTime = it.totalElapsedTime,
                    holesPlayed = it.holesPlayed,
                    assetControlOverride = it.assetControlOverride,
                    onDest = it.onDest
                )
            }
            .also {
                Database.updateCartsRound(it)
            }

        return true
    }

    suspend fun loadCartReport(): Boolean {
        dnaService
            .cartReportByType(
                body = CartReportByTypeRequest(
                    idCompany = prefs.companyID,
                    reportTypeID = 43
                )
            )
            .let {
                if (it == null) {
                    Logger.e(TAG, message = {
                        "Error to loadCartsRoundForCourse"
                    })
                    return false
                }
                it
            }
            .list
            .map {
                CartReportEntity(
                    holeNumber = it.holeNumber,
                    idCourse = it.idCourse,
                    defaultPace = it.defaultPace,
                    averagePace = it.averagePace,
                    differentialPace = it.differentialPace
                )
            }
            .also {
                Database.updateCartReport(it)
            }

        return true
    }

    val courseList = Database
        .courseList
        .combine(Database.cartReport) { a, b ->
            a.map {
                CourseFullDetail(
                    id = it.id,
                    courseName = it.courseName,
                    defaultCourse = it.defaultCourse,
                    playersNumber = it.playersNumber,
                    layoutHoles = it.layoutHoles,
                    holes = b.filter { h -> h.idCourse == it.id }.map { h ->
                        CourseFullDetail.HoleData(
                            holeNumber = h.holeNumber,
                            defaultPace = h.defaultPace,
                            averagePace = h.averagePace,
                            differentialPace = h.differentialPace
                        )
                    }
                )
            }
        }

    val cartsFullDetail = Database
        .cartRoundList
        .combine(Database.cartList) { rounds, carts ->
            CartFullFlow(
                carts = carts,
                cartRounds = rounds,
                courseList = emptyList()
            )
        }
        .combine(courseList) { d, courses ->
            d.apply {
                courseList = courses
            }
        }
        .map { fD ->
            fD.carts.map {
                val cartRound = fD.cartRounds.findLast { c-> c.id == it.id }
                val course = fD.courseList.find { c -> c.id == cartRound?.idCourse }

                CartFullDetail(
                    id = it.id,
                    course = course,
                    cartName = it.cartName,
                    startTime = cartRound?.roundStartTime,
                    currPosTime = cartRound?.currPosTime,
                    currPosLon = cartRound?.currPosLon,
                    currPosLat = cartRound?.currPosLat,
                    currPosHole = cartRound?.currPosHole,
                    totalNetPace = cartRound?.totalNetPace,
                    totalElapsedTime = cartRound?.totalElapsedTime,
                    returnAreaSts = cartRound?.onDest ?: 0,
                    holesPlayed =cartRound?.holesPlayed ?: 0,
                    idTrip = cartRound?.idTrip ?: -1,
                    hasControlAccess = it.controllerAccess == 1,
                    idDeviceModel = it.idDeviceModel ?: 0,
                    assetControlOverride = cartRound?.assetControlOverride,
                    lastActivity = cartRound?.roundStartTime ?: it.lastActivity
                )
            }
        }

    private fun <T> Flow<List<T>>.filterList(predicate: (T) -> Boolean) = this.map {
        it.filter { d ->
            predicate(d)
        }
    }

    private fun <T, R> Flow<List<T>>.mapList(transform: (T) -> R) = this.map { list ->
        list.map {
            transform(it)
        }
    }
}