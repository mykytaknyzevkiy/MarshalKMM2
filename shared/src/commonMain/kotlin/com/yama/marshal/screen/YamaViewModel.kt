package com.yama.marshal.screen

import co.touchlab.kermit.Logger
import kotlinx.coroutines.*

abstract class YamaViewModel {
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(tag = "YamaViewModel", throwable = throwable, message = {
            "Error in some view model"
        })
    }

    private val job = Job()
    val viewModelScope = CoroutineScope(Dispatchers.Default) + errorHandler + job

    fun onClear() {
        job.cancel()
        viewModelScope.cancel()
    }
}