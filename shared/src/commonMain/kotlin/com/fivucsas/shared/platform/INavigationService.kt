package com.fivucsas.shared.platform

/**
 * Navigation Service Interface (UI Port)
 *
 * Platform abstraction for navigation operations following Hexagonal Architecture.
 * This port allows the domain layer to trigger navigation without depending on UI framework.
 *
 * Design Principles Applied:
 * - Dependency Inversion Principle (DIP): Domain layer depends on abstraction
 * - Interface Segregation Principle (ISP): Focused interface for navigation
 * - Single Responsibility Principle (SRP): Only handles navigation
 *
 * Hexagonal Architecture Role: PORT (Primary/Driving Port)
 */
interface INavigationService {
    /**
     * Navigate to a specific screen
     * @param route Screen route identifier
     * @param params Optional navigation parameters
     */
    fun navigateTo(route: String, params: Map<String, Any> = emptyMap())

    /**
     * Navigate back to previous screen
     * @return true if navigation successful, false if no previous screen
     */
    fun navigateBack(): Boolean

    /**
     * Clear navigation stack and navigate to route
     * @param route Screen route identifier
     */
    fun navigateAndClearStack(route: String)

    /**
     * Pop back stack up to a specific route
     * @param route Target route
     * @param inclusive Whether to include the target route in pop
     */
    fun popUpTo(route: String, inclusive: Boolean = false)
}

/**
 * Common navigation routes
 */
object NavigationRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val KIOSK = "kiosk"
    const val ENROLLMENT = "enrollment"
    const val VERIFICATION = "verification"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
}
