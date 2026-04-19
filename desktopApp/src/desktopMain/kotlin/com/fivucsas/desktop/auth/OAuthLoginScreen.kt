package com.fivucsas.desktop.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Hosted-first login entry point (default route, 2026-04-18).
 *
 * Shows a single "Sign in with FIVUCSAS" button that fires the RFC 8252
 * loopback flow via [OAuthLoopbackClient]. On success, invokes [onLoggedIn]
 * with the returned [AccessTokens]. Errors are surfaced inline.
 *
 * [onDebugNativeAuth] is wired to the hidden debug flag that still exposes
 * the deprecated native auth screens (see Main.kt). Remove after one
 * release cycle per the hosted-first migration plan.
 */
@Composable
fun OAuthLoginScreen(
    onLoggedIn: (AccessTokens) -> Unit,
    onDebugNativeAuth: (() -> Unit)? = null,
    loopbackClient: OAuthLoopbackClient = remember { OAuthLoopbackClient() },
) {
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "FIVUCSAS Desktop",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign in through your default browser to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (busy) return@Button
                busy = true
                error = null
                scope.launch {
                    try {
                        val tokens = loopbackClient.login()
                        onLoggedIn(tokens)
                    } catch (e: Throwable) {
                        error = e.message ?: "Sign-in failed"
                    } finally {
                        busy = false
                    }
                }
            },
            enabled = !busy,
            modifier = Modifier.widthIn(min = 280.dp).fillMaxWidth(0.5f),
        ) {
            Text(if (busy) "Waiting for browser…" else "Sign in with FIVUCSAS")
        }

        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (onDebugNativeAuth != null) {
            Spacer(modifier = Modifier.height(48.dp))
            TextButton(onClick = onDebugNativeAuth) {
                Text(
                    text = "Debug: use legacy native auth (deprecated)",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
