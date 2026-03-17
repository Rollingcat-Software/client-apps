package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.Device
import com.fivucsas.shared.domain.model.WebAuthnCredential

data class DeviceUiState(
    val devices: List<Device> = emptyList(),
    val webAuthnCredentials: List<WebAuthnCredential> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showRemoveDialog: Boolean = false,
    val deviceToRemove: Device? = null
)
