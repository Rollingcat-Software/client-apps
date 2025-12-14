package com.fivucsas.shared.platform

import platform.Foundation.NSLog

/**
 * iOS Logger Implementation
 *
 * Uses NSLog for logging on iOS devices.
 * Follows Hexagonal Architecture by implementing ILogger interface.
 *
 * Design Principles Applied:
 * - Dependency Inversion Principle: Implements platform abstraction
 * - Single Responsibility: Handles only logging operations
 * - Open/Closed Principle: Extensible through interface
 *
 * iOS Logging Features:
 * - NSLog: Standard iOS logging to system console
 * - Timestamps automatically added
 * - Visible in Xcode console and device logs
 * - Can be viewed using Console.app on macOS
 *
 * Log Format: [TAG] LEVEL: Message
 */
class IosLogger : ILogger {

    /**
     * Log a debug message
     * Used for detailed diagnostic information
     */
    override fun debug(tag: String, message: String) {
        log(LogLevel.DEBUG, tag, message)
    }

    /**
     * Log an info message
     * Used for general informational messages
     */
    override fun info(tag: String, message: String) {
        log(LogLevel.INFO, tag, message)
    }

    /**
     * Log a warning message
     * Used for potentially harmful situations
     */
    override fun warn(tag: String, message: String) {
        log(LogLevel.WARN, tag, message)
    }

    /**
     * Log an error message
     * Used for error events that might still allow the app to continue
     */
    override fun error(tag: String, message: String, throwable: Throwable?) {
        val errorMessage = if (throwable != null) {
            "$message\nException: ${throwable.message}\nStackTrace: ${throwable.stackTraceToString()}"
        } else {
            message
        }
        log(LogLevel.ERROR, tag, errorMessage)
    }

    /**
     * Log a verbose message
     * Used for most detailed diagnostic information
     */
    override fun verbose(tag: String, message: String) {
        log(LogLevel.VERBOSE, tag, message)
    }

    /**
     * Internal logging method that formats and outputs to NSLog
     */
    private fun log(level: LogLevel, tag: String, message: String) {
        val emoji = when (level) {
            LogLevel.VERBOSE -> "💬"
            LogLevel.DEBUG -> "🔍"
            LogLevel.INFO -> "ℹ️"
            LogLevel.WARN -> "⚠️"
            LogLevel.ERROR -> "❌"
        }

        val formattedMessage = "$emoji [$tag] ${level.name}: $message"
        NSLog(formattedMessage)
    }
}

/**
 * iOS-specific Logger Extensions
 */
object IosLoggerHelper {
    /**
     * Create a logger instance
     */
    fun createLogger(): ILogger = IosLogger()

    /**
     * Log tags for common iOS modules
     */
    object Tags {
        const val CAMERA = "iOS.Camera"
        const val STORAGE = "iOS.Storage"
        const val BIOMETRIC = "iOS.Biometric"
        const val NETWORK = "iOS.Network"
        const val UI = "iOS.UI"
        const val LIFECYCLE = "iOS.Lifecycle"
        const val PERMISSIONS = "iOS.Permissions"
    }
}
