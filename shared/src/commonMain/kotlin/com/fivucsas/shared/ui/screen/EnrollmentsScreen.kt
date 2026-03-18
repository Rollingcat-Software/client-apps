package com.fivucsas.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.domain.model.Enrollment
import com.fivucsas.shared.domain.model.EnrollmentStatus
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.EnrollmentViewModel
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.organisms.EmptyState

@Composable
fun EnrollmentsScreen(
    viewModel: EnrollmentViewModel,
    userId: String,
    onStartEnrollment: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadEnrollments(userId)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = s(StringKey.ENROLLMENTS_TITLE),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = s(StringKey.ENROLLMENTS_SUBTITLE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onStartEnrollment != null) {
                OutlinedButton(onClick = onStartEnrollment) {
                    Text(s(StringKey.START_ENROLLMENT))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.loadEnrollments(userId) }) {
                        Text(s(StringKey.RETRY))
                    }
                }
            }
        } else if (uiState.enrollments.isEmpty()) {
            EmptyState(
                title = s(StringKey.NO_ENROLLMENTS),
                message = s(StringKey.NO_DATA)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.enrollments) { enrollment ->
                    EnrollmentCard(enrollment = enrollment)
                }
            }
        }
    }
}

@Composable
private fun EnrollmentCard(enrollment: Enrollment) {
    val methodDisplayName = when (enrollment.method.uppercase()) {
        "PASSWORD" -> s(StringKey.PASSWORD_AUTH_METHOD)
        "FACE", "FACE_RECOGNITION" -> s(StringKey.FACE_RECOGNITION)
        "FINGERPRINT" -> s(StringKey.FINGERPRINT)
        "VOICE", "VOICE_RECOGNITION" -> s(StringKey.VOICE_RECOGNITION)
        "NFC", "NFC_DOCUMENT" -> s(StringKey.NFC_DOCUMENT)
        "TOTP" -> s(StringKey.TOTP)
        "EMAIL_OTP" -> s(StringKey.EMAIL_OTP)
        "SMS_OTP" -> s(StringKey.SMS_OTP)
        "HARDWARE_KEY" -> s(StringKey.HARDWARE_KEY)
        else -> enrollment.method
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = methodDisplayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                StatusBadge(
                    text = when (enrollment.status) {
                        EnrollmentStatus.ENROLLED -> s(StringKey.ENROLLED)
                        EnrollmentStatus.ACTIVE -> s(StringKey.ACTIVE)
                        EnrollmentStatus.PENDING -> s(StringKey.LOADING)
                        EnrollmentStatus.REVOKED -> s(StringKey.INACTIVE)
                        EnrollmentStatus.EXPIRED -> s(StringKey.INACTIVE)
                    },
                    isPositive = enrollment.status == EnrollmentStatus.ENROLLED || enrollment.status == EnrollmentStatus.ACTIVE
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (enrollment.enrolledAt.isNotBlank()) {
                Text(
                    text = "${s(StringKey.ENROLLMENT_DATE)}: ${enrollment.enrolledAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
