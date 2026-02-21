package com.fivucsas.shared.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

class AppNavigator(start: AppRoute) {
    private val backStack = mutableStateListOf(start)

    val currentRoute: AppRoute
        get() = backStack.last()

    val canPop: Boolean
        get() = backStack.size > 1

    fun navigate(
        route: AppRoute,
        replace: Boolean = false,
        clearBackStack: Boolean = false
    ) {
        when {
            clearBackStack -> {
                backStack.clear()
                backStack.add(route)
            }
            replace -> {
                backStack[backStack.lastIndex] = route
            }
            else -> backStack.add(route)
        }
    }

    fun pop(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}

@Composable
fun rememberAppNavigator(start: AppRoute = AppRoute.Splash): AppNavigator {
    return remember { AppNavigator(start) }
}
