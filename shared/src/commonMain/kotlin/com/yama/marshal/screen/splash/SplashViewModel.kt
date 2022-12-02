package com.yama.marshal.screen.splash

import com.yama.marshal.repository.UserRepository
import com.yama.marshal.screen.YamaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class SplashViewState {
    object Loading: SplashViewState()
    object RequestLogin: SplashViewState()
    object OK: SplashViewState()
}

class SplashViewModel : YamaViewModel() {
    private val _currentViewState = MutableStateFlow<SplashViewState>(
        SplashViewState.Loading
    )
    val currentViewState: StateFlow<SplashViewState>
        get() = _currentViewState

    private val userRepository = UserRepository()

    fun loadData() {
        _currentViewState.value = SplashViewState.Loading

        if (!userRepository.isUserLogin()) {
            _currentViewState.value = SplashViewState.RequestLogin
            return
        }
    }
}