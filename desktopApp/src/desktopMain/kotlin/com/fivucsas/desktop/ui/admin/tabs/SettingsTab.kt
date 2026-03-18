package com.fivucsas.desktop.ui.admin.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.ui.admin.components.AdminConstants
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.StringResources
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.AppTheme
import com.fivucsas.shared.presentation.state.ConnectionStatus
import com.fivucsas.shared.presentation.state.HealthStatus
import com.fivucsas.shared.presentation.state.SettingsState
import com.fivucsas.shared.presentation.state.TimeFormat
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel

/**
 * Settings Tab Component
 *
 * Comprehensive settings interface with sections for:
 * - System configuration
 * - Security settings
 * - Biometric configuration
 * - Notifications
 * - Appearance
 * - Maintenance tools
 *
 * @param viewModel Admin view model
 */
@Composable
fun SettingsTab(
    viewModel: AdminViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val isLoading = uiState.isLoading

    Column(
        modifier = Modifier.fillMaxSize().padding(UIDimens.SpacingLarge)
    ) {
        // Header with action buttons
        SettingsHeader(
            hasUnsavedChanges = uiState.hasUnsavedSettings,
            onSave = { viewModel.saveSettings() },
            onReset = { viewModel.resetSettings() },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

        // Scrollable settings content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingLarge)
        ) {
            item {
                SystemSettingsSection(
                    settings = settings,
                    onSettingsChange = viewModel::updateSettings
                )
            }

            item {
                SecuritySettingsSection(
                    settings = settings,
                    onSettingsChange = viewModel::updateSettings
                )
            }

            item {
                BiometricSettingsSection(
                    settings = settings,
                    onSettingsChange = viewModel::updateSettings
                )
            }

            item {
                NotificationSettingsSection(
                    settings = settings,
                    onSettingsChange = viewModel::updateSettings
                )
            }

            item {
                AppearanceSettingsSection(
                    settings = settings,
                    onSettingsChange = viewModel::updateSettings
                )
            }

            item {
                MaintenanceSection(
                    settings = settings,
                    onClearCache = { viewModel.clearCache() },
                    onTestConnection = { viewModel.testDatabaseConnection() },
                    onExportLogs = { viewModel.exportLogs() },
                    onCheckHealth = { viewModel.checkSystemHealth() },
                    isLoading = isLoading
                )
            }

            item {
                Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))
            }
        }
    }
}

/**
 * Settings Header with Save/Reset Actions
 */
@Composable
private fun SettingsHeader(
    hasUnsavedChanges: Boolean,
    onSave: () -> Unit,
    onReset: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                s(StringKey.SETTINGS_TITLE),
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                s(StringKey.SETTINGS_SUBTITLE),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)) {
            OutlinedButton(
                onClick = onReset,
                enabled = !isLoading && hasUnsavedChanges
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(UIDimens.IconSmall)
                )
                Spacer(modifier = Modifier.width(UIDimens.SpacingSmall))
                Text(s(StringKey.RESET_SETTINGS))
            }

            Button(
                onClick = onSave,
                enabled = !isLoading && hasUnsavedChanges
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(UIDimens.IconSmall),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(UIDimens.IconSmall)
                    )
                }
                Spacer(modifier = Modifier.width(UIDimens.SpacingSmall))
                Text(s(StringKey.SAVE_SETTINGS))
            }
        }
    }
}

/**
 * System Settings Section
 */
