package com.yama.marshal.tool

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

internal class YamPaging<T> constructor(
    private val fullFlow: Flow<List<T>>,
    private val scope: CoroutineScope
) {
    private val _list = MutableStateFlow(emptyList<T>())
    val list: StateFlow<List<T>>
        get() = _list

    private val nIndex = MutableStateFlow(Pair(0, 0))

    init {
        readFullFlow()
    }

    private fun readFullFlow() {
        fullFlow
            .combine(nIndex) { a, b ->
                a.subList(b.first, a.size)
            }
            .onEach {
                _list.value = it
            }
            .launchIn(scope)
    }

    fun load(firstVisibleIndex: Int, last: Int) {
        nIndex.value = Pair(firstVisibleIndex, last)
    }
}

internal fun <T> Flow<List<T>>.toYamaPagingState(scope: CoroutineScope): YamPaging<T> {
    return YamPaging(this, scope)
}