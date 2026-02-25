package com.fivucsas.desktop.ui.kiosk.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.ui.components.DesktopDashboardActionCard
import com.fivucsas.desktop.ui.components.DesktopSectionHeader

@Composable
fun WelcomeScreen(
    onEnroll: () -> Unit,
    onVerify: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        DesktopSectionHeader(
            title = "FIVUCSAS Kiosk",
            subtitle = "Secure identity verification terminal"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DesktopDashboardActionCard(
                icon = Icons.Default.PersonAdd,
                title = "New Enrollment",
                subtitle = "Register new user with biometric capture",
                onClick = onEnroll,
                modifier = Modifier.width(320.dp)
            )
            DesktopDashboardActionCard(
                icon = Icons.Default.VerifiedUser,
                title = "Verify Identity",
                subtitle = "Verify an existing user with face recognition",
                onClick = onVerify,
                modifier = Modifier.width(320.dp)
            )
        }
    }
}
