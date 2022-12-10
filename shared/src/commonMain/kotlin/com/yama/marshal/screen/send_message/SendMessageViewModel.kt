package com.yama.marshal.screen.send_message

import androidx.compose.runtime.mutableStateListOf
import com.yama.marshal.data.entity.CompanyMessage
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.screen.YamaViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SendMessageViewState {
    object Empty: SendMessageViewState()
    object Loading: SendMessageViewState()
    object Success: SendMessageViewState()
}

class SendMessageViewModel : YamaViewModel() {
    private val companyRepository = CompanyRepository()

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String>
        get() = _currentMessage

    private val _messagesList = mutableStateListOf<CompanyMessage>()
    val messages: List<CompanyMessage>
        get() = _messagesList

    private val _currentState = MutableStateFlow<SendMessageViewState>(SendMessageViewState.Empty)
    val currentState: StateFlow<SendMessageViewState>
        get() = _currentState

    fun updateMessage(data: String) {
        _currentMessage.value = data
    }

    fun loadMessages() {
        companyRepository
            .companyMessages
            .onEach {
                _messagesList.clear()
                _messagesList.addAll(it)
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(cartID: Int) = viewModelScope.launch {
        _currentState.emit(SendMessageViewState.Loading)

        (if (_messagesList.any { it.message == _currentMessage.value })
            companyRepository.sendMessageToCarts(
                cartIds = intArrayOf(cartID),
                idMessage = _messagesList.find { it.message == _currentMessage.value }?.id ?: return@launch
            )
        else
            companyRepository.sendMessageToCarts(
                cartIds = intArrayOf(cartID),
                message = _currentMessage.value
            )).also {
                if (it)
                    _currentState.emit(SendMessageViewState.Success)
            else
                _currentState.emit(SendMessageViewState.Empty)
        }
    }
}