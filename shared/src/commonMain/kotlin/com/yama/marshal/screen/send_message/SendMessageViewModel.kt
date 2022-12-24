package com.yama.marshal.screen.send_message

import androidx.compose.runtime.mutableStateListOf
import com.yama.marshal.data.entity.CompanyMessage
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.screen.YamaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class SendMessageViewState {
    object Empty: SendMessageViewState()
    object Loading: SendMessageViewState()
    object Success: SendMessageViewState()
}

class SendMessageViewModel : YamaViewModel() {
    private val _messagesList = mutableStateListOf<CompanyMessage>()
    val messages: List<CompanyMessage>
        get() = _messagesList

    private val _currentState = MutableStateFlow<SendMessageViewState>(SendMessageViewState.Empty)
    val currentState: StateFlow<SendMessageViewState>
        get() = _currentState

    fun loadMessages() {
        CompanyRepository
            .companyMessages
            .onEach {
                _messagesList.clear()
                _messagesList.addAll(it)
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(cartID: Int, message: String) = viewModelScope.launch {
        if (message.isBlank())
            return@launch

        _currentState.emit(SendMessageViewState.Loading)

        val isSuccess = if (_messagesList.any { it.message == message })
            CompanyRepository.sendMessageToCarts(
                cartIds = intArrayOf(cartID),
                idMessage = _messagesList.find { it.message == message }?.id ?: return@launch
            )
        else
            CompanyRepository.sendMessageToCarts(
                cartIds = intArrayOf(cartID),
                message = message
            )

        if (isSuccess)
            _currentState.emit(SendMessageViewState.Success)
        else
            _currentState.emit(SendMessageViewState.Empty)
    }

    fun emptyState() {
        _currentState.value = SendMessageViewState.Empty
    }
}