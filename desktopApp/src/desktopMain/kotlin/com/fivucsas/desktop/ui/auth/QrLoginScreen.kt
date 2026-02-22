package com.fivucsas.desktop.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.presentation.viewmodel.auth.QrLoginStatus

@Composable
fun QrLoginScreen(
    sessionCode: String?,
    qrPayload: String?,
    status: QrLoginStatus,
    isLoading: Boolean,
    errorMessage: String?,
    onContinue: () -> Unit,
    onBackToLogin: () -> Unit,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1B2B),
                        Color(0xFF101E32)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "QR Verification",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Scan this code with your mobile app to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QrPlaceholder()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (status) {
                    QrLoginStatus.WAITING_FOR_MOBILE_SCAN -> "Waiting for mobile scan..."
                    QrLoginStatus.WAITING_FOR_DESKTOP_APPROVAL -> "Scan completed. Waiting for approval..."
                    QrLoginStatus.APPROVED -> "Approved. Ready to continue."
                    QrLoginStatus.ERROR -> "QR session error. Generate a new code."
                    QrLoginStatus.IDLE -> "Preparing QR session..."
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            if (status == QrLoginStatus.WAITING_FOR_MOBILE_SCAN || status == QrLoginStatus.IDLE) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(color = Color.White)
            }

            sessionCode?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Session: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            qrPayload?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFCDD2),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onBackToLogin) {
                    Text("Back to Login")
                }
                OutlinedButton(onClick = onRefresh, enabled = !isLoading) {
                    Text("Regenerate QR")
                }
                Button(
                    onClick = onContinue,
                    enabled = status == QrLoginStatus.APPROVED && !isLoading
                ) {
                    Text("Continue to Dashboard")
                }
            }
        }
    }
}

@Composable
private fun QrPlaceholder() {
    val dark = Color(0xFF1B1F24)
    val light = Color(0xFFF5F5F5)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(5) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { col ->
                    val isDark = (row + col) % 2 == 0
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isDark) dark else light)
                    )
                }
            }
        }
    }
}
