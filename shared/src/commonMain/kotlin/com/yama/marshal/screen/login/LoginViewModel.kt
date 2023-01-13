package com.yama.marshal.screen.login

import com.yama.marshal.repository.CartRepository
import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.repository.CourseRepository
import com.yama.marshal.repository.UserRepository
import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.service.MarshalNotificationService
import com.yama.marshal.tool.prefs
import com.yama.marshal.tool.userName
import com.yama.marshal.tool.userPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal interface UserDataViewModel {
    val userRepository: UserRepository
    
    suspend fun loadData(): Boolean {
        val userName = prefs.userName
        val userPassword = prefs.userPassword

        userRepository.login(
            userName ?: return false,
            userPassword ?: return false
        )

        userRepository.userData().also {
            if (!it)
                return false
        }

        CourseRepository.loadCourses().also {
            if (!it)
                return false
        }

        CourseRepository.loadHoles().also {
            if (!it)
                return false
        }

        CartRepository.loadCarts().also {
            if (!it)
                return false
        }

        CartRepository.loadCartsRound().also {
            if (!it)
                return false
        }

        CompanyRepository.loadMessages().also {
            if (!it)
                return false
        }

        CompanyRepository.loadGeofenceList().also {
            if (!it)
                return false
        }

        MarshalNotificationService.restart()

        return true
    }
}

internal sealed class LoginViewState {
    object Empty: LoginViewState()
    object Loading: LoginViewState()
    object Error: LoginViewState()
    object OK: LoginViewState()
}

internal class LoginViewModel : YamaViewModel(), UserDataViewModel {
    private val _currentViewState = MutableStateFlow<LoginViewState>(LoginViewState.Empty)
    val currentState: StateFlow<LoginViewState>
        get() = _currentViewState

    override val userRepository = UserRepository()

    fun login(userName: String, password: String) = viewModelScope.launch {
        _currentViewState.emit(LoginViewState.Loading)

        prefs.userName = userName
        prefs.userPassword = password

        loadData().also {
            if (!it) {
                _currentViewState.emit(LoginViewState.Error)
                return@launch
            }
        }

        _currentViewState.emit(LoginViewState.OK)
    }

    override fun onClear() {
        _currentViewState.value = LoginViewState.Empty
    }
}