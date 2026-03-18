package com.fivucsas.shared.platform

/**
 * Platform abstraction for push notification services (FCM/APNs).
 *
 * Android: Implemented using Firebase Cloud Messaging
 * iOS: Would use APNs (Apple Push Notification Service)
 * Desktop: Not supported
 *
 * This is a prep interface — implementations are stubs until
 * Firebase credentials are configured per platform.
 */
interface IPushNotificationService {
    /**
     * Register the device's push notification token with the backend.
     *
     * @param userId The user ID to associate with this device token
     * @param token The FCM/APNs device token
     */
    suspend fun registerToken(userId: String, token: String)

    /**
     * Retrieve the current device push notification token, if available.
     *
     * @return The FCM/APNs token, or null if not available
     */
    suspend fun getToken(): String?

    /**
     * Check if push notifications are supported on this platform.
     *
     * @return true if the platform supports push notifications
     */
    fun isSupported(): Boolean
}
