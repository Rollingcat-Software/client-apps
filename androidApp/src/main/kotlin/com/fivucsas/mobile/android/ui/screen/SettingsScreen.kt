package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.StringResources
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.ui.components.molecules.ExpandableCard
import com.fivucsas.shared.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userRole: UserRole = UserRole.USER,
    onNavigateBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToVoiceAuth: () -> Unit = {},
    onNavigateToEmailOtp: () -> Unit = {},
    onNavigateToSmsOtp: () -> Unit = {},
    onNavigateToTotpEnroll: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToLiveness: () -> Unit = {},
    onNavigateToCardDetection: () -> Unit = {},
    onNavigateToHardwareToken: () -> Unit = {},
    onLogout: () -> Unit
) {
    val notificationsEnabled = remember { mutableStateOf(true) }
    val biometricEnabled = remember { mutableStateOf(true) }
    val analyticsEnabled = remember { mutableStateOf(false) }
    val rateLimitInput = remember { mutableStateOf("120") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s(StringKey.SETTINGS_TITLE)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s(StringKey.BACK))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.OnSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UIDimens.SpacingMedium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            // Language Selection
            ExpandableCard(
                title = s(StringKey.LANGUAGE),
                subtitle = "Turkish / English"
            ) {
                var languageExpanded by remember { mutableStateOf(false) }
                var currentLang by remember { mutableStateOf(StringResources.currentLanguage) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = languageExpanded,
                        onExpandedChange = { languageExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = currentLang.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(s(StringKey.LANGUAGE)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false }
                        ) {
                            StringResources.Language.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.displayName) },
                                    onClick = {
                                        StringResources.setLanguage(lang)
                                        currentLang = lang
                                        languageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            ExpandableCard(
                title = s(StringKey.NAV_NOTIFICATIONS),
                subtitle = s(StringKey.NOTIFICATIONS_ENABLED)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = s(StringKey.NOTIFICATIONS_ENABLED),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = notificationsEnabled.value,
                        onCheckedChange = { notificationsEnabled.value = it }
                    )
                }
            }

            ExpandableCard(
                title = s(StringKey.SECURITY_TITLE),
                subtitle = s(StringKey.BIOMETRIC_AUTH)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = s(StringKey.BIOMETRIC_AUTH),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = biometricEnabled.value,
                        onCheckedChange = { biometricEnabled.value = it }
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = s(StringKey.CHANGE_PASSWORD),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToChangePassword() }
                )
            }

            ExpandableCard(
                title = s(StringKey.NAV_SETTINGS),
                subtitle = "Data and privacy"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = s(StringKey.NAV_ANALYTICS),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = analyticsEnabled.value,
                        onCheckedChange = { analyticsEnabled.value = it }
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = s(StringKey.NAV_HELP),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToHelp() }
                )
                Text(
                    text = s(StringKey.NAV_ABOUT),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToAbout() }
                )
            }

            // Authentication Methods
            ExpandableCard(
                title = s(StringKey.BIOMETRIC_AUTH),
                subtitle = "Voice, OTP, TOTP, Liveness, Card, Token"
            ) {
                Text(
                    text = s(StringKey.VOICE_RECOGNITION),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onNavigateToVoiceAuth() }
                )
                Text(
                    text = s(StringKey.EMAIL_OTP),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onNavigateToEmailOtp() }
                )
                Text(
                    text = s(StringKey.SMS_OTP),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onNavigateToSmsOtp() }
                )
                Text(
                    text = s(StringKey.TOTP),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onNavigateToTotpEnroll() }
                )
                Text(
                    text = s(StringKey.LIVENESS_TITLE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onNavigateToLiveness() }
                )
                Text(
                    text = s(StringKey.CARD_DETECTION_TITLE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onNavigateToCardDetection() }
                )
                Text(
                    text = s(StringKey.HARDWARE_TOKEN_TITLE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onNavigateToHardwareToken() }
                )
            }

            // Analytics
            ExpandableCard(
                title = s(StringKey.ANALYTICS_TITLE),
                subtitle = s(StringKey.ANALYTICS_SUBTITLE)
            ) {
                Text(
                    text = s(StringKey.ANALYTICS_TITLE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onNavigateToAnalytics() }
                )
            }

            if (userRole.hasPermission(Permission.PLATFORM_SETTINGS_UPDATE)) {
                ExpandableCard(
                    title = "System Settings",
                    subtitle = "Platform-level defaults"
                ) {
                    Text(
                        text = "Session Policy",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Token and access-session rules are managed centrally.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Password Policy",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Global password requirements are applied for all tenants.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rateLimitInput.value,
                        onValueChange = { rateLimitInput.value = it },
                        label = { Text("Default rate limit per minute") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { /* System settings save not yet implemented */ }) {
                        Text(s(StringKey.SAVE_SETTINGS))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(s(StringKey.LOGOUT))
            }
        }
    }
}
