package com.yama.marshal.screen.main

import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.repository.CartRepository
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.repository.CourseRepository
import com.yama.marshal.repository.UserRepository
import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.tool.FleetSorter
import com.yama.marshal.tool.HoleSorter
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.format
import io.ktor.util.date.*
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

    val courseList = CourseRepository
        .courseList
        .map {
            it.toMutableList().apply {
                add(0, CourseFullDetail(
                    id = null,
                    courseName = "All",
                    defaultCourse = 0,
                    playersNumber = 0,
                    layoutHoles = null,
                    holes = emptyList(),
                    vectors = ""
                ))
            }
        }
        .onEach {
            if (_selectedCourse.value == null)
                _selectedCourse.emit(if (it.size == 1) it.first() else it[1])
        }

    val fleetList = CartRepository
        .cartActiveList
        .combine(_selectedCourse) { a, b ->
            if (b?.id.isNullOrBlank())
                a
        else
            a.filter { c -> c.course?.id == b?.id }
        }
        .combine(_currentFleetSort) { a, b ->
            a.sortedWith(FleetSorter(b))
        }

    val holeList = CourseRepository
        .holeList
        .combine(_selectedCourse) {a, b ->
            if (b?.id.isNullOrBlank())
                a
            else
                a.filter { it.idCourse == b?.id }
        }
        .combine(_currentHoleSort) { a, b ->
            a.sortedWith(HoleSorter(b))
        }

    val alertList = CompanyRepository
        .alerts
        .combine(_selectedCourse) { a, b ->
            a.filter { b?.id.isNullOrBlank() || it.courseID == b?.id }
        }
        .map {
            it.sortedByDescending { a -> a.date.timestamp }
        }

    fun updateSort(type: SortType.SortFleet) {
        _currentFleetSort.value = type
    }

    fun updateSort(type: SortType.SortHole) {
        _currentHoleSort.value = type
    }

    fun selectCourse(course: CourseFullDetail) {
        _selectedCourse.value = course
    }

    fun flagCart(cart: CartFullDetail) = viewModelScope.launch {
        CartRepository.flagCart(cart.id)
    }

    fun logOut() {
        UserRepository().logOut()
    }
}