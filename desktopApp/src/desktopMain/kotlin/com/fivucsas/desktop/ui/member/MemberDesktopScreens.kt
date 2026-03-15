package com.fivucsas.desktop.ui.member

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.ui.components.DesktopAppShell
import com.fivucsas.desktop.ui.components.DesktopInfoBanner
import com.fivucsas.desktop.ui.components.DesktopBannerType
import com.fivucsas.desktop.ui.components.DesktopSectionHeader
import com.fivucsas.desktop.ui.components.DesktopTable
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.presentation.viewmodel.UserProfileViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.koin.compose.koinInject

// ─── Profile Screen ─────────────────────────────────────────────────────────

@Composable
fun MemberDesktopProfileScreen(
    role: UserRole,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onReEnroll: () -> Unit,
    onDeleteEnrollment: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: UserProfileViewModel = koinInject()
    val profileState by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProfile() }

    val user = profileState.user
    val displayName = user?.name ?: ""
    val displayEmail = user?.email ?: ""
    val displayPhone = user?.phoneNumber ?: ""
    val displayId = user?.idNumber ?: ""
    val displayDate = user?.enrollmentDate ?: ""

    val isSelfBiometricRole = role == UserRole.USER || role == UserRole.TENANT_MEMBER
    var showDeleteConfirm by remember { mutableStateOf(false) }

    DesktopAppShell(
        title = "My Profile",
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = displayEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Role: ${role.name.replace('_', ' ')}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OutlinedButton(onClick = onEditProfile) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Profile")
                }
            }

            // Personal Information Card
            DesktopTable(title = "Personal Information") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileRow("Name", displayName)
                    ProfileRow("Email", displayEmail)
                    ProfileRow("Phone", displayPhone)
                    ProfileRow("ID Number", displayId)
                    ProfileRow("Member Since", displayDate)
                }
            }

            // Account Actions
            DesktopSectionHeader(title = "Account Actions")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = onChangePassword) {
                    Text("Change Password")
                }
                if (isSelfBiometricRole && role.hasPermission(Permission.ENROLL_SELF_UPDATE)) {
                    Button(onClick = onReEnroll) {
                        Text("Re-Enroll Face")
                    }
                }
                Button(onClick = onOpenSettings) {
                    Text("Settings & Help")
                }
            }

            if (isSelfBiometricRole && role.hasPermission(Permission.ENROLL_SELF_DELETE)) {
                if (showDeleteConfirm) {
                    DesktopInfoBanner(
                        type = DesktopBannerType.Warning,
                        text = "Are you sure you want to delete your biometric enrollment? This cannot be undone."
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                showDeleteConfirm = false
                                onDeleteEnrollment()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Confirm Delete")
                        }
                        OutlinedButton(onClick = { showDeleteConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete My Enrollment")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Edit Profile Screen ────────────────────────────────────────────────────

@Composable
fun MemberDesktopEditProfileScreen(
    onSave: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: UserProfileViewModel = koinInject()
    val profileState by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProfile() }

    val user = profileState.user
    val nameParts = (user?.name ?: "").split(" ", limit = 2)
    var firstName by remember(user) { mutableStateOf(nameParts.getOrElse(0) { "" }) }
    var lastName by remember(user) { mutableStateOf(nameParts.getOrElse(1) { "" }) }
    var phone by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }
    val email = user?.email ?: ""
    val idNumber = user?.idNumber ?: ""

    DesktopAppShell(
        title = "Edit Profile",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DesktopSectionHeader(
                title = "Edit Profile",
                subtitle = "Update your personal information"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Email (read-only)") },
                        enabled = false,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = idNumber,
                        onValueChange = {},
                        label = { Text("ID Number (read-only)") },
                        enabled = false,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onSave(firstName, lastName, phone) }) {
                    Text("Save Changes")
                }
                OutlinedButton(onClick = onBack) {
                    Text("Cancel")
                }
            }
        }
    }
}

// ─── Activity History Screen ────────────────────────────────────────────────

