package com.yama.marshal.network

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

actual class MarshalSocket: CoroutineScope, MarshalSocketIO {
    companion object {
        private const val TAG = "MarshalSocket"
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        onError(throwable.message ?: "Unknown")
    }

    actual fun connect() = launch {

    }

    actual fun disconnect() {
    }

    actual fun sendMessage(topic: String, message: String) {
    }

    private fun login() = launch {

    }
}