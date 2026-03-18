package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.AnalyticsViewModel
import com.fivucsas.shared.ui.theme.AppColors

// Color palette for stat cards
private val GreenGood = Color(0xFF1B5E20)
private val GreenLight = Color(0xFF4CAF50)
private val BlueInfo = Color(0xFF1565C0)
private val OrangeWarning = Color(0xFFE65100)
private val RedDanger = Color(0xFFC62828)
private val PurpleAccent = Color(0xFF6A1B9A)
private val TealAccent = Color(0xFF00796B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s(StringKey.ANALYTICS_TITLE)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s(StringKey.BACK))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadStatistics() }) {
                        Icon(Icons.Default.Refresh, contentDescription = s(StringKey.REFRESH))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.OnSurface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.loadStatistics() }) {
                        Text(s(StringKey.RETRY))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = s(StringKey.ANALYTICS_TITLE),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = s(StringKey.ANALYTICS_SUBTITLE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                val stats = uiState.statistics

                // -- Overview Cards --
                SectionTitle(s(StringKey.ANALYTICS_OVERVIEW))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = s(StringKey.TOTAL_USERS),
                        value = stats?.totalUsers?.toString() ?: "0",
                        icon = Icons.Default.Group,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = s(StringKey.ACTIVE_USERS),
                        value = stats?.activeUsers?.toString() ?: "0",
                        icon = Icons.Default.Person,
                        color = GreenGood,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = s(StringKey.ANALYTICS_VERIFICATIONS_TODAY),
                        value = stats?.verificationsToday?.toString() ?: "0",
                        icon = Icons.Default.Verified,
                        color = BlueInfo,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = s(StringKey.ANALYTICS_FAILED_ATTEMPTS),
                        value = stats?.failedAttempts?.toString() ?: "0",
                        icon = Icons.Default.Error,
                        color = if ((stats?.failedAttempts ?: 0) > 10) RedDanger else OrangeWarning,
                        modifier = Modifier.weight(1f)
                    )
                }

                // -- Success Rate Card --
                val successRateValue = (stats?.successRate ?: 0.0) * 100
                val successColor = when {
                    successRateValue >= 90.0 -> GreenGood
                    successRateValue >= 70.0 -> OrangeWarning
                    else -> RedDanger
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = successColor.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = successColor,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = s(StringKey.ANALYTICS_SUCCESS_RATE),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%.1f", successRateValue)}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = successColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressBar(
                            progress = (successRateValue / 100.0).toFloat().coerceIn(0f, 1f),
                            color = successColor
                        )
                    }
                }

                // Pending verifications
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = s(StringKey.ANALYTICS_PENDING),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stats?.pendingVerifications?.toString() ?: "0",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangeWarning
                        )
                    }
                }

                // -- Enrollments by Method --
                SectionTitle(s(StringKey.ANALYTICS_ENROLLMENTS_BY_METHOD))

                EnrollmentMethodRow(
                    label = s(StringKey.FACE_RECOGNITION),
                    count = stats?.faceEnrollments ?: 0,
                    total = stats?.totalUsers ?: 1,
                    icon = Icons.Default.Face,
                    color = BlueInfo
                )
                EnrollmentMethodRow(
                    label = s(StringKey.VOICE_RECOGNITION),
                    count = stats?.voiceEnrollments ?: 0,
                    total = stats?.totalUsers ?: 1,
                    icon = Icons.Default.Mic,
                    color = PurpleAccent
                )
                EnrollmentMethodRow(
                    label = s(StringKey.FINGERPRINT),
                    count = stats?.fingerprintEnrollments ?: 0,
                    total = stats?.totalUsers ?: 1,
                    icon = Icons.Default.Fingerprint,
                    color = TealAccent
                )
                EnrollmentMethodRow(
                    label = s(StringKey.TOTP),
                    count = stats?.totpEnrollments ?: 0,
                    total = stats?.totalUsers ?: 1,
                    icon = Icons.Default.Shield,
                    color = GreenLight
                )
                EnrollmentMethodRow(
                    label = s(StringKey.NFC_DOCUMENT),
                    count = stats?.nfcEnrollments ?: 0,
                    total = stats?.totalUsers ?: 1,
                    icon = Icons.Default.Nfc,
                    color = OrangeWarning
                )

                // -- Recent Activity --
                SectionTitle(s(StringKey.ANALYTICS_RECENT_ACTIVITY))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActivityCard(
                        title = s(StringKey.ANALYTICS_LOGINS_TODAY),
                        value = stats?.loginsToday?.toString() ?: "0",
                        icon = Icons.Default.Login,
                        color = BlueInfo,
                        modifier = Modifier.weight(1f)
                    )
                    ActivityCard(
                        title = s(StringKey.ANALYTICS_REGISTRATIONS_TODAY),
                        value = stats?.registrationsToday?.toString() ?: "0",
                        icon = Icons.Default.PersonAdd,
                        color = GreenGood,
                        modifier = Modifier.weight(1f)
                    )
                    ActivityCard(
                        title = s(StringKey.ANALYTICS_ENROLLMENTS_TODAY),
                        value = stats?.enrollmentsToday?.toString() ?: "0",
                        icon = Icons.Default.PhonelinkSetup,
                        color = PurpleAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

@Composable
private fun EnrollmentMethodRow(
    label: String,
    count: Int,
    total: Int,
    icon: ImageVector,
    color: Color
) {
    val fraction = if (total > 0) count.toFloat() / total.toFloat() else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$count / $total",
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                ProgressBar(
                    progress = fraction.coerceIn(0f, 1f),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.06f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
