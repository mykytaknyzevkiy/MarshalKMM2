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
        .map { nAlerts ->
            val alerts = mutableListOf<AlertEntity>()

            for (notification in nAlerts) {
                val idCart = notification.idCart

                val cart = Database
                    .cartRoundList
                    .map { l ->
                        l.findLast { it.id == idCart }
                    }
                    .first() ?: CartRoundItem(id = idCart)


                val alert: AlertEntity? = when (notification) {
                    is MarshalNotification.PaceNotification -> {
                        Logger.i(TAG, message = {
                            "process PaceNotification $notification"
                        })

                        if (cart.currPosHole != notification.currentHole
                            || cart.holesPlayed != notification.holesPlayed
                            || cart.totalNetPace != notification.totalPace
                            || cart.roundStartTime != notification.roundDate
                        )
                            cart.copy(
                                currPosHole = notification.currentHole,
                                holesPlayed = notification.holesPlayed,
                                totalNetPace = notification.totalPace,
                                roundStartTime = notification.roundDate
                            ).also { Database.addCartRound(it) }

                        AlertEntity(
                            courseID = notification.idCourse,
                            date = notification.date,
                            cartID = notification.idCart,
                            type = AlertType.Pace,
                            netPace = notification.totalPace
                        )
                    }

                    is MarshalNotification.FenceNotification -> {
                        Logger.i(TAG, message = {
                            "process FenceNotification $notification"
                        })

                        AlertEntity(
                            courseID = notification.idCourse,
                            date = notification.date,
                            cartID = notification.idCart,
                            geofenceID = notification.idFence,
                            type = AlertType.Fence
                        )
                    }

                    is MarshalNotification.ReturnAreaNotification -> {
                        Logger.i(TAG, message = {
                            "process ReturnAreaNotification $notification"
                        })

                        if (cart.onDest != notification.status)
                            cart.copy(onDest = notification.status).also {
                                Database.addCartRound(it)
                            }

                        null
                    }

                    is MarshalNotification.BatteryAlertNotification -> {
                        Logger.i(TAG, message = {
                            "process BatteryAlertNotification $notification"
                        })

                        AlertEntity(
                            courseID = notification.idCourse,
                            date = notification.date,
                            cartID = notification.idCart,
                            type = AlertType.Battery
                        )
                    }

                    is MarshalNotification.EndTripNotification -> {
                        if (cart.currPosHole != null
                            || cart.idCourse != null
                            || cart.onDest != 0
                            || cart.idTrip != -1
                        )
                            cart.copy(
                                currPosHole = null,
                                idCourse = null,
                                onDest = 0,
                                idTrip = -1
                            ).also {
                                Database.addCartRound(it)
                            }

                        null
                    }

                    is MarshalNotification.MarshalMessageNotification -> {
                        Logger.i(TAG, message = {
                            "process MarshalNotification $notification"
                        })

                        val cartID = notification.idCart

                        val rawMessage = notification.message

                        val components = rawMessage.split(" - ".toRegex())

                        val message = if (components.size >= 2) {
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

                        CartRepository.addCartMessage(message)

                        null
                    }

                    else -> {
                        Logger.i(TAG, message = {
                            "process Unsupported $notification"
                        })

                        null
                    }
                }

                if (alert != null)
                    alerts.add(alert)
            }

            alerts
        }
        .onEach { alerts ->
            Database.addAlerts(alerts)
        }

    fun start() = this.launch(Dispatchers.Default) {
        onNotification.launchIn(this)

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