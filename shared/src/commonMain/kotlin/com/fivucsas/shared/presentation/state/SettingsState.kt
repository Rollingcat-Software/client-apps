package com.fivucsas.shared.presentation.state

/**
 * Settings State
 *
 * Represents all configurable settings in the admin dashboard.
 * Immutable data class following clean architecture principles.
 */
data class SettingsState(
    // System Settings
    val apiEndpoint: String = "http://localhost:8080/api",
    val biometricProcessorUrl: String = "http://localhost:8081",
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,

    // Security Settings
    val sessionTimeoutMinutes: Int = 30,
    val failedLoginThreshold: Int = 5,
    val accountLockoutMinutes: Int = 15,
    val passwordMinLength: Int = 8,
    val requireSpecialChars: Boolean = true,
    val requireNumbers: Boolean = true,
    val requireUppercase: Boolean = true,

    // Biometric Settings
    val livenessThreshold: Float = 0.7f,
    val faceMatchingThreshold: Float = 0.85f,
    val minEnrollmentQuality: Int = 80,
    val enableFaceRecognition: Boolean = true,
    val enableFingerprintRecognition: Boolean = false,
    val enableIrisRecognition: Boolean = false,

    // Notification Settings
    val emailNotificationsEnabled: Boolean = true,
    val securityAlertThreshold: Int = 3,
    val notificationRecipients: List<String> = listOf("admin@fivucsas.com"),

    // Appearance Settings
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: String = "en",
    val dateFormat: String = "yyyy-MM-dd",
    val timeFormat: TimeFormat = TimeFormat.HOUR_24,

    // Maintenance
    val lastCacheClear: String? = null,
    val lastBackup: String? = null,
    val systemHealthStatus: HealthStatus = HealthStatus.GOOD
)

/**
 * Connection status enum
 */
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    ERROR,
    CHECKING
}

/**
 * Application theme enum
 */
enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Time format enum
 */
enum class TimeFormat {
    HOUR_12,
    HOUR_24
}

/**
 * System health status
 */
enum class HealthStatus {
    GOOD,
    WARNING,
    CRITICAL,
    UNKNOWN
}