@Composable
fun MemberDesktopActivityHistoryScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    data class HistoryEntry(
        val category: String,
        val title: String,
        val description: String,
        val timestamp: String,
        val score: String,
        val isSuccess: Boolean
    )

    val filters = listOf("all" to "All", "verification" to "Verifications", "enrollment" to "Enrollments")
    var selectedFilter by remember { mutableStateOf("all") }

    val sections = listOf(
        "Today" to listOf(
            HistoryEntry("verification", "Verification Successful", "Confidence: 94%", "10:30 AM", "94%", true),
            HistoryEntry("verification", "Verification Successful", "Confidence: 91%", "09:15 AM", "91%", true)
        ),
        "Yesterday" to listOf(
            HistoryEntry("verification", "Verification Failed", "Low confidence score", "3:14 PM", "62%", false),
            HistoryEntry("verification", "Verification Successful", "Confidence: 91%", "3:15 PM", "91%", true)
        ),
        "January 28, 2026" to listOf(
            HistoryEntry("enrollment", "Face Enrollment Completed", "Quality score: 88%", "2:00 PM", "88%", true)
        )
    )

    val filteredSections = sections.mapNotNull { (title, entries) ->
        val filtered = if (selectedFilter == "all") entries
        else entries.filter { it.category == selectedFilter }
        if (filtered.isEmpty()) null else title to filtered
    }

    DesktopAppShell(
        title = "Activity History",
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DesktopSectionHeader(
                title = "Activity History",
                subtitle = "Your recent verification and enrollment activities"
            )

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { (value, label) ->
                    if (selectedFilter == value) {
                        Button(onClick = { selectedFilter = value }) { Text(label) }
                    } else {
                        OutlinedButton(onClick = { selectedFilter = value }) { Text(label) }
                    }
                }
            }

            // History sections
            filteredSections.forEach { (sectionTitle, entries) ->
                DesktopTable(title = sectionTitle, subtitle = "${entries.size} activities") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        entries.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (entry.category == "verification") Icons.Default.Security else Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = if (entry.isSuccess) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = entry.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = entry.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.score,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (entry.isSuccess) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = entry.timestamp,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredSections.isEmpty()) {
                DesktopInfoBanner(
                    type = DesktopBannerType.Info,
                    text = "No activities found for the selected filter."
                )
            }
        }
    }
}

// ─── Settings & Help Screen ─────────────────────────────────────────────────

@Composable
fun MemberDesktopSettingsHelpScreen(
    onBack: () -> Unit
) {
    DesktopAppShell(
        title = "Settings & Help",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DesktopSectionHeader(
                title = "Help & FAQ",
                subtitle = "Frequently asked questions and troubleshooting"
            )

            FaqCard(
                title = "Why do we need camera access?",
                subtitle = "Permissions",
                answer = "We use the camera to securely capture your face for verification. " +
                    "Your biometric data is encrypted and stored securely."
            )
            FaqCard(
                title = "How long does verification take?",
                subtitle = "Timing",
                answer = "Most verifications complete in under one minute. " +
                    "The process includes face detection, liveness check, and matching."
            )
            FaqCard(
                title = "What if verification fails?",
                subtitle = "Recovery",
                answer = "Try again in better lighting or remove any obstructions like glasses. " +
                    "If the problem persists, try re-enrolling your face."
            )
            FaqCard(
                title = "How do I re-enroll my face?",
                subtitle = "Enrollment",
                answer = "Go to your Profile screen and click 'Re-Enroll Face'. " +
                    "Follow the on-screen instructions to capture a new enrollment photo."
            )
            FaqCard(
                title = "How do I delete my biometric data?",
                subtitle = "Privacy",
                answer = "Go to your Profile screen and click 'Delete My Enrollment'. " +
                    "This will permanently remove your biometric data. You will need to re-enroll to use face verification."
            )
        }
    }
}

