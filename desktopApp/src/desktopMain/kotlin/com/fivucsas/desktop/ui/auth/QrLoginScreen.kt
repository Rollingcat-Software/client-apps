package com.fivucsas.desktop.ui.auth

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.ui.components.DesktopAppShell
import com.fivucsas.desktop.ui.components.DesktopBannerType
import com.fivucsas.desktop.ui.components.DesktopInfoBanner
import com.fivucsas.desktop.ui.components.DesktopSectionHeader
import com.fivucsas.shared.presentation.state.QrLoginStatus
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

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
    DesktopAppShell(
        title = "QR Login",
        onBack = onBackToLogin
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DesktopSectionHeader(
                title = "QR Verification",
                subtitle = "Scan this code with your mobile app to continue"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QrCodeCanvas(payload = qrPayload)
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
                color = MaterialTheme.colorScheme.onSurface
            )

            if (status == QrLoginStatus.WAITING_FOR_MOBILE_SCAN || status == QrLoginStatus.IDLE) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            sessionCode?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Session: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            qrPayload?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                DesktopInfoBanner(
                    type = DesktopBannerType.Error,
                    text = it,
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
private fun QrCodeCanvas(payload: String?) {
    val matrix = payload
        ?.takeIf { it.isNotBlank() }
        ?.let {
            runCatching {
                QRCodeWriter().encode(it, BarcodeFormat.QR_CODE, 256, 256)
            }.getOrNull()
        }

    if (matrix == null) {
        Text(
            text = "Waiting for QR payload...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF37474F),
            textAlign = TextAlign.Center
        )
        return
    }

    Canvas(
        modifier = Modifier
            .size(256.dp)
            .background(Color.White)
    ) {
        val moduleSize = size.minDimension / matrix.width.toFloat()
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix.get(x, y)) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x * moduleSize, y * moduleSize),
                        size = Size(moduleSize, moduleSize)
                    )
                }
            }
        }
    }
}
