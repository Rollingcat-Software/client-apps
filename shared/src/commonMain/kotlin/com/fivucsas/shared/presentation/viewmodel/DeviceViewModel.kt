package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.Device
import com.fivucsas.shared.domain.repository.DeviceRepository
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.DeviceUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeviceViewModel(
    private val deviceRepository: DeviceRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    fun loadDevices(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            deviceRepository.getDevices(userId).fold(
                onSuccess = { devices ->
                    _uiState.update {
                        it.copy(isLoading = false, devices = devices)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load devices")
                        )
                    }
                }
            )
        }
    }

    fun loadWebAuthnCredentials(userId: String) {
        viewModelScope.launch {
            deviceRepository.getWebAuthnCredentials(userId).fold(
                onSuccess = { credentials ->
                    _uiState.update {
                        it.copy(webAuthnCredentials = credentials)
                    }
                },
                onFailure = { /* Non-critical, silently ignore */ }
            )
        }
    }

    fun showRemoveDialog(device: Device) {
        _uiState.update {
            it.copy(showRemoveDialog = true, deviceToRemove = device)
        }
    }

    fun hideRemoveDialog() {
        _uiState.update {
            it.copy(showRemoveDialog = false, deviceToRemove = null)
        }
    }

    fun confirmRemove(userId: String) {
        val device = _uiState.value.deviceToRemove ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showRemoveDialog = false) }

            deviceRepository.removeDevice(device.id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            deviceToRemove = null,
                            successMessage = s(StringKey.DEVICE_REMOVED)
                        )
                    }
                    loadDevices(userId)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "remove device")
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