@Composable
private fun FaqCard(
    title: String,
    subtitle: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── My Invitations Screen ──────────────────────────────────────────────────

private data class ReceivedInvite(
    val id: String,
    val tenantName: String,
    val invitedBy: String,
    val role: String,
    val receivedAt: String,
    val expiresAt: String,
    val status: ReceivedInviteStatus
)

private enum class ReceivedInviteStatus { PENDING, ACCEPTED, DECLINED, EXPIRED }

@Composable
fun MemberDesktopMyInvitationsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val invites = remember {
        mutableListOf(
            ReceivedInvite("1", "Acme Corporation", "admin@acme.com", "TENANT_MEMBER", "2026-02-20", "2026-03-20", ReceivedInviteStatus.PENDING),
            ReceivedInvite("2", "Globex Inc.", "hr@globex.com", "TENANT_MEMBER", "2026-02-18", "2026-03-18", ReceivedInviteStatus.PENDING),
            ReceivedInvite("3", "Wayne Enterprises", "ops@wayne.com", "TENANT_MEMBER", "2026-01-15", "2026-02-15", ReceivedInviteStatus.ACCEPTED),
            ReceivedInvite("4", "Stark Industries", "tony@stark.com", "TENANT_ADMIN", "2026-01-05", "2026-02-05", ReceivedInviteStatus.EXPIRED)
        )
    }
    var successMessage by remember { mutableStateOf<String?>(null) }

    DesktopAppShell(
        title = "My Invitations",
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DesktopSectionHeader(
                title = "My Invitations",
                subtitle = "View and respond to tenant invitations"
            )

            successMessage?.let {
                DesktopInfoBanner(type = DesktopBannerType.Info, text = it)
            }

            val pending = invites.filter { it.status == ReceivedInviteStatus.PENDING }
            val past = invites.filter { it.status != ReceivedInviteStatus.PENDING }

            if (invites.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Invitations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "You have no pending or past invitations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (pending.isNotEmpty()) {
                DesktopTable(title = "Pending", subtitle = "${pending.size} invitations") {
                    pending.forEach { invite ->
                        InviteRow(
                            invite = invite,
                            onAccept = {
                                val idx = invites.indexOfFirst { it.id == invite.id }
                                if (idx >= 0) {
                                    invites[idx] = invite.copy(status = ReceivedInviteStatus.ACCEPTED)
                                    successMessage = "Joined ${invite.tenantName}"
                                }
                            },
                            onDecline = {
                                val idx = invites.indexOfFirst { it.id == invite.id }
                                if (idx >= 0) {
                                    invites[idx] = invite.copy(status = ReceivedInviteStatus.DECLINED)
                                    successMessage = "Invitation declined"
                                }
                            }
                        )
                    }
                }
            }

            if (past.isNotEmpty()) {
                DesktopTable(title = "Past", subtitle = "${past.size} invitations") {
                    past.forEach { invite ->
                        InviteRow(invite = invite, onAccept = null, onDecline = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteRow(
    invite: ReceivedInvite,
    onAccept: (() -> Unit)?,
    onDecline: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(invite.tenantName, fontWeight = FontWeight.SemiBold)
                Text(
                    "From: ${invite.invitedBy}  |  Role: ${invite.role.replace("TENANT_", "")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Expires: ${invite.expiresAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                invite.status.name,
                fontWeight = FontWeight.Bold,
                color = when (invite.status) {
                    ReceivedInviteStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                    ReceivedInviteStatus.ACCEPTED -> MaterialTheme.colorScheme.primary
                    ReceivedInviteStatus.DECLINED -> MaterialTheme.colorScheme.error
                    ReceivedInviteStatus.EXPIRED -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (invite.status == ReceivedInviteStatus.PENDING && onAccept != null && onDecline != null) {
                OutlinedButton(onClick = onDecline) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline")
                }
                Button(onClick = onAccept) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
            }
        }
    }
}

// ─── Request Membership (Join Tenant) Screen ─────────────────────────────────

private data class TenantInfo(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int
)

@Composable
fun MemberDesktopRequestMembershipScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val allTenants = remember {
        listOf(
            TenantInfo("t1", "Acme Corporation", "Global technology solutions provider", 142),
            TenantInfo("t2", "Globex Inc.", "International logistics and supply chain", 87),
            TenantInfo("t3", "Wayne Enterprises", "Diversified industrial conglomerate", 312),
            TenantInfo("t4", "Stark Industries", "Advanced technology and defense", 256),
            TenantInfo("t5", "Umbrella Corp.", "Pharmaceutical and biotech research", 64)
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var requestedTenantIds by remember { mutableStateOf(setOf<String>()) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val filteredTenants = if (searchQuery.isBlank()) allTenants
    else allTenants.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
    }

    DesktopAppShell(
        title = "Join a Tenant",
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DesktopSectionHeader(
                title = "Join a Tenant",
                subtitle = "Search for available tenants and request membership"
            )

            successMessage?.let {
                DesktopInfoBanner(type = DesktopBannerType.Info, text = it)
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search tenants...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "${filteredTenants.size} tenants available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (filteredTenants.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No tenants found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Try a different search term.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                DesktopTable(title = "Available Tenants", subtitle = "${filteredTenants.size} tenants") {
                    filteredTenants.forEach { tenant ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column {
                                    Text(tenant.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        tenant.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "${tenant.memberCount} members",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (tenant.id in requestedTenantIds) {
                                OutlinedButton(onClick = {}, enabled = false) { Text("Requested") }
                            } else {
                                Button(onClick = {
                                    requestedTenantIds = requestedTenantIds + tenant.id
                                    successMessage = "Membership request sent to ${tenant.name}"
                                }) {
                                    Text("Request Membership")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
