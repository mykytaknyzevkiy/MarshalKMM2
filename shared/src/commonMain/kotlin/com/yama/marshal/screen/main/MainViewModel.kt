package com.yama.marshal.screen.main

import androidx.compose.runtime.mutableStateListOf
import com.yama.marshal.data.entity.HoleEntity
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.tool.*
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.prefs
import com.yama.marshal.tool.setCartFlag
import io.ktor.util.date.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface SortType {
    val label: String
    val weight: Float

    enum class SortFleet(
        override val label: String,
        override val weight: Float
    ) : SortType {
        CAR(Strings.fleet_list_screen_table_row_car_label, 0.8f),
        START_TIME(Strings.fleet_list_screen_table_row_start_time_label, 0.7f),
        PLACE_OF_PLAY(Strings.fleet_list_screen_table_row_place_of_place_label, 1f),
        HOLE(Strings.fleet_list_screen_table_row_hole_label, 0.5f)
    }

    enum class SortHole(
        override val label: String,
        override val weight: Float
    ) : SortType {
        HOLE(Strings.hole_screen_table_row_hole_label, 0.3f),
        PACE_OF_PLAY(Strings.hole_screen_table_row_pace_of_play_label, 1f)
    }
}

class MainViewModel : YamaViewModel() {
    val clock = flow {
        while (true) {
            emit(GMTDate())
            delay(60 * 1000)
        }
    }.map {
        it.format("hh:mm a")
    }

    private val _currentFleetSort = MutableStateFlow(SortType.SortFleet.CAR)
    val currentFleetSort: StateFlow<SortType.SortFleet>
        get() = _currentFleetSort

    private val _currentHoleSort = MutableStateFlow(SortType.SortHole.HOLE)
    val currentHoleSort: StateFlow<SortType.SortHole>
        get() = _currentHoleSort

    private val _selectedCourse = MutableStateFlow<CourseFullDetail?>(null)
    val selectedCourse: StateFlow<CourseFullDetail?>
        get() = _selectedCourse

    private val _courseList = MutableStateFlow(emptyList<CourseFullDetail>())
    val courseList: StateFlow<List<CourseFullDetail>>
        get() = _courseList

    val fleetList: List<CartFullDetail>
        get() = CompanyRepository.cartsFullDetail

    private val _holeList = mutableStateListOf<HoleEntity>()
    val holeList: List<HoleEntity>
        get() = _holeList

    fun load() {
        CompanyRepository
            .courseList
            .map {
                ArrayList<CourseFullDetail>().apply {
                    add(
                        CourseFullDetail(
                            id = null,
                            courseName = "All",
                            defaultCourse = 0,
                            playersNumber = 0,
                            layoutHoles = null,
                            holes = emptyList()
                        )
                    )
                    addAll(it)
                }
            }
            .onEach {
                _courseList.emit(it)
                if (_selectedCourse.value == null)
                    _selectedCourse.emit(if (it.size == 1) it.first() else it[1])
            }
            .launchIn(viewModelScope)

        CompanyRepository
            .holeList
            .onEach {
                _holeList.clear()
                _holeList.addAll(it.sortedWith(HoleSorter(_currentHoleSort.value)))
            }
            .launchIn(viewModelScope)
    }

    fun updateFleetSort(type: SortType.SortFleet) {
        _currentFleetSort.value = type
    }

    fun updateHoleSort(type: SortType.SortHole) {
        _currentHoleSort.value = type

        _holeList.sortWith(HoleSorter(type))
    }

    fun selectCourse(course: CourseFullDetail) {
        _selectedCourse.value = course
    }

    fun flagCart(cart: CartFullDetail) {
        CompanyRepository.flagCart(cart.id)
    }

}