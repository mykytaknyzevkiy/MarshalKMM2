package com.yama.marshal.repository

import androidx.compose.runtime.mutableStateListOf
import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.CartItem
import com.yama.marshal.data.entity.CartRoundItem
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CartMessageModel
import com.yama.marshal.network.model.request.CartDetailsListRequest
import com.yama.marshal.network.model.request.CartLastLocationRequest
import com.yama.marshal.network.model.request.CartShutdownRequest
import com.yama.marshal.network.model.request.CompanyCartsRoundDetailsRequest
import com.yama.marshal.network.service.DNAService
import com.yama.marshal.network.unit.AuthManager
import com.yama.marshal.network.unit.parseDate
import com.yama.marshal.repository.unit.YamaRepository
import com.yama.marshal.tool.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

internal object CartRepository: YamaRepository() {
    private const val TAG = "CartRepository"

    private val dnaService = DNAService()

    val cartList = Database.cartList
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

    private val _cartMessages = mutableStateListOf<CartMessageModel>()
    val cartMessages: List<CartMessageModel>
        get() = _cartMessages

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

    suspend fun unFlagCart(cartID: Int) {
        Logger.i(TAG, message = { "On un flag cart with id id $cartID" })

        prefs.disCartFlag(cartID)

        Database
            .cartBy(cartID)
            .first()
            ?.also {
                Database.updateCart(it.copy(isFlag = false))
            }
    }

    suspend fun loadUpdateCartsLocation(cartIds: IntArray) {
        val list = dnaService.cartsLocation(
            CartLastLocationRequest(cartIds.toList())
        )?.list ?: return

        for (data in list) {
            if (data.size != 11)
                continue

            val cartID = data[0]?.toIntOrNull() ?: continue

            val lat = data[3]?.toDoubleOrNull() ?: continue
            val lng = data[4]?.toDoubleOrNull() ?: continue

            val hole = data[9]?.toIntOrNull() ?: continue

            val cart = findCart(cartID).first() ?: continue

            if (cart.currPosHole == hole
                && cart.currPosLat == lat
                && cart.currPosLon == lng)
                continue

            val cartRound = Database
                .cartRoundList
                .map { l ->
                    l.findLast { it.id == cartID }
                }
                .first() ?: CartRoundItem(id = cartID)

            cartRound.copy(
                currPosHole = hole,
                currPosLat = lat,
                currPosLon = lng
            ).also {
                Database.addCartRound(it)
            }
        }
    }

    suspend fun shutDown(id: Int) {
        dnaService.cartShutdown(CartShutdownRequest(idCart = id))

        Database
            .cartBy(id)
            .first()
            ?.also {
                Database.updateCart(it.copy(assetControlOverride = 1))
            }
    }

    suspend fun restore(id: Int) {
        dnaService.cartRestore(CartShutdownRequest(idCart = id))

        Database
            .cartBy(id)
            .first()
            ?.also {
                Database.updateCart(it.copy(assetControlOverride = 0))
            }
    }

    fun addCartMessage(message: CartMessageModel) {
        _cartMessages.add(message)
    }

    fun removeCartMessage(index: Int) {
        if (index >= _cartMessages.size)
            return

        _cartMessages.removeAt(index)
    }

    fun findCart(id: Int) = cartList
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