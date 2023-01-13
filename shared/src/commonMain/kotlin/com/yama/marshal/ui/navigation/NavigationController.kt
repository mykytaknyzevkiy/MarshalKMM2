package com.yama.marshal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal data class NavArg(
    internal val key: String,
    internal val value: Any?
)

internal data class StackRoute internal constructor(
    val route: String,
    val args: List<NavArg>
)

@Composable
internal fun rememberNavController(currentRoute: String): NavigationController {
    return remember { NavigationController(currentRoute) }
}

internal class NavigationController constructor(currentRoute: String) {
    private val stackRoutes = linkedSetOf<StackRoute>()

    private val _currentState = MutableStateFlow(StackRoute(route = currentRoute, args = emptyList()))
    internal val currentRoute: StateFlow<StackRoute>
        get() = _currentState

    fun popBack() {
        if (stackRoutes.isEmpty())
            return

        val route = stackRoutes.last()
        _currentState.value = route
        stackRoutes.remove(route)
    }

    fun navigateTo(route: String, args: List<NavArg> = emptyList()) {
        stackRoutes.add(_currentState.value)

        navigateToAndFinish(route, args)
    }

    fun navigateToAndFinish(route: String, args: List<NavArg> = emptyList()) {
        _currentState.value = StackRoute(route = route, args = args)
    }

    fun isBackStackEmpty() = stackRoutes.isEmpty()
}

internal fun List<NavArg>.findInt(key: String): Int? =
    this.find { it.key == key }?.value.let { if (it == null) null else it as Int }

internal fun List<NavArg>.findString(key: String): String? =
    this.find { it.key == key }?.value.let { if (it == null) null else it as String }