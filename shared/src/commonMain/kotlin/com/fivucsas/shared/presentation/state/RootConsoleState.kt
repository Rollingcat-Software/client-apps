package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.AuditLogEntry
import com.fivucsas.shared.domain.model.Capabilities
import com.fivucsas.shared.domain.model.GlobalUser
import com.fivucsas.shared.domain.model.RootFilter
import com.fivucsas.shared.domain.model.RootSystemSettings
import com.fivucsas.shared.domain.model.SecurityEvent
import com.fivucsas.shared.domain.model.TenantDetail
import com.fivucsas.shared.domain.model.TenantSummary

data class RootConsoleUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val capabilities: Capabilities? = null,
    val selectedTenantId: String? = null,
    val filter: RootFilter = RootFilter(),
    val tenants: List<TenantSummary> = emptyList(),
    val tenantDetail: TenantDetail? = null,
    val users: List<GlobalUser> = emptyList(),
    val tenantAdmins: List<GlobalUser> = emptyList(),
    val auditLogs: List<AuditLogEntry> = emptyList(),
    val securityEvents: List<SecurityEvent> = emptyList(),
    val settings: RootSystemSettings? = null,
    val impersonatingTenantId: String? = null,
    val showImpersonationConfirm: Boolean = false
)

sealed interface RootConsoleUiEvent {
    data class Load(val tenantId: String? = null) : RootConsoleUiEvent
    data class UpdateQuery(val value: String) : RootConsoleUiEvent
    data class SelectTenant(val tenantId: String?) : RootConsoleUiEvent
    data class ToggleUserEnabled(val userId: String, val enabled: Boolean) : RootConsoleUiEvent
    data class UpdateUserProfile(
        val userId: String,
        val fullName: String,
        val email: String,
        val role: String,
        val tenantId: String?
    ) : RootConsoleUiEvent
    data class DeleteTenant(val tenantId: String) : RootConsoleUiEvent
    data class DeleteUser(val userId: String) : RootConsoleUiEvent
    data object RefreshAudit : RootConsoleUiEvent
    data object RefreshSecurity : RootConsoleUiEvent
    data class ConfirmImpersonation(val tenantId: String) : RootConsoleUiEvent
    data object CancelImpersonation : RootConsoleUiEvent
}

sealed interface RootConsoleUiEffect {
    data class ShowMessage(val message: String) : RootConsoleUiEffect
    data class OpenTenantContext(val tenantId: String) : RootConsoleUiEffect
}
