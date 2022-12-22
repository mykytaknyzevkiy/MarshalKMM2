package com.yama.marshal.screen.map

import co.touchlab.kermit.Logger
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.screen.YamaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MapViewModel : YamaViewModel() {
    private val _holeState = MutableStateFlow<CourseFullDetail.HoleData?>(null)
    val holeState: StateFlow<CourseFullDetail.HoleData?>
        get() = _holeState

    private val _courseState = MutableStateFlow<CourseFullDetail?>(null)
    val courseState: StateFlow<CourseFullDetail?>
        get() = _courseState

    fun loadHole(id: Int, courseID: String) {
        CompanyRepository
            .findHole(id, courseID)
            .onEach {
                _holeState.emit(it)
            }
            .launchIn(viewModelScope)
    }

    fun loadCourse(id: String) {
        CompanyRepository
            .findCourse(id)
            .onEach {
                _courseState.emit(it)
            }
            .launchIn(viewModelScope)
    }
}