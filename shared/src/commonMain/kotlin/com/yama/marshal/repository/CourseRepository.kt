package com.yama.marshal.repository

import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.CourseEntity
import com.yama.marshal.data.entity.HoleEntity
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.network.model.request.CartReportByTypeRequest
import com.yama.marshal.network.model.request.CourseGPSVectorDetailsRequest
import com.yama.marshal.network.model.request.CourseRelationshipListRequest
import com.yama.marshal.network.service.DNAService
import com.yama.marshal.network.service.IGolfService
import com.yama.marshal.repository.unit.YamaRepository
import com.yama.marshal.tool.companyID
import com.yama.marshal.tool.mapList
import com.yama.marshal.tool.prefs
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

object CourseRepository : YamaRepository() {
    private const val TAG = "CompanyRepository"

    private val dnaService = DNAService()
    private val iGolfService = IGolfService()

    val holeList = Database
        .cartReport
        .mapList { h ->
            CourseFullDetail.HoleData(
                holeNumber = h.id,
                defaultPace = h.defaultPace,
                averagePace = h.averagePace,
                differentialPace = h.differentialPace,
                idCourse = h.idCourse
            )
        }
        .onStart {
            loadHoles()
        }

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
                    holes = b.filter { h -> h.idCourse == it.id },
                    vectors = it.vectors
                )
            }
        }
        .onStart {
            loadCourses()
        }

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
            .map {
                val courseResponse = iGolfService.courseVectors(
                    CourseGPSVectorDetailsRequest(idCourse = it.id)
                )

                if (courseResponse == null) {
                    Logger.e(TAG, message = {
                        "Cannot load GPSVector for course ${it.id}"
                    })
                    return false
                }

                val vectors = Json
                    .parseToJsonElement(courseResponse)
                    .jsonObject["vectorGPSObject"]
                    .also { jsonElement ->
                        if (jsonElement == null) {
                            Logger.e(TAG, message = {
                                "Cannot load GPSVector for course ${it.id}"
                            })
                            return false
                        }
                    }
                    .toString()

                it.copy(vectors = vectors)
            }
            .also {
                Database.updateCourses(it)
            }

        return true
    }

    suspend fun loadHoles(): Boolean {
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

    fun findCourse(id: String) = courseList
        .map { l -> l.find { it.id == id } }
        .onEach { if (it == null) loadCourses() }
        .filter { it != null }
        .map { it!! }

    fun findHole(id: Int, courseID: String) = holeList
        .map { l -> l.find { it.holeNumber == id && it.idCourse == courseID } }
        .onEach { if (it == null) loadHoles() }
        .filter { it != null }
        .map {
            it!!
        }
}