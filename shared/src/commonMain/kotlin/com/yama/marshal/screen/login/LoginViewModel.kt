package com.yama.marshal.screen.login

import com.yama.marshal.repository.UserRepository
import com.yama.marshal.screen.YamaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginViewState {
    object Empty: LoginViewState()
    object Loading: LoginViewState()
    object Error: LoginViewState()
    object OK: LoginViewState()
}

class LoginViewModel : YamaViewModel() {
    private val _currentViewState = MutableStateFlow<LoginViewState>(LoginViewState.Empty)
    val currentState: StateFlow<LoginViewState>
        get() = _currentViewState

    private val userRepository = UserRepository()

    fun login(userName: String, password: String) = viewModelScope.launch {
        _currentViewState.emit(LoginViewState.Loading)

        userRepository.login(
            userName, password
        ).also {
            if (it)
                _currentViewState.emit(LoginViewState.OK)
            else
                _currentViewState.emit(LoginViewState.Error)
        }
    }

}