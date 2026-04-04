package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.OAuth2Client
import com.fivucsas.shared.domain.repository.OAuth2ClientRepository
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.DeveloperPortalUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeveloperPortalViewModel(
    private val oAuth2ClientRepository: OAuth2ClientRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(DeveloperPortalUiState())
    val uiState: StateFlow<DeveloperPortalUiState> = _uiState.asStateFlow()

    companion object {
        val AVAILABLE_SCOPES = listOf("openid", "profile", "email", "auth")
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            oAuth2ClientRepository.listClients().fold(
                onSuccess = { apps ->
                    _uiState.update { it.copy(isLoading = false, apps = apps) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load apps")
                        )
                    }
                }
            )
        }
    }

    // --- Register dialog ---

    fun showRegisterDialog() {
        _uiState.update {
            it.copy(
                showRegisterDialog = true,
                registerAppName = "",
                registerRedirectUris = "",
                registerScopes = listOf("openid")
            )
        }
    }

    fun hideRegisterDialog() {
        _uiState.update { it.copy(showRegisterDialog = false) }
    }

    fun updateAppName(name: String) {
        _uiState.update { it.copy(registerAppName = name) }
    }

    fun updateRedirectUris(uris: String) {
        _uiState.update { it.copy(registerRedirectUris = uris) }
    }

    fun toggleScope(scope: String) {
        _uiState.update { state ->
            val current = state.registerScopes
            val updated = if (scope in current) current - scope else current + scope
            state.copy(registerScopes = updated)
        }
    }

    fun registerApp() {
        val state = _uiState.value
        val appName = state.registerAppName.trim()
        val redirectUris = state.registerRedirectUris.trim()
        if (appName.isBlank() || redirectUris.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRegistering = true, errorMessage = null) }

            oAuth2ClientRepository.registerClient(appName, redirectUris, state.registerScopes).fold(
                onSuccess = { newApp ->
                    _uiState.update {
                        it.copy(
                            isRegistering = false,
                            showRegisterDialog = false,
                            apps = listOf(newApp.copy(clientSecret = null)) +
                                it.apps.filter { a -> a.id != newApp.id },
                            createdApp = newApp
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isRegistering = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "register app")
                        )
                    }
                }
            )
        }
    }

    // --- Credentials dialog ---

    fun dismissCredentials() {
        _uiState.update { it.copy(createdApp = null) }
    }

    // --- Delete ---

    fun showDeleteDialog(app: OAuth2Client) {
        _uiState.update { it.copy(showDeleteDialog = true, appToDelete = app) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, appToDelete = null) }
    }

    fun confirmDelete() {
        val app = _uiState.value.appToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            oAuth2ClientRepository.deleteClient(app.id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            showDeleteDialog = false,
                            appToDelete = null,
                            apps = it.apps.filter { a -> a.id != app.id },
                            successMessage = s(StringKey.DEV_PORTAL_DELETE_SUCCESS)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "delete app")
                        )
                    }
                }
            )
        }
    }

    // --- Clipboard feedback ---

    fun setCopiedLabel(label: String?) {
        _uiState.update { it.copy(copiedLabel = label) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
