package com.fivucsas.mobile.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fivucsas.mobile.android.ui.navigation.AppNavigation
import com.fivucsas.mobile.android.ui.theme.FIVUCSASTheme
import com.fivucsas.shared.presentation.viewmodel.NfcApprovalViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    // Shared singleton — same instance observed by the approval screen and
    // optionally driven by notification action taps via ApprovalActionReceiver.
    private val nfcApprovalViewModel: NfcApprovalViewModel by inject()

    companion object {
        private const val TAG = "MainActivity"
        private const val DEEP_LINK_SCHEME = "fivucsas"
        private const val DEEP_LINK_HOST = "nfc-session"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cold-start deep link (user tapped the notification body while app was killed).
        handleDeepLink(intent)

        setContent {
            FIVUCSASTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Warm-start deep link (app was already in memory).
        setIntent(intent)
        handleDeepLink(intent)
    }

    /**
     * If the incoming intent is a `fivucsas://nfc-session/{sessionId}` URI,
     * extract the session id and route it to the shared NfcApprovalViewModel
     * so the approval screen can pick it up. See
     * docs/plans/NFC_PUSH_APPROVAL_PROTOCOL.md §4.2.
     */
    private fun handleDeepLink(intent: Intent?) {
        val uri: Uri = intent?.data ?: return
        if (uri.scheme != DEEP_LINK_SCHEME || uri.host != DEEP_LINK_HOST) return

        val sessionId = uri.pathSegments?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: uri.getQueryParameter("sessionId")
            ?: uri.getQueryParameter("session_id")

        if (sessionId.isNullOrBlank()) {
            Log.w(TAG, "nfc-session deep link without sessionId: $uri")
            return
        }

        Log.i(TAG, "NFC approval deep link received for session $sessionId")
        nfcApprovalViewModel.onDeepLinkArrived(sessionId)
    }
}
