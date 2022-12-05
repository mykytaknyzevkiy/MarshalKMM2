package com.yama.marshal.screen.login

import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.repository.UserRepository
import com.yama.marshal.screen.YamaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface UserDataViewModel {
    val userRepository: UserRepository
    val companyRepository: CompanyRepository

    suspend fun loadData(): Boolean {
        userRepository.userData().also {
            if (!it)
                return false
        }

        companyRepository.loadCourses().also {
            if (!it)
                return false
        }

        return true
    }
}

sealed class LoginViewState {
    object Empty: LoginViewState()
    object Loading: LoginViewState()
    object Error: LoginViewState()
    object OK: LoginViewState()
}

class LoginViewModel : YamaViewModel(), UserDataViewModel {
    private val _currentViewState = MutableStateFlow<LoginViewState>(LoginViewState.Empty)
    val currentState: StateFlow<LoginViewState>
        get() = _currentViewState

    override val userRepository = UserRepository()

    override val companyRepository: CompanyRepository = CompanyRepository()

    fun login(userName: String, password: String) = viewModelScope.launch {
        _currentViewState.emit(LoginViewState.Loading)

        userRepository.login(
            userName, password
        ).also {
            if (!it) {
                _currentViewState.emit(LoginViewState.Error)
                return@launch
            }
        }

        loadData().also {
            if (!it) {
                _currentViewState.emit(LoginViewState.Error)
                return@launch
            }
        }

        _currentViewState.emit(LoginViewState.OK)
    }

}