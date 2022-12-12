package com.yama.marshal.screen.main

import androidx.compose.runtime.mutableStateListOf
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

enum class SortFleet(val label: String, val weight: Float) {
    CAR(Strings.fleet_list_screen_table_row_car_label, 0.8f),
    START_TIME(Strings.fleet_list_screen_table_row_start_time_label, 0.7f),
    PLACE_OF_PLAY(Strings.fleet_list_screen_table_row_place_of_place_label, 1f),
    HOLE(Strings.fleet_list_screen_table_row_hole_label, 0.5f)
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

    private val companyRepository = CompanyRepository()

    private val _currentFleetSort = MutableStateFlow(SortFleet.CAR)
    val currentFleetSort: StateFlow<SortFleet>
        get() = _currentFleetSort

    private val _selectedCourse = MutableStateFlow<CourseFullDetail?>(null)
    val selectedCourse: StateFlow<CourseFullDetail?>
        get() = _selectedCourse

    private val _courseList = MutableStateFlow(emptyList<CourseFullDetail>())
    val courseList: StateFlow<List<CourseFullDetail>>
        get() = _courseList

    private val _fleetList = mutableStateListOf<CartFullDetail>()
    val fleetList: List<CartFullDetail>
        get() = _fleetList

    fun load() {
        companyRepository
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

        companyRepository
            .cartsFullDetail
            .flowOn(Dispatchers.Default)
            .map {
                it.filter { c ->
                    c.lastActivity != null && !c.lastActivity.isBeforeDate(GMTDate())
                }
            }
            .map {
                it.sortedWith(FleetSorter(_currentFleetSort.value))
            }
            .onEach {
                _fleetList.clear()
                _fleetList.addAll(it)
            }
            .launchIn(viewModelScope)
    }

    fun updateFleetSort(type: SortFleet) {
        _currentFleetSort.value = type

        viewModelScope.launch(Dispatchers.Default) {
            _fleetList.sortWith(FleetSorter(type))
        }
    }

    fun selectCourse(course: CourseFullDetail) {
        _selectedCourse.value = course
    }

    fun flagCart(cart: CartFullDetail) {
        prefs.setCartFlag(cart.id)

        cart.isFlag = true

        updateFleetSort(_currentFleetSort.value)
    }

}