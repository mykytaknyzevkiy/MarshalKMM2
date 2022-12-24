package com.yama.marshal.service

import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.CartRoundItem
import com.yama.marshal.data.model.AlertModel
import com.yama.marshal.network.MarshalSocket
import com.yama.marshal.network.model.request.MarshalNotification
import com.yama.marshal.repository.CartRepository
import com.yama.marshal.repository.CartRepository.findCart
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.repository.CourseRepository
import com.yama.marshal.repository.CourseRepository.findCourse
import com.yama.marshal.tool.any
import com.yama.marshal.tool.filterList
import com.yama.marshal.tool.mapList
import com.yama.marshal.tool.onEachList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlin.coroutines.CoroutineContext

object MarshalNotificationService : CoroutineScope {
    private const val TAG = "MarshalNotificationService"

    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private val marshalSocket = MarshalSocket()

    private val onNotification = marshalSocket.onMessage
        .catch {
            Logger.e(TAG, throwable = it)
        }
        .map {
            val responseJson = try {
                Json.parseToJsonElement(it)
            } catch (e: Exception) {
                Logger.e(TAG, message = { "Parse responseJson to jsonElement"}, throwable = e)
                throw Exception("Parse json")
            }

            val jsonArray = try {
                responseJson.jsonArray
            } catch (e: Exception) {
                throw Exception("Get json array")
            }

            MarshalNotification.parse(jsonArray)
        }
    
    private val penceNotificationManager = onNotification
        .filterList { it is MarshalNotification.PaceNotification }
        .mapList { it as MarshalNotification.PaceNotification }
        .onEachList { notification ->
            Logger.i(TAG, message = {
                "process PaceNotification $notification"
            })

            val idCart = notification.idCart

            val cartRound = Database
                .cartRoundList
                .map { l ->
                    l.findLast { it.id == idCart }
                }
                .first() ?: CartRoundItem(id = idCart)

            if (cartRound.currPosHole != notification.currentHole
                || cartRound.holesPlayed != notification.holesPlayed
                || cartRound.totalNetPace != notification.totalPace
                || cartRound.roundStartTime != notification.roundDate)
                cartRound.copy(
                    currPosHole = notification.currentHole,
                    holesPlayed = notification.holesPlayed,
                    totalNetPace = notification.totalPace,
                    roundStartTime = notification.roundDate
                ).also { Database.addCartRound(it) }
        }

    private val fenceNotificationManager = onNotification
        .filterList { it is MarshalNotification.FenceNotification }
        .mapList { it as MarshalNotification.FenceNotification }
        .onEachList { notification ->
            Logger.i(TAG, message = {
                "process FenceNotification $notification"
            })

            CompanyRepository.alerts.any {
                it is AlertModel.Fence
                        && it.date.timestamp == notification.date.timestamp
            }.also {
                if (!it)
                    CompanyRepository.addAlert(
                        AlertModel.Fence(
                            courseID = notification.idCourse,
                            date = notification.date,
                            course = CourseRepository.findCourse(notification.idCourse),
                            cart = CartRepository.findCart(notification.idCart)
                                .filter { c -> c != null }.map { c -> c!! },
                            geofence = CompanyRepository.findGeofence(notification.idFence)
                        )
                    )
            }
        }

    private val returnAreaNotificationManager = onNotification
        .filterList { it is MarshalNotification.ReturnAreaNotification }
        .mapList { it as MarshalNotification.ReturnAreaNotification }
        .onEachList { data ->
            Logger.i(TAG, message = {
                "process ReturnAreaNotification $data"
            })

            val idCart = data.idCart

            val cart = Database
                .cartRoundList
                .map { l ->
                    l.findLast { it.id == idCart }
                }
                .first() ?: CartRoundItem(id = idCart)

            if (cart.onDest != data.status)
                cart.copy(onDest = data.status).also {
                    Database.addCartRound(it)
                }
        }

    private val batteryAlertNotificationManager = onNotification
        .filterList { it is MarshalNotification.BatteryAlertNotification }
        .mapList { it as MarshalNotification.BatteryAlertNotification }
        .onEachList { data ->
            Logger.i(TAG, message = {
                "process BatteryAlertNotification $data"
            })

            CompanyRepository.alerts.any {
                it is AlertModel.Battery
                        && it.date.timestamp == data.date.timestamp
            }.also {
                if (!it)
                    CompanyRepository.addAlert(
                        AlertModel.Battery(
                            courseID = data.idCourse,
                            date = data.date,
                            course = findCourse(data.idCourse),
                            cart = findCart(data.idCart).map { c-> c!! },
                        )
                    )
            }

        }

    private val endTripNotificationManager = onNotification
        .filterList { it is MarshalNotification.EndTripNotification }
        .mapList { it as MarshalNotification.EndTripNotification }
        .onEachList { data ->
            Logger.i(TAG, message = {
                "process EndTripNotification $data"
            })

            val idCart = data.idCart

            val cart = Database
                .cartRoundList
                .map { l ->
                    l.findLast { it.id == idCart }
                }
                .first()

            if (cart != null && (cart.currPosHole != null
                        || cart.idCourse != null
                        || cart.onDest != 0
                        || cart.idTrip != -1))
                cart.copy(
                    currPosHole = null,
                    idCourse = null,
                    onDest = 0,
                    idTrip = -1
                ).also {
                    Database.addCartRound(it)
                }
        }

    fun start() = this.launch(Dispatchers.Default) {
        merge(
            penceNotificationManager,
            fenceNotificationManager,
            returnAreaNotificationManager,
            batteryAlertNotificationManager,
            endTripNotificationManager
        ).launchIn(this)

        marshalSocket.connect()

        while (true) {
            delay(5 * 60 * 1000L)

            CourseRepository.loadHoles()
            CartRepository.loadCarts()
            CartRepository.loadCartsRound()
        }
    }
}