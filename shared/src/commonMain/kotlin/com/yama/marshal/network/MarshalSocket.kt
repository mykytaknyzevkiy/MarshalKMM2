package com.yama.marshal.network

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class MarshalSocketIO {
    companion object {
        private const val TAG = "MarshalSocket"
    }

    protected fun onError(message: String) {
        Logger.e(TAG, message = { "onError $message" })
    }

    protected fun onMessage(json: String) {
        Logger.i(TAG, message = { "onMessage $json" })
    }
}

expect class MarshalSocket() : CoroutineScope, MarshalSocketIO {

    fun connect(): Job

    fun disconnect()

    fun sendMessage(topic: String, json: String)
}