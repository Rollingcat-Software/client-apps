package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.CapabilityPolicy
import com.fivucsas.shared.domain.model.RootFilter
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.RootAdminRepository
import com.fivucsas.shared.presentation.state.RootConsoleUiEffect
import com.fivucsas.shared.presentation.state.RootConsoleUiEvent
import com.fivucsas.shared.presentation.state.RootConsoleUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RootConsoleViewModel(
    private val role: UserRole,
    private val repository: RootAdminRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(
        RootConsoleUiState(
            capabilities = CapabilityPolicy.fromRole(role)
        )
    )
    val state: StateFlow<RootConsoleUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<RootConsoleUiEffect>()
    val effect: SharedFlow<RootConsoleUiEffect> = _effect.asSharedFlow()

    private var searchJob: Job? = null

    fun onEvent(event: RootConsoleUiEvent) {
        when (event) {
            is RootConsoleUiEvent.Load -> load(event.tenantId)
            is RootConsoleUiEvent.UpdateQuery -> updateQuery(event.value)
            is RootConsoleUiEvent.SelectTenant -> selectTenant(event.tenantId)
            is RootConsoleUiEvent.ToggleUserEnabled -> toggleUser(event.userId, event.enabled)
            is RootConsoleUiEvent.UpdateUserProfile -> updateUserProfile(
                userId = event.userId,
                fullName = event.fullName,
                email = event.email,
                role = event.role,
                tenantId = event.tenantId
            )
            is RootConsoleUiEvent.DeleteTenant -> deleteTenant(event.tenantId)
            is RootConsoleUiEvent.DeleteUser -> deleteUser(event.userId)
            RootConsoleUiEvent.RefreshAudit -> refreshAudit()
            RootConsoleUiEvent.RefreshSecurity -> refreshSecurity()
            is RootConsoleUiEvent.ConfirmImpersonation -> confirmImpersonation(event.tenantId)
            RootConsoleUiEvent.CancelImpersonation -> _state.update { it.copy(showImpersonationConfirm = false) }
        }
    }

    private fun load(tenantId: String?) {
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val filter = _state.value.filter.copy(tenantId = tenantId)
            val tenants = repository.getTenants(filter).getOrElse { return@launch failFrom(it) }
            val users = repository.getUsers(filter).getOrElse { return@launch failFrom(it) }
            val admins = repository.getTenantAdmins(filter).getOrElse { return@launch failFrom(it) }
            val audit = repository.getAuditLogs(filter).getOrElse { return@launch failFrom(it) }
            val security = repository.getSecurityEvents(filter).getOrElse { return@launch failFrom(it) }
            val settings = repository.getSystemSettings().getOrElse { return@launch failFrom(it) }
            val detail = tenantId?.let { repository.getTenantDetail(it).getOrNull() }
            _state.update {
                it.copy(
                    isLoading = false,
                    filter = filter,
                    selectedTenantId = tenantId,
                    tenants = tenants,
                    users = users,
                    tenantAdmins = admins,
                    auditLogs = audit,
                    securityEvents = security,
                    settings = settings,
                    tenantDetail = detail
                )
            }
        }
    }

    private fun updateQuery(value: String) {
        _state.update { it.copy(filter = it.filter.copy(query = value)) }
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            load(_state.value.selectedTenantId)
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    private fun selectTenant(tenantId: String?) {
        load(tenantId)
    }

    private fun toggleUser(userId: String, enabled: Boolean) {
        scope.launch {
            val result = repository.updateUser(userId, enabled)
            if (result.isFailure) return@launch failFrom(result.exceptionOrNull() ?: Exception("422"))
            load(_state.value.selectedTenantId)
        }
    }

    private fun deleteTenant(tenantId: String) {
        scope.launch {
            val result = repository.deleteTenant(tenantId)
            if (result.isFailure) return@launch failFrom(result.exceptionOrNull() ?: Exception("422"))
            _effect.emit(RootConsoleUiEffect.ShowMessage("Tenant deleted"))
            load(null)
        }
    }

    private fun deleteUser(userId: String) {
        scope.launch {
            val result = repository.deleteUser(userId)
            if (result.isFailure) return@launch failFrom(result.exceptionOrNull() ?: Exception("422"))
            _effect.emit(RootConsoleUiEffect.ShowMessage("User removed"))
            load(_state.value.selectedTenantId)
        }
    }

    private fun updateUserProfile(
        userId: String,
        fullName: String,
        email: String,
        role: String,
        tenantId: String?
    ) {
        scope.launch {
            val result = repository.updateUserProfile(
                userId = userId,
                fullName = fullName,
                email = email,
                role = role,
                tenantId = tenantId
            )
            if (result.isFailure) return@launch failFrom(result.exceptionOrNull() ?: Exception("422"))
            _effect.emit(RootConsoleUiEffect.ShowMessage("User updated"))
            load(_state.value.selectedTenantId)
        }
    }

    private fun refreshAudit() {
        scope.launch {
            val audit = repository.getAuditLogs(_state.value.filter)
                .getOrElse { return@launch failFrom(it) }
            _state.update { it.copy(auditLogs = audit) }
        }
    }

    private fun refreshSecurity() {
        scope.launch {
            val events = repository.getSecurityEvents(_state.value.filter)
                .getOrElse { return@launch failFrom(it) }
            _state.update { it.copy(securityEvents = events) }
        }
    }

    private fun confirmImpersonation(tenantId: String) {
        scope.launch {
            _state.update { it.copy(impersonatingTenantId = tenantId, showImpersonationConfirm = true) }
            _effect.emit(RootConsoleUiEffect.OpenTenantContext(tenantId))
        }
    }

    private fun failFrom(error: Throwable) {
        val reason = error.message ?: "Unknown error"
        val mapped = when (reason) {
            "401" -> "Session expired (401)."
            "403" -> "Operation denied by backend (403)."
            "409" -> "Conflict while applying changes (409)."
            "422" -> "Invalid request data (422)."
            "429" -> "Rate limit reached (429)."
            else -> reason
        }
        _state.update { it.copy(isLoading = false, errorMessage = mapped) }
    }

    fun applySystemSettings(settingsText: String, rateLimit: Int, passwordPolicy: String) {
        scope.launch {
            val payload = com.fivucsas.shared.domain.model.RootSystemSettings(
                jwtPolicySummary = settingsText,
                defaultRateLimitPerMinute = rateLimit,
                passwordPolicySummary = passwordPolicy
            )
            val updated = repository.updateSystemSettings(payload).getOrElse { return@launch failFrom(it) }
            _state.update { it.copy(settings = updated) }
            _effect.emit(RootConsoleUiEffect.ShowMessage("Settings updated"))
        }
    }
}

fun rootInitialFilter(query: String = ""): RootFilter = RootFilter(query = query)
