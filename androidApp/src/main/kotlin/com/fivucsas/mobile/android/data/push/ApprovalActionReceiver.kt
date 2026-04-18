package com.fivucsas.mobile.android.data.push

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.fivucsas.shared.domain.repository.NfcApprovalRepository
import com.fivucsas.shared.presentation.state.ApprovalDecision
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Handles the Allow / Deny action-button taps on an auth-approval FCM
 * notification.
 *
 * Flow:
 *   FCM notification action tap → PendingIntent.getBroadcast →
 *   this receiver → POST /api/v1/auth/approval/{sessionId}/decide?decision=...
 *   → cancel notification, toast the result.
 *
 * Backend: see docs/plans/NFC_PUSH_APPROVAL_PROTOCOL.md.
 */
class ApprovalActionReceiver : BroadcastReceiver(), KoinComponent {

    private val repository: NfcApprovalRepository by inject()

    companion object {
        private const val TAG = "FCM-ApprovalAction"
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_DECISION = "decision"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val DECISION_ALLOW = "allow"
        const val DECISION_DENY = "deny"

        // Receiver-scoped coroutine supervisor so goAsync() pending work survives
        // the onReceive return.
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        val decisionRaw = intent.getStringExtra(EXTRA_DECISION)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        if (sessionId.isNullOrBlank() || decisionRaw.isNullOrBlank()) {
            Log.w(TAG, "Missing session_id / decision on approval intent")
            return
        }

        val decision = when (decisionRaw) {
            DECISION_ALLOW -> ApprovalDecision.ALLOW
            DECISION_DENY -> ApprovalDecision.DENY
            else -> {
                Log.w(TAG, "Unknown decision value: $decisionRaw")
                return
            }
        }

        // goAsync keeps the receiver alive across the suspend network call.
        val pending = goAsync()
        scope.launch {
            try {
                val result = repository.submitDecision(sessionId, decision)
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = {
                            if (notificationId > 0) {
                                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
                                    as? NotificationManager
                                nm?.cancel(notificationId)
                            }
                            val msg = when (decision) {
                                ApprovalDecision.ALLOW -> "Access approved"
                                ApprovalDecision.DENY -> "Access denied"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Approval submission failed", error)
                            Toast.makeText(
                                context,
                                "Failed to submit decision: ${error.message ?: "unknown error"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected failure in ApprovalActionReceiver", e)
            } finally {
                pending.finish()
            }
        }
    }
}
