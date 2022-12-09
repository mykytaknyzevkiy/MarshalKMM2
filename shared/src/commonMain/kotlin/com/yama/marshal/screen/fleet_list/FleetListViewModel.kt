package com.yama.marshal.screen.fleet_list

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.tool.FleetSorter
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.prefs
import com.yama.marshal.tool.setCartFlag
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortFleet(val label: String, val weight: Float) {
    CAR(Strings.fleet_list_screen_table_row_car_label, 0.8f),
    START_TIME(Strings.fleet_list_screen_table_row_start_time_label, 0.7f),
    PLACE_OF_PLAY(Strings.fleet_list_screen_table_row_place_of_place_label, 1f),
    HOLE(Strings.fleet_list_screen_table_row_hole_label, 0.5f)
}

class FleetListViewModel : YamaViewModel() {
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

    val fleetList = mutableStateListOf<CartFullDetail>()

    private var currentLoadCartJob: Job? = null

    fun load() {
        companyRepository
            .courseList
            .map {
                ArrayList<CourseFullDetail>().apply {
                    add(
                        CourseFullDetail(
                            id = "",
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

        _selectedCourse.onEach {
            loadCarts()
        }.launchIn(viewModelScope)
    }

    private fun loadCarts() {
        val courseEntity = selectedCourse.value ?: return

        companyRepository
            .cartOfCourse(courseEntity.id)
            .onEach {
                fleetList.clear()
                fleetList.addAll(it)
            }
            .launchIn(this.viewModelScope)
            .also {
                currentLoadCartJob = it
            }
    }

    fun updateSort(type: SortFleet) {
        _currentFleetSort.value = type

        fleetList.sortWith(FleetSorter(type))
    }

    fun selectCourse(course: CourseFullDetail) {
        _selectedCourse.value = course
    }

    fun flagCart(cart: CartFullDetail) {
        prefs.setCartFlag(cart.id)

        loadCarts()
    }
}