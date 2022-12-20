package com.yama.marshal.repository

import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.*
import com.yama.marshal.data.entity.GeofenceItem
import com.yama.marshal.data.model.AlertModel
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.network.AuthManager
import com.yama.marshal.network.DNAService
import com.yama.marshal.network.model.*
import com.yama.marshal.tool.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.Synchronized

object CompanyRepository: CoroutineScope {
    data class CartFullFlow(
        val carts: List<CartItem>,
        val cartRounds: List<CartRoundItem> ,
        var courseList: List<CourseFullDetail>
    )

    private const val TAG = "CompanyRepository"

    override val coroutineContext: CoroutineContext = Dispatchers.Default

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

    suspend fun loadGeofenceList(): Boolean {
        dnaService
            .geofenceList(GeofenceListRequest(idCompany = prefs.companyID, isActive = 1))
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
                GeofenceItem(
                    id = it.id,
                    name = it.name,
                    type = it.type
                )
            }
            .also {
                Database.updateGeofenceList(it)
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

    @Synchronized
    fun processNotification(paceNotification: MarshalNotification.PaceNotification) {
        Logger.i(TAG, message = {
            "process PaceNotification $paceNotification"
        })

        val idCart = paceNotification.idCart

        val cart = _cartsFullDetail.find { it.id == idCart }

        if (cart == null) {
            Logger.e(TAG, message = {
                "Cannot find cart with id $idCart"
            })
            return
        }

        val index = _cartsFullDetail.indexOf(cart)

        if (cart.currPosHole == paceNotification.currentHole
            && cart.holesPlayed == paceNotification.holesPlayed
            && cart.totalNetPace == paceNotification.totalPace
            && cart.startTime == paceNotification.roundDate)
            return

        _cartsFullDetail[index] = cart.copy(
            currPosHole = paceNotification.currentHole,
            holesPlayed = paceNotification.holesPlayed,
            totalNetPace = paceNotification.totalPace,
            startTime = paceNotification.roundDate
        )

        //Update alerts
        _alerts.find {
            it is AlertModel.Pace
                    && it.date.timestamp == paceNotification.date.timestamp
        }.also {
            if (it == null)
                _alerts.add(AlertModel.Pace(
                    date = paceNotification.date,
                    course = findCourse(paceNotification.idCourse),
                    cart = findCart(paceNotification.idCart),
                    netPace = paceNotification.totalPace
                ))
        }

        Logger.i(TAG, message = {
            "process PaceNotification updated"
        })
    }

    @Synchronized
    fun processNotification(fenceNotification: MarshalNotification.FenceNotification) {
        Logger.i(TAG, message = {
            "process FenceNotification $fenceNotification"
        })

        val idCart = fenceNotification.idCart

        val cart = _cartsFullDetail.find { it.id == idCart }

        if (cart == null) {
            Logger.e(TAG, message = {
                "Cannot find cart with id $idCart"
            })
            return
        }

        if (fenceNotification.idCart < 0) {
            Logger.e(TAG, message = {
                "Ignore fence $fenceNotification"
            })
            return
        }

        //Update alerts
        _alerts.find {
            it is AlertModel.Fence
                    && it.date.timestamp == fenceNotification.date.timestamp
        }.also {
            if (it == null)
                _alerts.add(AlertModel.Fence(
                    date = fenceNotification.date,
                    course = findCourse(fenceNotification.idCourse),
                    cart = findCart(fenceNotification.idCart),
                    geofence = findGeofence(fenceNotification.idFence)
                ))
        }
    }

    @Synchronized
    fun processNotification(data: MarshalNotification.ReturnAreaNotification) {
        Logger.i(TAG, message = {
            "process ReturnAreaNotification $data"
        })

        val idCart = data.idCart

        val cart = _cartsFullDetail.find { it.id == idCart }

        if (cart == null) {
            Logger.e(TAG, message = {
                "Cannot find cart with id $idCart"
            })
            return
        }

        val index = _cartsFullDetail.indexOf(cart)

        if (cart.returnAreaSts == data.status)
            return

        _cartsFullDetail[index] = cart.copy(
            returnAreaSts = data.status
        )

        Logger.i(TAG, message = {
            "process ReturnAreaNotification updated"
        })
    }

    @Synchronized
    fun processNotification(data: MarshalNotification.BatteryAlertNotification) {
        Logger.i(TAG, message = {
            "process BatteryAlertNotification $data"
        })

        val idCart = data.idCart

        val cart = _cartsFullDetail.find { it.id == idCart }

        if (cart == null) {
            Logger.e(TAG, message = {
                "Cannot find cart with id $idCart"
            })
            return
        }

        //Update alerts
        _alerts.find {
            it is AlertModel.Battery
                    && it.date.timestamp == data.date.timestamp
        }.also {
            if (it == null)
                _alerts.add(AlertModel.Battery(
                    date = data.date,
                    course = findCourse(data.idCourse),
                    cart = findCart(data.idCart),
                ))
        }
    }

    @Synchronized
    fun processNotification(data: MarshalNotification.EndTripNotification) {
        Logger.i(TAG, message = {
            "process EndTripNotification $data"
        })

        val idCart = data.idCart

        val cart = _cartsFullDetail.find { it.id == idCart }

        if (cart == null) {
            Logger.e(TAG, message = {
                "Cannot find cart with id $idCart"
            })
            return
        }

        val index = _cartsFullDetail.indexOf(cart)

        if (cart.currPosHole == null
            && cart.course == null
            && cart.returnAreaSts == 0
            && cart.idTrip == -1)
            return

        _cartsFullDetail[index] = cart.copy(
            currPosHole = null,
            course = null,
            returnAreaSts = 0,
            idTrip = -1
        )

        Logger.i(TAG, message = {
            "process EndTripNotification update"
        })
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
        /*Database.cartList
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
            .launchIn(scope)*/

        Database.cartRoundList
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
            .onEach { data ->
                val cartList = data.carts

                if (_cartsFullDetail.size < cartList.size) {
                    _cartsFullDetail.addAll(
                        cartList
                            .filter { c -> !_cartsFullDetail.any { it.id == c.id} }
                            .map { cart ->
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
                            }
                    )
                }
                else if (_cartsFullDetail.size > cartList.size) {
                    _cartsFullDetail.removeAll {
                        cartList.any { c -> c.id ==  it.id}
                    }
                }

                for (cartRound in data.cartRounds) {
                    val oldCart = _cartsFullDetail.find { it.id == cartRound.id }

                    if (oldCart == null) {
                        Logger.e(TAG, message = { "Cannot find cart id ${cartRound.id} by cart round callback" })
                        continue
                    } else if (oldCart.isContentEqual(cartRound))
                        continue

                    val index = _cartsFullDetail.indexOf(oldCart)

                    val course = data.courseList.find { c -> c.id == cartRound.idCourse }

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

    private fun findCart(id: Int) = cartsFullDetail.map { l ->
        l.find { it.id == id }
    }.onEach {
        if (it == null)
            loadCarts()
    }.filter {
        it != null
    }.map {
        it!!
    }

    private fun findCourse(id: String) = courseList.map { l ->
        l.find { it.id == id }
    }.onEach {
        if (it == null)
            loadCourses()
    }.filter {
        it != null
    }.map {
        it!!
    }

    private fun findGeofence(id: Int) = geofenceList.map { l ->
        l.find { it.id == id }
    }.onEach {
        if (it == null)
            loadGeofenceList()
    }.filter {
        it != null
    }.map {
        it!!
    }

    private fun findHole(id: Int) = holeList.map { l ->
        l.find { it.id == id }
    }.onEach {
        if (it == null)
            loadCartReport()
    }.filter {
        it != null
    }.map {
        it!!
    }

    val holeList = Database.cartReport

    private val geofenceList = Database.geofenceList

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

    private val _cartsFullDetail = MutableStateFlow(emptyList<CartFullDetail>())
    val cartsFullDetail: StateFlow<List<CartFullDetail>>
        get() = _cartsFullDetail

    private val _alerts = MutableStateFlow<List<AlertModel>>(emptyList())
    val alerts: StateFlow<List<AlertModel>>
        get() = _alerts
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