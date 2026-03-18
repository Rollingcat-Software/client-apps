package com.fivucsas.mobile.android.data.push

import com.fivucsas.shared.platform.IPushNotificationService

/**
 * Android Push Notification Service stub.
 *
 * This is a prep implementation that compiles and runs without Firebase.
 * When Firebase credentials are available, this class will be updated to:
 * 1. Use FirebaseMessaging.getInstance().token to get the FCM token
 * 2. Register the token with the backend via API call
 *
 * The infrastructure is ready — just add firebase-messaging dependency
 * and implement the TODO sections.
 */
class AndroidPushNotificationService : IPushNotificationService {

    override suspend fun registerToken(userId: String, token: String) {
        // TODO: implement when Firebase credentials are available
        // Call backend API: POST /api/v1/devices/{userId}/push-token
        // with body: { "token": token, "platform": "ANDROID" }
    }

    override suspend fun getToken(): String? {
        // TODO: implement when Firebase credentials are available
        // return FirebaseMessaging.getInstance().token.await()
        return null
    }

    override fun isSupported(): Boolean {
        // TODO: return true when Firebase is configured
        return false
    }
}
