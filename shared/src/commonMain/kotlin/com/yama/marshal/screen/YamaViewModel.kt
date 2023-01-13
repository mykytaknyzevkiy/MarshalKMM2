package com.yama.marshal.screen

import co.touchlab.kermit.Logger
import kotlinx.coroutines.*

internal abstract class YamaViewModel {
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Logger.e(tag = "YamaViewModel", throwable = throwable, message = {
            "Error in some view model"
        })
    }

    private val job = Job()
    protected val viewModelScope = CoroutineScope(Dispatchers.Default) + errorHandler + job

    open fun onClear() {
        job.cancelChildren()
    }
}