package com.fivucsas.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.platform.AuthWidgetCallback
import com.fivucsas.shared.platform.AuthWidgetConfig
import com.fivucsas.shared.platform.AuthWidgetResult
import com.fivucsas.shared.platform.isAuthWidgetAvailable
import com.fivucsas.shared.platform.launchAuthWidget

/**
 * Compose screen that launches the FIVUCSAS auth widget via WebView.
 *
 * On platforms where the widget is available (Android, Desktop), it launches
 * immediately on first composition. The screen shows a loading indicator
 * while the widget is open, and displays the result or error afterwards.
 *
 * On unsupported platforms (iOS), it shows a message directing the user
 * to the native auth flow.
 *
 * @param clientId   Tenant client ID
 * @param flow       Auth flow type ("login", "enroll", "verify")
 * @param userId     Optional user ID for targeted auth
 * @param onComplete Called with the auth result on success
 * @param onCancel   Called when user cancels
 * @param onError    Called with error message on failure
 */
@Composable
fun AuthWidgetScreen(
    clientId: String,
    flow: String = "login",
    userId: String? = null,
    onComplete: (AuthWidgetResult) -> Unit = {},
    onCancel: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    var state by remember { mutableStateOf<WidgetState>(WidgetState.Loading) }

    if (!isAuthWidgetAvailable()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Auth widget is not available on this platform.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCancel) {
                Text("Go Back")
            }
        }
        return
    }

    LaunchedEffect(clientId, flow, userId) {
        val config = AuthWidgetConfig(
            clientId = clientId,
            flow = flow,
            userId = userId
        )
        launchAuthWidget(config, object : AuthWidgetCallback {
            override fun onComplete(result: AuthWidgetResult) {
                state = WidgetState.Done(result)
                onComplete(result)
            }

            override fun onError(message: String) {
                state = WidgetState.Error(message)
                onError(message)
            }

            override fun onCancel() {
                state = WidgetState.Cancelled
                onCancel()
            }
        })
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val s = state) {
            is WidgetState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Opening authentication...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is WidgetState.Done -> {
                Text(
                    text = if (s.result.success) "Authentication successful" else "Authentication failed",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            is WidgetState.Error -> {
                Text(
                    text = "Error: ${s.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCancel) {
                    Text("Go Back")
                }
            }
            is WidgetState.Cancelled -> {
                Text(
                    text = "Authentication cancelled",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private sealed class WidgetState {
    object Loading : WidgetState()
    data class Done(val result: AuthWidgetResult) : WidgetState()
    data class Error(val message: String) : WidgetState()
    object Cancelled : WidgetState()
}
