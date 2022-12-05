package com.yama.marshal.screen.fleet_list

import com.yama.marshal.data.entity.CourseEntity
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.screen.YamaViewModel
import kotlinx.coroutines.flow.*

enum class SortFleet(val label: String, val weight: Float) {
    CAR("fleet_list_screen_table_row_car_label", 0.8f),
    START_TIME("fleet_list_screen_table_row_start_time_label", 0.7f),
    PLACE_OF_PLAY("fleet_list_screen_table_row_place_of_place_label", 1f),
    HOLE("fleet_list_screen_table_row_hole_label", 0.5f)
}

class FleetListViewModel : YamaViewModel() {
    private val companyRepository = CompanyRepository()

    private val _currentFleetSort = MutableStateFlow(SortFleet.CAR)
    val currentFleetSort: StateFlow<SortFleet>
        get() = _currentFleetSort

    private val _selectedCourse = MutableStateFlow<CourseEntity?>(null)
    val selectedCourse: StateFlow<CourseEntity?>
        get() = _selectedCourse

    private val _courseList = MutableStateFlow(emptyList<CourseEntity>())
    val courseList: StateFlow<List<CourseEntity>>
        get() = _courseList

    fun load() {
        companyRepository
            .courseList
            .map {
                ArrayList<CourseEntity>().apply {
                    add(
                        CourseEntity(
                            id = "",
                            courseName = "All",
                            defaultCourse = 0,
                            playersNumber = 0,
                            layoutHoles = null
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
    }

    fun updateSort(type: SortFleet) {
        _currentFleetSort.value = type
    }
}