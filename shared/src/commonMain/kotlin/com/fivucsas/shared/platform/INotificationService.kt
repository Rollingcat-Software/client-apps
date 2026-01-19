package com.fivucsas.shared.platform

/**
 * Notification Service Interface (UI Port)
 *
 * Platform abstraction for notification operations following Hexagonal Architecture.
 * Allows domain layer to show notifications without depending on UI framework.
 *
 * Design Principles Applied:
 * - Dependency Inversion Principle (DIP): Domain layer depends on abstraction
 * - Interface Segregation Principle (ISP): Focused interface for notifications
 * - Single Responsibility Principle (SRP): Only handles notification operations
 *
 * Hexagonal Architecture Role: PORT (Primary/Driving Port)
 */
interface INotificationService {
    /**
     * Show a success notification
     * @param message Success message
     * @param duration Duration in milliseconds (null for auto)
     */
    fun showSuccess(message: String, duration: Long? = null)

    /**
     * Show an error notification
     * @param message Error message
     * @param duration Duration in milliseconds (null for auto)
     */
    fun showError(message: String, duration: Long? = null)

    /**
     * Show a warning notification
     * @param message Warning message
     * @param duration Duration in milliseconds (null for auto)
     */
    fun showWarning(message: String, duration: Long? = null)

    /**
     * Show an info notification
     * @param message Info message
     * @param duration Duration in milliseconds (null for auto)
     */
    fun showInfo(message: String, duration: Long? = null)

    /**
     * Clear all notifications
     */
    fun clearAll()
}

/**
 * Notification types
 */
enum class NotificationType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}
