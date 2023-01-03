package com.yama.marshal.screen.map

import co.touchlab.kermit.Logger
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.repository.CartRepository
import com.yama.marshal.repository.CourseRepository
import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.tool.filterList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class MapViewModel : YamaViewModel() {
    companion object {
        private const val TAG = "MapViewModel"
    }

    private val _holeState = MutableStateFlow<CourseFullDetail.HoleData?>(null)
    val holeState: StateFlow<CourseFullDetail.HoleData?>
        get() = _holeState

    private val _courseState = MutableStateFlow<CourseFullDetail?>(null)
    val courseState: StateFlow<CourseFullDetail?>
        get() = _courseState

    private val _cartsState = MutableStateFlow<List<CartFullDetail>>(emptyList())
    val cartsState: StateFlow<List<CartFullDetail>>
        get() = _cartsState

    private val _cartIDState = MutableStateFlow(-1)
    val cartIDState: StateFlow<Int>
        get() = _cartIDState

    @OptIn(ExperimentalCoroutinesApi::class)
    val cartsLocationUpdater = channelFlow {
        while (!isClosedForSend) {
            Logger.d(TAG, message = {
                "on carts location update"
            })

            val cartIds = cartsState.value.map { it.id }.toIntArray()

            CartRepository.loadUpdateCartsLocation(cartIds)

            delay(1000L * 60 * 2)

            send(1)
        }
    }

    fun loadHole(id: Int, courseID: String) {
        CourseRepository
            .findHole(id, courseID)
            .onEach {
                _holeState.emit(it)
            }
            .launchIn(viewModelScope)

        CartRepository
            .cartActiveList
            .filterList {
                it.currPosHole == id
            }
            .onEach {
                _cartsState.value = it
            }
            .launchIn(viewModelScope)
    }

    fun loadCourse(id: String) {
        CourseRepository
            .findCourse(id)
            .onEach {
                _courseState.emit(it)
            }
            .launchIn(viewModelScope)
    }

    fun loadCart(id: Int) {
        _cartIDState.value = id

        CartRepository
            .findCart(id)
            .filter { it != null }
            .map { it!! }
            .onEach {
                _cartsState.value = listOf(it)
                _holeState.value = it.hole
            }
            .launchIn(viewModelScope)
    }

    override fun onClear() {
        super.onClear()
        _cartIDState.value = -1
        _holeState.value = null
        _courseState.value = null
        _cartsState.value = emptyList()
    }
}