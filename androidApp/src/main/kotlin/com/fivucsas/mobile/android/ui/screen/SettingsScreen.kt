package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import com.fivucsas.mobile.android.data.preferences.ThemePreferences
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.StringResources
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.ui.components.molecules.ExpandableCard
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.ThemeMode
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userRole: UserRole = UserRole.USER,
    onNavigateBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToEmailOtp: () -> Unit = {},
    onNavigateToSmsOtp: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToHardwareToken: () -> Unit = {},
    onNavigateToAuthenticator: () -> Unit = {},
    /**
     * Navigates ROOT users to the real platform-level System Settings screen
     * (shared `SystemSettingsScreen`, backed by RootConsoleViewModel /
     * RootAdminRepository). The previous inline "System Settings" card here was
     * wired to the *tenant*-scoped TenantSettingsViewModel, never called
     * loadSettings(), and exposed a rate-limit field that mapped to no domain
     * field — pressing Save could PUT tenants/settings with default values and
     * overwrite real tenant config. We no longer offer that editor inline.
     */
    onNavigateToSystemSettings: () -> Unit = {},
    onLogout: () -> Unit,
    themePreferences: ThemePreferences = koinInject()
) {
    val notificationsEnabled = remember { mutableStateOf(true) }
    val biometricEnabled = remember { mutableStateOf(true) }
    val analyticsEnabled = remember { mutableStateOf(false) }

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
                .background(AppColors.Background)
                .padding(paddingValues)
                .padding(UIDimens.SpacingMedium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            SettingsSectionLabel(text = s(StringKey.SETTINGS_TITLE))

            // Language Selection
            ExpandableCard(
                title = s(StringKey.LANGUAGE),
                subtitle = "Türkçe / English"
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
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
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

            // Appearance / Theme mode (SYSTEM / LIGHT / DARK)
            val currentThemeMode by themePreferences.themeMode.collectAsState()
            val themeSubtitle = when (currentThemeMode) {
                ThemeMode.SYSTEM -> s(StringKey.THEME_SYSTEM)
                ThemeMode.LIGHT -> s(StringKey.THEME_LIGHT)
                ThemeMode.DARK -> s(StringKey.THEME_DARK)
            }
            ExpandableCard(
                title = s(StringKey.THEME_SECTION_TITLE),
                subtitle = themeSubtitle
            ) {
                val options = listOf(
                    ThemeMode.SYSTEM to s(StringKey.THEME_SYSTEM),
                    ThemeMode.LIGHT to s(StringKey.THEME_LIGHT),
                    ThemeMode.DARK to s(StringKey.THEME_DARK),
                )
                options.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { themePreferences.setThemeMode(mode) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentThemeMode == mode,
                            onClick = { themePreferences.setThemeMode(mode) }
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
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

                Spacer(modifier = Modifier.size(8.dp))
                SettingsNavRow(
                    text = s(StringKey.CHANGE_PASSWORD),
                    onClick = onNavigateToChangePassword
                )
            }

            ExpandableCard(
                title = s(StringKey.NAV_SETTINGS),
                subtitle = s(StringKey.SETTINGS_DATA_PRIVACY_SUB)
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
                Spacer(modifier = Modifier.size(4.dp))
                SettingsNavRow(text = s(StringKey.NAV_HELP), onClick = onNavigateToHelp)
                SettingsNavRow(text = s(StringKey.NAV_ABOUT), onClick = onNavigateToAbout)
            }

            SettingsSectionLabel(text = s(StringKey.BIOMETRIC_AUTH))

            // Authentication Methods — only the native TOTP Authenticator remains.
            // Server-pipeline biometric surfaces (Voice, TOTP enroll, Liveness,
            // Card-Detection) and the Biometric-Backup card were removed: they
            // duplicate the hosted page / web dashboard bio backend.
            ExpandableCard(
                title = s(StringKey.BIOMETRIC_AUTH),
                subtitle = s(StringKey.SETTINGS_AUTH_METHODS_SUB)
            ) {
                SettingsNavRow(text = s(StringKey.AUTH_TITLE), onClick = onNavigateToAuthenticator)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.Error
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(s(StringKey.LOGOUT), fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))
        }
    }
}

/** Small bold muted section label — matches the web settings group headers. */
@Composable
private fun SettingsSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        color = AppColors.OnSurfaceVariant,
        modifier = modifier
    )
}

/**
 * Consistent navigation row inside a settings card: an indigo label and a
 * trailing chevron, full-width tappable. Replaces the bare clickable links so
 * every in-card action reads the same.
 */
@Composable
private fun SettingsNavRow(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = AppColors.Primary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppColors.OnSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
