package com.yama.marshal.repository

import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.CompanyMessage
import com.yama.marshal.data.entity.GeofenceItem
import com.yama.marshal.data.model.AlertModel
import com.yama.marshal.network.model.request.CartMessageListRequest
import com.yama.marshal.network.model.request.CartMessageSentRequest
import com.yama.marshal.network.model.request.GeofenceListRequest
import com.yama.marshal.network.service.DNAService
import com.yama.marshal.tool.add
import com.yama.marshal.tool.companyID
import com.yama.marshal.tool.filterList
import com.yama.marshal.tool.prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

object CompanyRepository : CoroutineScope {
    private const val TAG = "CompanyRepository"

    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private val dnaService = DNAService()

    val companyMessages = Database
        .companyMessages
        .filterList {
            it.message.isNotBlank()
        }

    val alerts = Database.alerts

    suspend fun addAlert(data: AlertModel) {
        Database.addAlert(data)
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

    fun findGeofence(id: Int) = Database
        .geofenceList
        //.filter { it.any { g -> g.id == id } }
        .map { it.find { g -> g.id == id } }
        .onEach {
            if (it == null)
                loadGeofenceList()
        }
        .filter { it != null }
        .map { it!! }
}