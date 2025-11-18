package com.fivucsas.shared.platform

/**
 * Logger Interface
 *
 * Platform abstraction for logging.
 * Enables testability and consistent logging across platforms.
 */
interface ILogger {
    /**
     * Log a debug message
     * @param tag Log tag/category
     * @param message Log message
     */
    fun debug(tag: String, message: String)

    /**
     * Log an info message
     * @param tag Log tag/category
     * @param message Log message
     */
    fun info(tag: String, message: String)

    /**
     * Log a warning message
     * @param tag Log tag/category
     * @param message Log message
     */
    fun warn(tag: String, message: String)

    /**
     * Log an error message
     * @param tag Log tag/category
     * @param message Log message
     * @param throwable Optional exception
     */
    fun error(tag: String, message: String, throwable: Throwable? = null)

    /**
     * Log a verbose message
     * @param tag Log tag/category
     * @param message Log message
     */
    fun verbose(tag: String, message: String)
}

/**
 * Log Level Enum
 */
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}
