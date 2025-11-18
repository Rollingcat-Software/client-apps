package com.fivucsas.desktop.platform

import com.fivucsas.shared.platform.ILogger
import com.fivucsas.shared.platform.LogLevel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Desktop Logger Implementation
 *
 * Desktop implementation of ILogger using standard output.
 * Can be extended to write to files or logging frameworks.
 */
class DesktopLoggerImpl(
    private val minLevel: LogLevel = LogLevel.DEBUG
) : ILogger {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    override fun debug(tag: String, message: String) {
        log(LogLevel.DEBUG, tag, message)
    }

    override fun info(tag: String, message: String) {
        log(LogLevel.INFO, tag, message)
    }

    override fun warn(tag: String, message: String) {
        log(LogLevel.WARN, tag, message)
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        log(LogLevel.ERROR, tag, message, throwable)
    }

    override fun verbose(tag: String, message: String) {
        log(LogLevel.VERBOSE, tag, message)
    }

    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        if (level.ordinal < minLevel.ordinal) return

        val timestamp = LocalDateTime.now().format(dateFormatter)
        val levelStr = level.name.padEnd(7)
        val logMessage = "[$timestamp] $levelStr [$tag] $message"

        when (level) {
            LogLevel.ERROR -> {
                System.err.println(logMessage)
                throwable?.printStackTrace(System.err)
            }
            LogLevel.WARN -> {
                System.err.println(logMessage)
            }
            else -> {
                println(logMessage)
            }
        }
    }
}
