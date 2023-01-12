package com.yama.marshal.tool

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class YamaList <E> (private val flow: Flow<List<E>>) {
    private val stateList = mutableStateListOf<E>()

    @Composable
    fun collect(): List<E> {
        val scope = rememberCoroutineScope()

        flow
            .onEach {
                stateList.clear()
                stateList.addAll(it)
            }
            .launchIn(scope)

        return stateList
    }
}

internal fun <E> Flow<List<E>>.toStateList() = YamaList(this)