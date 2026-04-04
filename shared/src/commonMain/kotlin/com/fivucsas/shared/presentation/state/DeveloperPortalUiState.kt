package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.OAuth2Client

data class DeveloperPortalUiState(
    val apps: List<OAuth2Client> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Register dialog
    val showRegisterDialog: Boolean = false,
    val registerAppName: String = "",
    val registerRedirectUris: String = "",
    val registerScopes: List<String> = listOf("openid"),
    val isRegistering: Boolean = false,

    // Credentials reveal dialog (shown once after creation)
    val createdApp: OAuth2Client? = null,

    // Delete confirmation
    val showDeleteDialog: Boolean = false,
    val appToDelete: OAuth2Client? = null,
    val isDeleting: Boolean = false,

    // Clipboard feedback
    val copiedLabel: String? = null
)
