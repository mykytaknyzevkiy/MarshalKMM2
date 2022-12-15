package com.yama.marshal.service

import co.touchlab.kermit.Logger
import com.yama.marshal.network.MarshalSocket
import com.yama.marshal.network.model.MarshalNotification
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.repository.filterList
import com.yama.marshal.repository.mapList
import com.yama.marshal.repository.onEachList
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
        .onEachList { 
            CompanyRepository.processNotification(it)
        }

    private val fenceNotificationManager = onNotification
        .filterList { it is MarshalNotification.FenceNotification }
        .mapList { it as MarshalNotification.FenceNotification }
        .onEachList {
            CompanyRepository.processNotification(it)
        }

    private val returnAreaNotificationManager = onNotification
        .filterList { it is MarshalNotification.ReturnAreaNotification }
        .mapList { it as MarshalNotification.ReturnAreaNotification }
        .onEachList {
            CompanyRepository.processNotification(it)
        }

    private val batteryAlertNotificationManager = onNotification
        .filterList { it is MarshalNotification.BatteryAlertNotification }
        .mapList { it as MarshalNotification.BatteryAlertNotification }
        .onEachList {
            CompanyRepository.processNotification(it)
        }

    private val endTripNotificationManager = onNotification
        .filterList { it is MarshalNotification.EndTripNotification }
        .mapList { it as MarshalNotification.EndTripNotification }
        .onEachList {
            CompanyRepository.processNotification(it)
        }

    fun start() = this.launch(Dispatchers.Default) {
        CompanyRepository.launchCartsUpdater(this)

        /*merge(
            penceNotificationManager,
            fenceNotificationManager,
            returnAreaNotificationManager,
            batteryAlertNotificationManager,
            endTripNotificationManager
        ).launchIn(this)*/

        onNotification
            .onEachList {
                when (it) {
                    is MarshalNotification.PaceNotification -> CompanyRepository.processNotification(it)
                    is MarshalNotification.FenceNotification -> CompanyRepository.processNotification(it)
                    is MarshalNotification.ReturnAreaNotification -> CompanyRepository.processNotification(it)
                    is MarshalNotification.BatteryAlertNotification -> CompanyRepository.processNotification(it)
                    is MarshalNotification.EndTripNotification -> CompanyRepository.processNotification(it)
                    else -> {

                    }
                }
            }
            .launchIn(this)

        marshalSocket.connect()

        while (true) {
            delay(5 * 60 * 1000L)

            CompanyRepository.loadCarts()
            CompanyRepository.loadCartsRound()
        }
    }
}