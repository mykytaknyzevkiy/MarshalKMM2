package com.yama.marshal.service

import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.AlertEntity
import com.yama.marshal.data.entity.AlertType
import com.yama.marshal.data.entity.CartRoundItem
import com.yama.marshal.data.model.AlertModel
import com.yama.marshal.data.model.CartMessageModel
import com.yama.marshal.network.MarshalSocket
import com.yama.marshal.network.model.request.MarshalNotification
import com.yama.marshal.repository.CartRepository
import com.yama.marshal.repository.CartRepository.findCart
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.repository.CourseRepository
import com.yama.marshal.repository.CourseRepository.findCourse
import com.yama.marshal.repository.UserRepository
import com.yama.marshal.tool.*
import com.yama.marshal.tool.prefs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlin.coroutines.CoroutineContext

object MarshalNotificationService : CoroutineScope {
    private const val TAG = "MarshalNotificationService"

    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job

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

            Database.alerts.any {
                it.type == AlertType.Pace
                        && it.date.timestamp == notification.date.timestamp
            }.also {
                if (!it)
                    Database.addAlert(
                        AlertEntity(
                            courseID = notification.idCourse,
                            date = notification.date,
                            cartID = notification.idCart,
                            type = AlertType.Pace,
                            netPace = notification.totalPace
                        )
                    )
            }
        }

    private val fenceNotificationManager = onNotification
        .filterList { it is MarshalNotification.FenceNotification }
        .mapList { it as MarshalNotification.FenceNotification }
        .onEachList { notification ->
            Logger.i(TAG, message = {
                "process FenceNotification $notification"
            })

            Database.alerts.any {
                it.type == AlertType.Fence
                        && it.date.timestamp == notification.date.timestamp
            }.also {
                if (!it)
                    Database.addAlert(
                        AlertEntity(
                            courseID = notification.idCourse,
                            date = notification.date,
                            cartID = notification.idCart,
                            geofenceID = notification.idFence,
                            type = AlertType.Fence
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
        .onEachList { notification ->
            Logger.i(TAG, message = {
                "process BatteryAlertNotification $notification"
            })

            Database.alerts.any {
                it.type == AlertType.Battery
                        && it.date.timestamp == notification.date.timestamp
            }.also {
                if (!it)
                    Database.addAlert(
                        AlertEntity(
                            courseID = notification.idCourse,
                            date = notification.date,
                            cartID = notification.idCart,
                            type = AlertType.Battery
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

    private val messageNotificationManager = onNotification
        .filterList { it is MarshalNotification.MarshalMessageNotification }
        .mapList { it as MarshalNotification.MarshalMessageNotification }
        .mapList { notification ->
            Logger.i(TAG, message = {
                "process MarshalNotification $notification"
            })

            val cartID = notification.idCart

            val rawMessage = notification.message

            val components = rawMessage.split(" - ".toRegex())

            if (components.size >= 2) {
                val type = components[0]

                var customMessage = ""
                for (i in 1 until components.size) {
                    customMessage += components[i]
                    if (i < components.size - 1) {
                        customMessage += " - "
                    }
                }

                if (type.equals("EMERGENCY", true))
                    CartMessageModel.Emergency(
                        cartID = cartID,
                        message = rawMessage
                    )
                else if (type.equals("GOLF CAR ISSUE", true))
                    CartMessageModel.Issue(
                        cartID = cartID,
                        message = rawMessage
                    )
                else
                    CartMessageModel.Custom(
                        cartID = cartID,
                        message = rawMessage
                    )
            } else
                CartMessageModel.Custom(
                    cartID = cartID,
                    message = rawMessage
                )
        }
        .onEachList {
            CartRepository.addCartMessage(it)
        }

    fun start() = this.launch(Dispatchers.Default) {
        merge(
            penceNotificationManager,
            fenceNotificationManager,
            returnAreaNotificationManager,
            batteryAlertNotificationManager,
            endTripNotificationManager,
            messageNotificationManager
        ).launchIn(this)

        marshalSocket.connect()

        while (true) {
            delay(2 * 60 * 1000L)

            val userName = prefs.userName
            val userPassword = prefs.userPassword

            UserRepository().login(
                userName ?: break,
                userPassword ?: break
            )

            marshalSocket.disconnect()
            marshalSocket.connect()

            CourseRepository.loadHoles()
            CartRepository.loadCarts()
            CartRepository.loadCartsRound()
        }
    }

    fun stop() {
        marshalSocket.disconnect()
        job.cancelChildren()
    }

    fun restart() {
        try {
            stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        start()
    }
}