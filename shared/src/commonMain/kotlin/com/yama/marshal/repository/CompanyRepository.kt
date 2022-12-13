package com.yama.marshal.repository

import androidx.compose.runtime.mutableStateListOf
import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.*
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.network.AuthManager
import com.yama.marshal.network.DNAService
import com.yama.marshal.network.model.*
import com.yama.marshal.tool.companyID
import com.yama.marshal.tool.parseDate
import com.yama.marshal.tool.prefs
import com.yama.marshal.tool.setCartFlag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

object CompanyRepository {
    private const val TAG = "CompanyRepository"

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

                Logger.i(tag = TAG, message = {
                    "carts success saved"
                })
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
                HoleEntity(
                    id = it.holeNumber,
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

    suspend fun loadMessages(): Boolean {
        dnaService
            .messageList(CartMessageListRequest(idCompany = prefs.companyID, active = 1))
            .let {
                if (it == null) {
                    Logger.e(TAG, message = {
                        "Cannot loadMessages"
                    })
                    return false
                }
                it
            }
            .list
            .map {
                CompanyMessage(
                    id = it.id,
                    message = it.message
                )
            }
            .also {
                Database.updateCompanyMessages(it)
            }

        return true
    }

    suspend fun sendMessageToCarts(cartIds: IntArray, idMessage: Int): Boolean {
        dnaService.sendMessageToCarts(
            body = CartMessageSentRequest(
                cartIds = cartIds.toList(),
                idMessage = idMessage,
                customMessage = null
            )
        ).also {
            return it != null
        }
    }

    suspend fun sendMessageToCarts(cartIds: IntArray, message: String): Boolean {
        dnaService.sendMessageToCarts(
            body = CartMessageSentRequest(
                cartIds = cartIds.toList(),
                idMessage = null,
                customMessage = message
            )
        ).also {
            return it != null
        }
    }

    suspend fun processNotification(paceNotification: MarshalNotification.PaceNotification) {

    }

    fun flagCart(cartID: Int) {
        Logger.i(TAG, message = { "On flag cart with id id $cartID" })

        prefs.setCartFlag(cartID)

        val index = _cartsFullDetail.indexOfFirst { it.id == cartID }

        if (index < 0) {
            Logger.e(TAG, message = {"Cannot find cart by id $cartID to flag cart"})
            return
        }

        _cartsFullDetail[index] = _cartsFullDetail[index].copy(
            isFlag = true
        )
    }

    fun launchCartsUpdater(scope: CoroutineScope) {
        Database.cartList
            .onEachList { cart ->
                if (_cartsFullDetail.any { it.id == cart.id }) {
                    val oldCart = _cartsFullDetail.find { it.id == cart.id } ?: return@onEachList
                    val realIndex = _cartsFullDetail.indexOf(oldCart)

                    if (!oldCart.isContentEqual(cart)) {
                        Logger.i(TAG, message = { "onUpdate cart id ${oldCart.id} by cartList callback" })

                        _cartsFullDetail[realIndex] = oldCart.copy(
                            cartName = cart.cartName,
                            hasControlAccess = cart.controllerAccess == 1,
                            idDeviceModel = cart.idDeviceModel ?: 0,
                            controllerAccess = cart.controllerAccess ?: 0,
                            lastActivity = cart.lastActivity
                        )
                    }
                }
                else {
                    Logger.i(TAG, message = { "on add cart id ${cart.id} by cartList callback" })

                    _cartsFullDetail.add(
                        CartFullDetail(
                            id = cart.id,
                            course = null,
                            cartName = cart.cartName,
                            startTime = null,
                            currPosTime = null,
                            currPosLon = null,
                            currPosLat = null,
                            currPosHole = null,
                            totalNetPace = null,
                            totalElapsedTime = null,
                            returnAreaSts = 0,
                            holesPlayed = 0,
                            idTrip = -1,
                            hasControlAccess = cart.controllerAccess == 1,
                            idDeviceModel = cart.idDeviceModel ?: 0,
                            assetControlOverride = null,
                            lastActivity = cart.lastActivity,
                            controllerAccess = cart.controllerAccess ?: 0
                        )
                    )
                }
            }
            .launchIn(scope)

        Database.cartRoundList
            .combine(courseList) {a, b -> Pair(a, b)}
            .onEach { data ->
                for (cartRound in data.first) {
                    val oldCart = _cartsFullDetail.findLast { it.id == cartRound.id }

                    if (oldCart == null) {
                        Logger.e(TAG, message = { "Cannot find cart id ${cartRound.id} by cart round callback" })
                        continue
                    }

                    val index = _cartsFullDetail.indexOf(oldCart)

                    val course = data.second.find { c -> c.id == cartRound.idCourse }

                    Logger.i(TAG, message = { "onUpdate cart id ${oldCart.id} by cart round callback" })

                    _cartsFullDetail[index] = oldCart.copy(
                        course = course,
                        startTime = cartRound.roundStartTime,
                        currPosTime = cartRound.currPosTime,
                        currPosLon = cartRound.currPosLon,
                        currPosLat = cartRound.currPosLat,
                        currPosHole = cartRound.currPosHole,
                        totalNetPace = cartRound.totalNetPace,
                        totalElapsedTime = cartRound.totalElapsedTime,
                        returnAreaSts = cartRound.onDest ?: 0,
                        holesPlayed = cartRound.holesPlayed ?: 0,
                        idTrip = cartRound.idTrip ?: -1,
                        assetControlOverride = cartRound.assetControlOverride,
                    )
                }
            }
            .launchIn(scope)
    }

    val holeList = Database.cartReport

    val courseList = Database
        .courseList
        .combine(holeList) { a, b ->
            a.map {
                CourseFullDetail(
                    id = it.id,
                    courseName = it.courseName,
                    defaultCourse = it.defaultCourse,
                    playersNumber = it.playersNumber,
                    layoutHoles = it.layoutHoles,
                    holes = b.filter { h -> h.idCourse == it.id }.map { h ->
                        CourseFullDetail.HoleData(
                            holeNumber = h.id,
                            defaultPace = h.defaultPace,
                            averagePace = h.averagePace,
                            differentialPace = h.differentialPace
                        )
                    }
                )
            }
        }

    val companyMessages = Database
        .companyMessages
        .filterList {
            it.message.isNotBlank()
        }

    private val _cartsFullDetail = mutableStateListOf<CartFullDetail>()
    val cartsFullDetail: List<CartFullDetail>
        get() = _cartsFullDetail
}

fun <T> Flow<List<T>>.filterList(predicate: (T) -> Boolean) = this.map {
    it.filter { d ->
        predicate(d)
    }
}

fun <T, R> Flow<List<T>>.mapList(transform: (T) -> R) = this.map { list ->
    list.map {
        transform(it)
    }
}

fun <T> Flow<List<T>>.onEachList(action: (T) -> Unit) = this.onEach { c ->
    c.onEach(action)
}