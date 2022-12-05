package com.yama.marshal.screen.fleet_list

import com.yama.marshal.screen.YamaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class SortFleet(val label: String) {
    CAR("fleet_list_screen_table_row_car_label"),
    START_TIME("fleet_list_screen_table_row_start_time_label"),
    PLACE_OF_PLAY("fleet_list_screen_table_row_place_of_place_label"),
    HOLE("fleet_list_screen_table_row_hole_label")
}

class FleetListViewModel : YamaViewModel() {
    private val _currentFleetSort = MutableStateFlow(SortFleet.CAR)
    val currentFleetSort: StateFlow<SortFleet>
        get() = _currentFleetSort

    fun updateSort(type: SortFleet) {
        _currentFleetSort.value = type
    }
}