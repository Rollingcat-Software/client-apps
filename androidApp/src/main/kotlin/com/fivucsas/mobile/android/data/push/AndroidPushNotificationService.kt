package com.fivucsas.mobile.android.data.push

import android.util.Log
import com.fivucsas.shared.data.remote.api.DeviceApi
import com.fivucsas.shared.platform.IPushNotificationService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Android Push Notification Service — real FCM implementation.
 *
 * Uses Firebase Cloud Messaging to:
 * 1. Retrieve the device FCM token
 * 2. Register the token with the backend (POST /devices/push-token)
 *
 * Requires a valid google-services.json in androidApp/.
 * Falls back gracefully if Firebase is not configured (catches all exceptions).
 */
class AndroidPushNotificationService(
    private val deviceApi: DeviceApi
) : IPushNotificationService {

    companion object {
        private const val TAG = "FCM-Push"
    }

    override suspend fun registerToken(userId: String, token: String) {
        try {
            deviceApi.registerPushToken(userId, token, "ANDROID")
            Log.i(TAG, "Push token registered for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register push token: ${e.message}", e)
        }
    }

    override suspend fun getToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM token retrieved (${token.take(10)}...)")
            token
        } catch (e: Exception) {
            Log.w(TAG, "FCM token unavailable (Firebase not configured?): ${e.message}")
            null
        }
    }

    override fun isSupported(): Boolean {
        return try {
            // Check if Firebase is initialized by attempting to get the instance
            FirebaseMessaging.getInstance()
            true
        } catch (e: Exception) {
            false
        }
    }
}
