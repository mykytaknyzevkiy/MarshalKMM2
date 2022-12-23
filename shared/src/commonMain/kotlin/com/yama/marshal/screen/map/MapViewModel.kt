package com.yama.marshal.screen.map

import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.repository.filterList
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

    private val _cartsState = MutableStateFlow<List<CartFullDetail>>(emptyList())
    val cartsState: StateFlow<List<CartFullDetail>>
        get() = _cartsState

    fun loadHole(id: Int, courseID: String) {
        CompanyRepository
            .findHole(id, courseID)
            .onEach {
                _holeState.emit(it)
            }
            .launchIn(viewModelScope)

        CompanyRepository
            .cartsFullDetail
            .filterList {
                it.currPosHole == id
            }
            .onEach {
                _cartsState.value = it
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

    fun loadCart(id: Int) {
        CompanyRepository
            .findCart(id)
            .onEach {
                _cartsState.value = listOf(it)
                _holeState.value = it.hole
            }
            .launchIn(viewModelScope)
    }

    override fun onClear() {
        super.onClear()
        _holeState.value = null
        _courseState.value = null
        _cartsState.value = emptyList()
    }
}