package com.yama.marshal.screen.main

import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CartMessageModel
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.repository.CartRepository
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.repository.CourseRepository
import com.yama.marshal.repository.UserRepository
import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.screen.login.UserDataViewModel
import com.yama.marshal.service.MarshalNotificationService
import com.yama.marshal.tool.*
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
        CAR(Strings.fleet_list_screen_table_row_car_label, 0.6f),

        START_TIME(Strings.fleet_list_screen_table_row_start_time_label, 0.8f),

        PLACE_OF_PLAY(Strings.fleet_list_screen_table_row_place_of_place_label, 1f),

        HOLE(Strings.fleet_list_screen_table_row_hole_label, 0.6f)
    }

    enum class SortHole(
        override val label: String,
        override val weight: Float
    ) : SortType {
        HOLE(Strings.hole_screen_table_row_hole_label, 0.3f),

        PACE_OF_PLAY(Strings.hole_screen_table_row_pace_of_play_label, 1f)
    }
}

sealed class MainFullReloadState {
    object Empty : MainFullReloadState()

    object Loading : MainFullReloadState()
}

class MainViewModel : YamaViewModel(), UserDataViewModel {
    override val userRepository: UserRepository = UserRepository()

    val clock = flow {
        while (true) {
            emit(GMTDate())
            delay(60 * 1000)
        }
    }.map {
        it.format("hh:mm a")
    }

    private val _currentFleetSort = MutableStateFlow(Pair(SortType.SortFleet.CAR, false))
    val currentFleetSort: StateFlow<Pair<SortType.SortFleet, Boolean>>
        get() = _currentFleetSort

    private val _currentHoleSort = MutableStateFlow(Pair(SortType.SortHole.HOLE, false))
    val currentHoleSort: StateFlow<Pair<SortType.SortHole, Boolean>>
        get() = _currentHoleSort

    private val _selectedCourse = MutableStateFlow<CourseFullDetail?>(null)
    val selectedCourse: StateFlow<CourseFullDetail?>
        get() = _selectedCourse

    val cartMessages: List<CartMessageModel>
        get() = CartRepository.cartMessages

    private val _fullReloadState = MutableStateFlow<MainFullReloadState>(MainFullReloadState.Empty)
    val fullReloadState: StateFlow<MainFullReloadState>
        get() = _fullReloadState

    val courseList = CourseRepository
        .courseList
        .map {
            it.toMutableList().apply {
                add(
                    0, CourseFullDetail(
                        id = null,
                        courseName = "All",
                        defaultCourse = 0,
                        playersNumber = 0,
                        layoutHoles = null,
                        holes = emptyList(),
                        vectors = ""
                    )
                )
            }
        }
        .onEach {
            if (_selectedCourse.value == null)
                _selectedCourse.emit(if (it.size == 1) it.first() else it[1])
        }

    internal val fleetList = CartRepository
        .cartList
        //.filterList { it.lastActivity?.isBeforeDate(GMTDate()) == false }
        .combine(_selectedCourse) { a, b ->
            if (b?.id.isNullOrBlank())
                a
            else
                a.filter { c -> c.course?.id == b?.id }
        }
        .combine(_currentFleetSort) { a, b ->
            a.sortedWith(FleetSorter(b.first, b.second))
        }
        .toStateList()

    internal val holeList = CourseRepository
        .holeList
        .combine(_selectedCourse) { a, b ->
            if (b?.id.isNullOrBlank())
                a
            else
                a.filter { it.idCourse == b?.id }
        }
        .combine(_currentHoleSort) { a, b ->
            a.sortedWith(HoleSorter(b.first, b.second))
        }
        .toStateList()

    internal val alertList = CompanyRepository
        .alerts
        .combine(_selectedCourse) { a, b ->
            a.filter { b?.id.isNullOrBlank() || it.courseID == b?.id }
        }
        .map {
            it.distinctBy { i -> i.id }
        }
        .map {
            it.reversed()
        }
        .flowOn(Dispatchers.Default)
        .toStateList()


    fun updateSort(type: SortType.SortFleet) {
        _currentFleetSort.value = Pair(
            type,
            _currentFleetSort.value.first == type && !_currentFleetSort.value.second
        )
    }

    fun updateSort(type: SortType.SortHole) {
        _currentHoleSort.value = Pair(
            type,
            _currentHoleSort.value.first == type && !_currentHoleSort.value.second
        )
    }

    fun selectCourse(course: CourseFullDetail) {
        _selectedCourse.value = course
    }

    fun flagCart(cart: CartFullDetail) = viewModelScope.launch {
        CartRepository.flagCart(cart.id)
    }

    fun unFlagCart(cart: CartFullDetail) = viewModelScope.launch {
        CartRepository.unFlagCart(cart.id)
    }

    fun shutDown(cartID: Int) = viewModelScope.launch {
        CartRepository.shutDown(cartID)
    }

    fun restore(cartID: Int) = viewModelScope.launch {
        CartRepository.restore(cartID)
    }

    fun logOut() {
        UserRepository().logOut()
        MarshalNotificationService.stop()
    }

    fun forceReload() = viewModelScope.launch {
        _fullReloadState.emit(MainFullReloadState.Loading)

        loadData()

        _fullReloadState.emit(MainFullReloadState.Empty)
    }

    override fun onClear() {
        super.onClear()
        _selectedCourse.value = null
    }
}