@Composable
private fun SystemSettingsSection(
    settings: SettingsState,
    onSettingsChange: (SettingsState) -> Unit
) {
    SettingsCard(
        title = "System Configuration",
        icon = Icons.Default.Settings,
        description = "Core system endpoints and connection settings"
    ) {
        // API Endpoint (Read-only)
        SettingsTextField(
            label = "API Endpoint",
            value = settings.apiEndpoint,
            onValueChange = { },
            readOnly = true,
            leadingIcon = Icons.Default.Api
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Biometric Processor URL
        SettingsTextField(
            label = "Biometric Processor URL",
            value = settings.biometricProcessorUrl,
            onValueChange = { onSettingsChange(settings.copy(biometricProcessorUrl = it)) },
            leadingIcon = Icons.Default.Fingerprint
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Connection Status
        ConnectionStatusIndicator(status = settings.connectionStatus)
    }
}

/**
 * Security Settings Section
 */
@Composable
private fun SecuritySettingsSection(
    settings: SettingsState,
    onSettingsChange: (SettingsState) -> Unit
) {
    SettingsCard(
        title = "Security Settings",
        icon = Icons.Default.Lock,
        description = "Password policies and session management"
    ) {
        // Session Timeout
        SettingsNumberField(
            label = "Session Timeout (minutes)",
            value = settings.sessionTimeoutMinutes,
            onValueChange = { onSettingsChange(settings.copy(sessionTimeoutMinutes = it)) },
            range = 5..120
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Failed Login Threshold
        SettingsNumberField(
            label = "Failed Login Attempts Threshold",
            value = settings.failedLoginThreshold,
            onValueChange = { onSettingsChange(settings.copy(failedLoginThreshold = it)) },
            range = 3..10
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Account Lockout Duration
        SettingsNumberField(
            label = "Account Lockout Duration (minutes)",
            value = settings.accountLockoutMinutes,
            onValueChange = { onSettingsChange(settings.copy(accountLockoutMinutes = it)) },
            range = 5..60
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        Divider()

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Password Policy
        Text(
            "Password Policy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        SettingsNumberField(
            label = "Minimum Password Length",
            value = settings.passwordMinLength,
            onValueChange = { onSettingsChange(settings.copy(passwordMinLength = it)) },
            range = 6..16
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))

        SettingsSwitch(
            label = "Require Special Characters",
            checked = settings.requireSpecialChars,
            onCheckedChange = { onSettingsChange(settings.copy(requireSpecialChars = it)) }
        )

        SettingsSwitch(
            label = "Require Numbers",
            checked = settings.requireNumbers,
            onCheckedChange = { onSettingsChange(settings.copy(requireNumbers = it)) }
        )

        SettingsSwitch(
            label = "Require Uppercase Letters",
            checked = settings.requireUppercase,
            onCheckedChange = { onSettingsChange(settings.copy(requireUppercase = it)) }
        )
    }
}

/**
 * Biometric Settings Section
 */
@Composable
private fun BiometricSettingsSection(
    settings: SettingsState,
    onSettingsChange: (SettingsState) -> Unit
) {
    SettingsCard(
        title = "Biometric Configuration",
        icon = Icons.Default.Fingerprint,
        description = "Thresholds and quality requirements for biometric verification"
    ) {
        // Liveness Detection Threshold
        SettingsSlider(
            label = "Liveness Detection Threshold",
            value = settings.livenessThreshold,
            onValueChange = { onSettingsChange(settings.copy(livenessThreshold = it)) },
            valueRange = 0f..1f,
            displayValue = "${(settings.livenessThreshold * 100).toInt()}%"
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Face Matching Threshold
        SettingsSlider(
            label = "Face Matching Threshold",
            value = settings.faceMatchingThreshold,
            onValueChange = { onSettingsChange(settings.copy(faceMatchingThreshold = it)) },
            valueRange = 0f..1f,
            displayValue = "${(settings.faceMatchingThreshold * 100).toInt()}%"
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Enrollment Quality
        SettingsSlider(
            label = "Minimum Enrollment Quality",
            value = settings.minEnrollmentQuality.toFloat(),
            onValueChange = { onSettingsChange(settings.copy(minEnrollmentQuality = it.toInt())) },
            valueRange = 50f..100f,
            displayValue = "${settings.minEnrollmentQuality}%"
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        Divider()

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Supported Biometric Types
        Text(
            "Supported Biometric Types",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))

        SettingsSwitch(
            label = "Face Recognition",
            checked = settings.enableFaceRecognition,
            onCheckedChange = { onSettingsChange(settings.copy(enableFaceRecognition = it)) }
        )

        SettingsSwitch(
            label = "Fingerprint Recognition",
            checked = settings.enableFingerprintRecognition,
            onCheckedChange = { onSettingsChange(settings.copy(enableFingerprintRecognition = it)) }
        )

        SettingsSwitch(
            label = "Iris Recognition",
            checked = settings.enableIrisRecognition,
            onCheckedChange = { onSettingsChange(settings.copy(enableIrisRecognition = it)) }
        )
    }
}

/**
 * Notification Settings Section
 */
@Composable
private fun NotificationSettingsSection(
    settings: SettingsState,
    onSettingsChange: (SettingsState) -> Unit
) {
    SettingsCard(
        title = "Notification Settings",
        icon = Icons.Default.Notifications,
        description = "Alert preferences and notification recipients"
    ) {
        SettingsSwitch(
            label = "Enable Email Notifications",
            checked = settings.emailNotificationsEnabled,
            onCheckedChange = { onSettingsChange(settings.copy(emailNotificationsEnabled = it)) }
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Security Alert Threshold
        SettingsNumberField(
            label = "Security Alert Threshold",
            value = settings.securityAlertThreshold,
            onValueChange = { onSettingsChange(settings.copy(securityAlertThreshold = it)) },
            range = 1..10,
            helperText = "Number of failed attempts before sending alert"
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Notification Recipients
        Text(
            "Notification Recipients",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))

        settings.notificationRecipients.forEach { email ->
            Text(
                email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Appearance Settings Section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSettingsSection(
    settings: SettingsState,
    onSettingsChange: (SettingsState) -> Unit
) {
    SettingsCard(
        title = s(StringKey.SETTINGS_SUBTITLE),
        icon = Icons.Default.ColorLens,
        description = "Theme, language, and display preferences"
    ) {
        // Language Selection
        var languageExpanded by remember { mutableStateOf(false) }
        var currentLang by remember { mutableStateOf(StringResources.currentLanguage) }

        ExposedDropdownMenuBox(
            expanded = languageExpanded,
            onExpandedChange = { languageExpanded = it }
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
                            onSettingsChange(settings.copy(language = lang.code))
                            languageExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Theme Selection
        var themeExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = themeExpanded,
            onExpandedChange = { themeExpanded = it }
        ) {
            OutlinedTextField(
                value = settings.theme.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Theme") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = themeExpanded,
                onDismissRequest = { themeExpanded = false }
            ) {
                AppTheme.values().forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme.name) },
                        onClick = {
                            onSettingsChange(settings.copy(theme = theme))
                            themeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Time Format Selection
        var timeFormatExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = timeFormatExpanded,
            onExpandedChange = { timeFormatExpanded = it }
        ) {
            OutlinedTextField(
                value = if (settings.timeFormat == TimeFormat.HOUR_12) "12-Hour" else "24-Hour",
                onValueChange = {},
                readOnly = true,
                label = { Text("Time Format") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeFormatExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = timeFormatExpanded,
                onDismissRequest = { timeFormatExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("12-Hour") },
                    onClick = {
                        onSettingsChange(settings.copy(timeFormat = TimeFormat.HOUR_12))
                        timeFormatExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("24-Hour") },
                    onClick = {
                        onSettingsChange(settings.copy(timeFormat = TimeFormat.HOUR_24))
                        timeFormatExpanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Date Format
        SettingsTextField(
            label = "Date Format",
            value = settings.dateFormat,
            onValueChange = { onSettingsChange(settings.copy(dateFormat = it)) },
            helperText = "e.g., yyyy-MM-dd, dd/MM/yyyy"
        )
    }
}

/**
 * Maintenance Section
 */
@Composable
private fun MaintenanceSection(
    settings: SettingsState,
    onClearCache: () -> Unit,
    onTestConnection: () -> Unit,
    onExportLogs: () -> Unit,
    onCheckHealth: () -> Unit,
    isLoading: Boolean
) {
    SettingsCard(
        title = "Maintenance & Diagnostics",
        icon = Icons.Default.Build,
        description = "System maintenance tools and health monitoring"
    ) {
        // System Health Status
        HealthStatusIndicator(status = settings.systemHealthStatus)

        Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

        // Maintenance Actions Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            item {
                MaintenanceButton(
                    label = "Clear Cache",
                    icon = Icons.Default.Delete,
                    onClick = onClearCache,
                    enabled = !isLoading
                )
            }

            item {
                MaintenanceButton(
                    label = "Test Connection",
                    icon = Icons.Default.Api,
                    onClick = onTestConnection,
                    enabled = !isLoading
                )
            }

            item {
                MaintenanceButton(
                    label = "Export Logs",
                    icon = Icons.Default.CloudUpload,
                    onClick = onExportLogs,
                    enabled = !isLoading
                )
            }

            item {
                MaintenanceButton(
                    label = "Health Check",
                    icon = Icons.Default.HealthAndSafety,
                    onClick = onCheckHealth,
                    enabled = !isLoading
                )
            }
        }

        Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

        // Last Maintenance Info
        if (settings.lastCacheClear != null) {
            Text(
                "Last cache clear: ${settings.lastCacheClear}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================
// HELPER COMPONENTS
// ============================================

/**
 * Settings Card Wrapper
 */
@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    description: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(UIDimens.SpacingLarge)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(UIDimens.IconMedium)
                )

                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

            content()
        }
    }
}

/**
 * Settings Text Field
 */
@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    leadingIcon: ImageVector? = null,
    helperText: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            readOnly = readOnly,
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null) }
            },
            modifier = modifier.fillMaxWidth(),
            singleLine = true
        )

        if (helperText != null) {
            Text(
                helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = UIDimens.SpacingMedium, top = 4.dp)
            )
        }
    }
}

/**
 * Settings Number Field
 */
@Composable
private fun SettingsNumberField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    helperText: String? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = value.toString(),
                onValueChange = { newValue ->
                    newValue.toIntOrNull()?.let {
                        if (it in range) onValueChange(it)
                    }
                },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }

        if (helperText != null) {
            Text(
                helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Settings Slider
 */
@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                displayValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Settings Switch
 */
@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * Connection Status Indicator
 */
@Composable
private fun ConnectionStatusIndicator(status: ConnectionStatus) {
    val (icon, color, text) = when (status) {
        ConnectionStatus.CONNECTED -> Triple(Icons.Default.CheckCircle, Color(0xFF4CAF50), "Connected")
        ConnectionStatus.DISCONNECTED -> Triple(Icons.Default.WifiOff, Color(0xFF9E9E9E), "Disconnected")
        ConnectionStatus.ERROR -> Triple(Icons.Default.Error, Color(0xFFF44336), "Connection Error")
        ConnectionStatus.CHECKING -> Triple(Icons.Default.Refresh, Color(0xFF2196F3), "Checking...")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(UIDimens.IconSmall)
        )
        Text(
            "Connection Status: $text",
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

/**
 * Health Status Indicator
 */
@Composable
private fun HealthStatusIndicator(status: HealthStatus) {
    val (icon, color, text) = when (status) {
        HealthStatus.GOOD -> Triple(Icons.Default.CheckCircle, Color(0xFF4CAF50), "System Health: Good")
        HealthStatus.WARNING -> Triple(Icons.Default.Error, Color(0xFFFF9800), "System Health: Warning")
        HealthStatus.CRITICAL -> Triple(Icons.Default.Error, Color(0xFFF44336), "System Health: Critical")
        HealthStatus.UNKNOWN -> Triple(Icons.Default.Error, Color(0xFF9E9E9E), "System Health: Unknown")
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(UIDimens.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(UIDimens.IconMedium)
            )
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

/**
 * Maintenance Button
 */
@Composable
private fun MaintenanceButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(72.dp),
        enabled = enabled
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(UIDimens.IconMedium)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
