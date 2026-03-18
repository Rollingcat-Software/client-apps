package com.fivucsas.shared.platform

/**
 * No-op Push Notification Service implementation.
 *
 * Used as a default when Firebase/APNs is not configured.
 * This allows the app to compile and run without Firebase credentials.
 */
class NoOpPushNotificationService : IPushNotificationService {
    override suspend fun registerToken(userId: String, token: String) {
        // No-op: Firebase not configured
    }

    override suspend fun getToken(): String? = null

    override fun isSupported(): Boolean = false
}
