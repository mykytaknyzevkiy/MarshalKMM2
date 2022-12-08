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

    fun cartOfCourse(idCourse: String) = channelFlow {
        flowOf(Database.cartList, courseList, Database.cartRoundList).collectLatest {
            this.channel.send(Database.cartList.value)
        }
    }
        .map { cartsDetail ->
            val course = courseList.first().find { it.id == idCourse }

            val cartsRoundOfCourse =
                Database.cartRoundList.value.filter { it.idCourse == idCourse || idCourse.isEmpty() }

            cartsDetail.let {
                if (idCourse.isNotEmpty())
                    cartsDetail.filter { c -> cartsRoundOfCourse.any { t -> t.id == c.id } }
                else
                    it
            }.map {
                CartFullDetail(
                    id = it.id,
                    course = course ?: CourseFullDetail(id = idCourse, courseName = "All"),
                    cartName = it.cartName,
                    startTime = cartsRoundOfCourse.find { d -> d.id == it.id }?.roundStartTime,
                    currPosTime = cartsRoundOfCourse.find { d -> d.id == it.id }?.currPosTime,
                    currPosLon = cartsRoundOfCourse.find { d -> d.id == it.id }?.currPosLon,
                    currPosLat = cartsRoundOfCourse.find { d -> d.id == it.id }?.currPosLat,
                    currPosHole = cartsRoundOfCourse.find { d -> d.id == it.id }?.currPosHole,
                    totalNetPace = cartsRoundOfCourse.find { d -> d.id == it.id }?.totalNetPace,
                    totalElapsedTime = cartsRoundOfCourse.find { d -> d.id == it.id }?.totalElapsedTime,
                    returnAreaSts = cartsRoundOfCourse.find { d -> d.id == it.id }?.onDest ?: 0,
                    holesPlayed = cartsRoundOfCourse.find { d -> d.id == it.id }?.holesPlayed ?: 0,
                    idTrip = cartsRoundOfCourse.find { d -> d.id == it.id }?.idTrip ?: -1,
                    hasControlAccess = it.controllerAccess == 1,
                    idDeviceModel = it.idDeviceModel ?: 0,
                    assetControlOverride = cartsRoundOfCourse.find { d -> d.id == it.id }?.assetControlOverride
                )
            }
        }
        .filterList {
            it.course.id == idCourse
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