package com.fivucsas.mobile.android.data.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fivucsas.mobile.android.MainActivity
import com.fivucsas.shared.platform.IPushNotificationService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

class FivucsasFirebaseMessagingService : FirebaseMessagingService() {

    private val pushService: IPushNotificationService by inject()

    companion object {
        private const val TAG = "FCM-Service"
        private const val CHANNEL_ID = "fivucsas_auth_requests"
        private const val CHANNEL_NAME = "Auth Requests"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM token received")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Push received: ${message.data}")

        val type = message.data["type"] ?: "notification"
        val title = message.data["title"] ?: message.notification?.title ?: "FIVUCSAS"
        val body = message.data["body"] ?: message.notification?.body ?: ""

        when (type) {
            "auth_approval" -> showAuthApprovalNotification(title, body, message.data)
            else -> showSimpleNotification(title, body)
        }
    }

    private fun showAuthApprovalNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "auth_approval")
            data.forEach { (k, v) -> putExtra(k, v) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showSimpleNotification(title: String, body: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Authentication approval requests"
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
