package com.yama.marshal.repository

import co.touchlab.kermit.Logger
import com.yama.marshal.data.Database
import com.yama.marshal.data.entity.CourseEntity
import com.yama.marshal.network.DNAService
import com.yama.marshal.network.model.CourseRelationshipListRequest
import com.yama.marshal.tool.companyID
import com.yama.marshal.tool.prefs
import kotlinx.coroutines.flow.onStart

class CompanyRepository {
    companion object {
        private const val TAG = "CompanyRepository"
    }

    private val dnaService = DNAService()

    suspend fun loadCourses(): Boolean {
        Logger.i(tag = TAG, message = {
            "loadCourses"
        })

        dnaService.courseRelationshipList(CourseRelationshipListRequest(prefs.companyID)).let {
            if (it == null)
                return false
            it
        }.resultList.map {
            CourseEntity(
                id = it.idCourse,
                courseName = it.courseName,
                defaultCourse = it.defaultCourse,
                playersNumber = it.playersNumber,
                layoutHoles = it.layoutHoles
            )
        }.also {
            Database.updateCourses(it)

            Logger.i(tag = TAG, message = {
                "courses success saved"
            })
        }

        return true
    }

    val courseList = Database
        .courseList
        .onStart {
           // loadCourses()
        }
}