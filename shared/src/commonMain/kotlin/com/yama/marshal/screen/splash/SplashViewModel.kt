package com.yama.marshal.screen.splash

import com.yama.marshal.screen.YamaViewModel

sealed class SplashViewState {
    object Loading: SplashViewState()
    object RequestLogin: SplashViewState()
    object OK: SplashViewState()
}

class SplashViewModel : YamaViewModel() {
    private val _curret

}