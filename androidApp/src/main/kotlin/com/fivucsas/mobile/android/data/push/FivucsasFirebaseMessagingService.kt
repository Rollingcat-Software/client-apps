package com.fivucsas.mobile.android.data.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
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
        private const val DEEP_LINK_SCHEME = "fivucsas"
        private const val DEEP_LINK_HOST = "nfc-session"
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
        val sessionId = data["session_id"] ?: data["sessionId"]
        if (sessionId.isNullOrBlank()) {
            Log.w(TAG, "auth_approval push missing session_id; rendering without actions")
            showSimpleNotification(title, body)
            return
        }

        val notificationId = System.currentTimeMillis().toInt()

        // Main body tap: deep-link into the approval screen.
        val deepLinkUri = Uri.Builder()
            .scheme(DEEP_LINK_SCHEME)
            .authority(DEEP_LINK_HOST)
            .appendPath(sessionId)
            .build()
        val contentIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setPackage(packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ApprovalActionReceiver.EXTRA_SESSION_ID, sessionId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action buttons: Allow / Deny fire the BroadcastReceiver.
        val allowPendingIntent = buildActionPendingIntent(
            sessionId = sessionId,
            decision = ApprovalActionReceiver.DECISION_ALLOW,
            notificationId = notificationId,
            requestCode = notificationId + 1
        )
        val denyPendingIntent = buildActionPendingIntent(
            sessionId = sessionId,
            decision = ApprovalActionReceiver.DECISION_DENY,
            notificationId = notificationId,
            requestCode = notificationId + 2
        )

        val allowAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_view, // thumb-up-like glyph from system drawables
            data["action_allow_label"] ?: "Allow",
            allowPendingIntent
        ).build()
        val denyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel, // thumb-down-like glyph
            data["action_deny_label"] ?: "Deny",
            denyPendingIntent
        ).build()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(allowAction)
            .addAction(denyAction)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun buildActionPendingIntent(
        sessionId: String,
        decision: String,
        notificationId: Int,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(this, ApprovalActionReceiver::class.java).apply {
            putExtra(ApprovalActionReceiver.EXTRA_SESSION_ID, sessionId)
            putExtra(ApprovalActionReceiver.EXTRA_DECISION, decision)
            putExtra(ApprovalActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
