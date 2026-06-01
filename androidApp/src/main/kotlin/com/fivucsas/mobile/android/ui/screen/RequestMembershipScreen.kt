package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.domain.model.TenantInfo
import com.fivucsas.shared.domain.repository.RootAdminRepository
import com.fivucsas.shared.ui.components.atoms.SearchTextField
import com.fivucsas.shared.ui.theme.AppColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestMembershipScreen(
    onNavigateBack: () -> Unit,
    rootAdminRepository: RootAdminRepository = koinInject()
) {
    var allTenants by remember { mutableStateOf(emptyList<TenantInfo>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        rootAdminRepository.getTenants().fold(
            onSuccess = { tenants ->
                allTenants = tenants.map { summary ->
                    TenantInfo(
                        id = summary.id,
                        name = summary.name,
                        description = "Members: ${summary.memberCount}",
                        memberCount = summary.memberCount
                    )
                }
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error.message ?: "Failed to load tenants"
                isLoading = false
            }
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredTenants = if (searchQuery.isBlank()) allTenants
    else allTenants.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Join a Tenant",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Honest notice: there is no self-service "request membership" /
            // join-tenant endpoint on the platform. Membership is granted by a
            // tenant admin sending an invitation (see My Invitations). This screen
            // is therefore a read-only directory of tenants — we never claim to
            // have sent a request we cannot actually send.
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.OnSurfaceVariant.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Self-service join requests aren't available yet. To join a " +
                        "tenant, ask an administrator to send you an invitation — it " +
                        "will appear under My Invitations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            SearchTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search tenants...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Text(
                text = "${filteredTenants.size} tenants available",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.OnSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (filteredTenants.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = AppColors.OnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tenants found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try a different search term.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTenants, key = { it.id }) { tenant ->
                        TenantCard(tenant = tenant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TenantCard(
    tenant: TenantInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tenant.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = tenant.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${tenant.memberCount} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }

                // No join-tenant endpoint exists, so this control is disabled with
                // honest copy rather than faking a "request sent" success.
                OutlinedButton(
                    onClick = {},
                    enabled = false
                ) {
                    Text("Invite only")
                }
            }
        }
    }
}
