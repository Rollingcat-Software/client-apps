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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.ui.components.atoms.SectionHeader
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    userRole: UserRole = UserRole.USER,
    currentRoute: String,
    onNavigateBottom: (String) -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onReEnroll: () -> Unit,
    onOpenSettings: () -> Unit,
    navItems: List<com.fivucsas.shared.ui.components.organisms.BottomNavItem> = com.fivucsas.mobile.android.ui.navigation.BottomNavDestinations.items
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {
                    if (userRole.hasPermission(Permission.PROFILE_UPDATE_SELF)) {
                        IconButton(onClick = onEditProfile) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit profile")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.OnSurface
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { onNavigateBottom(it.route) }
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(72.dp)
                )
                Column {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.OnSurfaceVariant
                    )
                    Text(
                        text = "Member since Jan 2026",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
            }

            SectionHeader(title = "Personal Information")
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
            ) {
                Column(modifier = Modifier.padding(UIDimens.SpacingMedium)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Name", style = MaterialTheme.typography.bodySmall, color = AppColors.OnSurfaceVariant)
                        Text(userName, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Email", style = MaterialTheme.typography.bodySmall, color = AppColors.OnSurfaceVariant)
                        Text(userEmail, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Phone", style = MaterialTheme.typography.bodySmall, color = AppColors.OnSurfaceVariant)
                        Text("+1 234 567 8900", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (userRole.hasPermission(Permission.ENROLL_SELF_CREATE)) {
                SectionHeader(title = "Biometric Status")
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(UIDimens.SpacingMedium)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Face Enrolled", style = MaterialTheme.typography.bodyMedium)
                            StatusBadge(text = "Active", type = StatusBadgeType.Success)
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Quality Score: 88%",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.OnSurfaceVariant
                        )
                        Text(
                            text = "Enrolled: Jan 28, 2026",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.OnSurfaceVariant
                        )
                        Text(
                            text = "Expires: Jul 28, 2026",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.OnSurfaceVariant
                        )
                    }
                }
            }

            SectionHeader(title = "Account Actions")
            Column(verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)) {
                Button(onClick = onChangePassword, modifier = Modifier.fillMaxWidth()) {
                    Text("Change Password")
                }
                if (userRole.hasPermission(Permission.ENROLL_SELF_UPDATE)) {
                    Button(onClick = onReEnroll, modifier = Modifier.fillMaxWidth()) {
                        Text("Re-Enroll Face")
                    }
                }
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Settings")
                }
            }
        }
    }
}
