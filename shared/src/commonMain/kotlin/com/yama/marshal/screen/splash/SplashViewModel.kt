package com.yama.marshal.screen.splash

import com.yama.marshal.repository.CompanyRepository
import com.yama.marshal.repository.UserRepository
import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.screen.login.UserDataViewModel
import com.yama.marshal.service.MarshalNotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SplashViewState {
    object Loading: SplashViewState()
    object RequestLogin: SplashViewState()
    object OK: SplashViewState()
}

class SplashViewModel : YamaViewModel(), UserDataViewModel {
    private val _currentViewState = MutableStateFlow<SplashViewState>(
        SplashViewState.Loading
    )
    val currentViewState: StateFlow<SplashViewState>
        get() = _currentViewState

    override val userRepository = UserRepository()
    
    fun startData() = viewModelScope.launch {
        _currentViewState.value = SplashViewState.Loading

        if (!userRepository.isUserLogin()) {
            _currentViewState.value = SplashViewState.RequestLogin
            return@launch
        }

        loadData().also {
            if (!it) {
                _currentViewState.value = SplashViewState.RequestLogin
                return@launch
            }
        }

        _currentViewState.value = SplashViewState.OK
    }
}