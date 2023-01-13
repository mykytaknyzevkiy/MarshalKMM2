package com.yama.marshal.tool

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class YamaList <E> (val flow: Flow<List<E>>,
                             private val scope: CoroutineScope) {
    private val stateList = mutableStateListOf<E>()
    val list: List<E>
        get() = stateList

    var scrollConnection: ScrollState? = null
    var lazyScroll: LazyListState? = null

    fun launch() {
        flow
            .onEach {
                stateList.clear()
                stateList.addAll(it)
            }
            .launchIn(scope)
    }

    suspend fun scrollTo(position: Int) {
        if (lazyScroll != null)
            lazyScroll?.scrollToItem(position)
        if (scrollConnection != null) {
            scrollConnection?.animateScrollTo(0)
        }
    }
}

internal fun <E> Flow<List<E>>.toStateList(scope: CoroutineScope) = YamaList(this, scope)