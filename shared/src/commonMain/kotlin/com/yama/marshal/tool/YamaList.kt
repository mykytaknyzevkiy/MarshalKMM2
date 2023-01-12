package com.yama.marshal.tool

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class YamaList <E> (private val flow: Flow<List<E>>) {
    private val stateList = mutableStateListOf<E>()

    private var scrollConnection: ScrollState? = null
    private var lazyScroll: LazyListState? = null

    @Composable
    fun collect(): List<E> {
        val scope = rememberCoroutineScope()

        var lastFirstItem: E? = null

        flow
            .onEach {
                stateList.clear()
                stateList.addAll(it)

                if (it.isNotEmpty()) {
                    if (lastFirstItem != it.first())
                        scrollTo(0)
                    lastFirstItem = it.first()
                }
            }
            .launchIn(scope)

        return stateList
    }

    @Composable
    fun collect(scrollConnection: ScrollState): List<E> {
        this.scrollConnection = scrollConnection

        val scope = rememberCoroutineScope()

        var lastFirstItem: E? = null

        flow
            .onEach {
                stateList.clear()
                stateList.addAll(it)

                if (it.isNotEmpty()) {
                    if (lastFirstItem != it.first())
                        scrollTo(0)
                    lastFirstItem = it.first()
                }
            }
            .launchIn(scope)

        return stateList
    }

    @Composable
    fun collect(lazyScroll: LazyListState): List<E> {
        this.lazyScroll = lazyScroll

        val scope = rememberCoroutineScope()

        var lastFirstItem: E? = null

        flow
            .onEach {
                stateList.clear()
                stateList.addAll(it)

                if (it.isNotEmpty()) {
                    if (lastFirstItem != it.first())
                        scrollTo(0)
                    lastFirstItem = it.first()
                }
            }
            .launchIn(scope)

        return stateList
    }

    suspend fun scrollTo(position: Int) {
        if (lazyScroll != null)
            lazyScroll?.scrollToItem(position)
        if (scrollConnection != null)
            scrollConnection?.scrollTo(0)
    }
}

internal fun <E> Flow<List<E>>.toStateList() = YamaList(this)