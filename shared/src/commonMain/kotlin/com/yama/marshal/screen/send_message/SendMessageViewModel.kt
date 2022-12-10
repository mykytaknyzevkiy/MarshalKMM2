package com.yama.marshal.screen.send_message

import com.yama.marshal.screen.YamaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SendMessageViewModel : YamaViewModel() {
    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String>
        get() = _currentMessage

    fun updateMessage(data: String) {
        _currentMessage.value = data
    }
}