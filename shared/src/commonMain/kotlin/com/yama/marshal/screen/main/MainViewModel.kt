package com.yama.marshal.screen.main

import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.tool.format
import io.ktor.util.date.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class MainViewModel : YamaViewModel() {
    val clock = flow {
        while (true) {
            emit(GMTDate())
            delay(60 * 1000)
        }
    }.map {
        it.format("hh:mm a")
    }

}