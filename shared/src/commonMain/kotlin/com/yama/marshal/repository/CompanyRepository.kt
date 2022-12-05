package com.yama.marshal.repository

import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.CartItem
import com.yama.marshal.data.entity.CourseEntity
import com.yama.marshal.network.AuthManager
import com.yama.marshal.network.DNAService
import com.yama.marshal.network.model.CartDetailsListRequest
import com.yama.marshal.network.model.CourseRelationshipListRequest
import com.yama.marshal.tool.companyID
import com.yama.marshal.tool.prefs
import io.ktor.util.date.*

class CompanyRepository {
    companion object {
        private const val TAG = "CompanyRepository"
    }

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
            .cartDetailsList(CartDetailsListRequest(prefs.companyID))
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
                            GMTDateParser("yyMMddHHmmss").parse(d)
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

    val courseList = Database
        .courseList
}