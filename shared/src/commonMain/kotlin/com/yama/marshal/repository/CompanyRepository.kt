package com.yama.marshal.repository

import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.*
import com.yama.marshal.data.entity.GeofenceItem
import com.yama.marshal.data.model.AlertModel
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.network.unit.AuthManager
import com.yama.marshal.network.service.DNAService
import com.yama.marshal.network.service.IGolfService
import com.yama.marshal.network.model.request.*
import com.yama.marshal.tool.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.Synchronized

object CompanyRepository : CoroutineScope {
    private const val TAG = "CompanyRepository"

    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private val dnaService = DNAService()

    val companyMessages = Database
        .companyMessages
        .filterList {
            it.message.isNotBlank()
        }

    private val _alerts = MutableStateFlow<List<AlertModel>>(emptyList())
    val alerts: StateFlow<List<AlertModel>>
        get() = _alerts

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
            && cart.startTime == paceNotification.roundDate
        )
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
                _alerts.add(
                    AlertModel.Pace(
                        courseID = paceNotification.idCourse,
                        date = paceNotification.date,
                        course = findCourse(paceNotification.idCourse),
                        cart = findCart(paceNotification.idCart),
                        netPace = paceNotification.totalPace
                    )
                )
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
                _alerts.add(
                    AlertModel.Fence(
                        courseID = fenceNotification.idCourse,
                        date = fenceNotification.date,
                        course = findCourse(fenceNotification.idCourse),
                        cart = findCart(fenceNotification.idCart),
                        geofence = findGeofence(fenceNotification.idFence)
                    )
                )
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
                _alerts.add(
                    AlertModel.Battery(
                        courseID = data.idCourse,
                        date = data.date,
                        course = findCourse(data.idCourse),
                        cart = findCart(data.idCart),
                    )
                )
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
            && cart.idTrip == -1
        )
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
}