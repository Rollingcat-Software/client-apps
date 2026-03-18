package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.User

data class UserProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isOfflineData: Boolean = false,
    val lastSyncTimestamp: String = ""
)
