package com.yama.marshal.repository

import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.CartItem
import com.yama.marshal.data.entity.CartRoundItem
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.network.model.request.CartDetailsListRequest
import com.yama.marshal.network.model.request.CompanyCartsRoundDetailsRequest
import com.yama.marshal.network.service.DNAService
import com.yama.marshal.network.unit.AuthManager
import com.yama.marshal.repository.unit.YamaRepository
import com.yama.marshal.tool.*
import io.ktor.util.date.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

object CartRepository: YamaRepository() {
    private const val TAG = "CartRepository"

    private val dnaService = DNAService()

    val cartActiveList = Database.cartList
        .filterList { it.lastActivity?.isBeforeDate(GMTDate()) == false }
        .combine(Database.cartRoundList) { cartsList, reportList ->
            cartsList.map { cart ->
                val cartRound = reportList.findLast { it.id == cart.id}

                CartFullDetail(
                    id = cart.id,
                    course = null,
                    cartName = cart.cartName,

                    startTime = cartRound?.roundStartTime,

                    currentCourseID = cartRound?.idCourse,

                    currPosTime = cartRound?.currPosTime,
                    currPosLon = cartRound?.currPosLon,
                    currPosLat = cartRound?.currPosLat,
                    currPosHole = cartRound?.currPosHole,

                    totalNetPace = cartRound?.totalNetPace,
                    totalElapsedTime = cartRound?.totalElapsedTime,

                    returnAreaSts = cartRound?.onDest ?: 0,

                    holesPlayed = cartRound?.holesPlayed ?: 0,

                    idTrip = cartRound?.idTrip ?: -1,

                    assetControlOverride = cartRound?.assetControlOverride,
                    hasControlAccess = cart.controllerAccess == 1,

                    idDeviceModel = cart.idDeviceModel ?: 0,

                    lastActivity = cart.lastActivity,
                    controllerAccess = cart.controllerAccess ?: 0,
                    hole = null,

                    isFlag = cart.isFlag
                )
            }
        }
        .combine(CourseRepository.courseList) { carts, courses ->
            carts.map { cart ->
                if (cart.currentCourseID == null)
                    cart
                else {
                    val course = courses.find { it.id == cart.currentCourseID }

                    cart.copy(
                        course = course,
                        hole = course?.holes?.find { it.holeNumber == cart.currPosHole }
                    )
                }
            }
        }
        .flowOn(Dispatchers.Default)

    suspend fun loadCarts(): Boolean {
        Logger.i(tag = TAG, message = {
            "loadCarts"
        })

        dnaService
            .cartDetailsList(CartDetailsListRequest(prefs.companyID, 1, 1))
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
            }

        return true
    }

    suspend fun loadCartsRound(): Boolean {
        Logger.i(tag = TAG, message = {
            "loadCartsRound"
        })

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

                Logger.i(tag = TAG, message = {
                    "carts round success saved"
                })
            }

        return true
    }

    suspend fun flagCart(cartID: Int) {
        Logger.i(TAG, message = { "On flag cart with id id $cartID" })

        prefs.setCartFlag(cartID)

        Database
            .cartBy(cartID)
            .first()
            ?.also {
                Database.updateCart(it.copy(isFlag = true))
            }
    }

    fun findCart(id: Int) = cartActiveList
        .map { l ->
            l.find { it.id == id }
        }
        .onEach {
            if (it == null) {
                loadCarts()
                loadCartsRound()
            }
        }
}