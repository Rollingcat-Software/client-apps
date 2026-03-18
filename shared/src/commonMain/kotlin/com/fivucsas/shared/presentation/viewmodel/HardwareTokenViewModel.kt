package com.fivucsas.shared.presentation.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * WebAuthn credential info displayed on screen.
 */
data class HardwareTokenCredential(
    val credentialId: String = "",
    val publicKeyAlgorithm: String = "",
    val attestationFormat: String = "",
    val transports: List<String> = emptyList(),
    val registeredAt: String = ""
)

/**
 * UI state for hardware token (WebAuthn cross-platform) screen.
 */
data class HardwareTokenUiState(
    val isRegistering: Boolean = false,
    val isVerifying: Boolean = false,
    val credential: HardwareTokenCredential? = null,
    val isRegistered: Boolean = false,
    val isVerified: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for hardware token (WebAuthn cross-platform) registration and verification.
 *
 * Uses Android's Credential Manager API or FIDO2 API for cross-platform
 * authenticator attachment (e.g., YubiKey, hardware security keys).
 *
 * Registration flow:
 * 1. Request creation options from server
 * 2. Call FIDO2 API with authenticatorAttachment: "cross-platform"
 * 3. Send attestation response to server
 *
 * Verification flow:
 * 1. Request assertion options from server
 * 2. Call FIDO2 API to get assertion
 * 3. Send assertion response to server
 */
class HardwareTokenViewModel {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(HardwareTokenUiState())
    val uiState: StateFlow<HardwareTokenUiState> = _uiState.asStateFlow()

    /**
     * Start hardware token registration.
     * In production, this would:
     * 1. Fetch challenge from server (GET /api/v1/webauthn/register/options)
     * 2. Call Android FIDO2 / Credential Manager API
     * 3. Send attestation to server (POST /api/v1/webauthn/register/verify)
     */
    fun register() {
        _uiState.update {
            it.copy(
                isRegistering = true,
                errorMessage = null,
                successMessage = null
            )
        }

        // Simulate registration flow for now.
        // Real implementation requires Activity context (Android-specific),
        // so the actual FIDO2 call is triggered from the Screen composable.
        _uiState.update {
            it.copy(
                isRegistering = false,
                isRegistered = true,
                credential = HardwareTokenCredential(
                    credentialId = "pending-fido2-integration",
                    publicKeyAlgorithm = "ES256",
                    attestationFormat = "packed",
                    transports = listOf("usb", "nfc", "ble"),
                    registeredAt = "Pending FIDO2 integration"
                ),
                successMessage = "Hardware token registration requires FIDO2 API integration. " +
                        "Connect a hardware security key (e.g., YubiKey) via USB, NFC, or BLE."
            )
        }
    }

    /**
     * Start hardware token verification.
     * In production, this would:
     * 1. Fetch assertion challenge from server
     * 2. Call FIDO2 API for cross-platform assertion
     * 3. Send assertion to server for verification
     */
    fun verify() {
        _uiState.update {
            it.copy(
                isVerifying = true,
                errorMessage = null,
                successMessage = null
            )
        }

        _uiState.update {
            it.copy(
                isVerifying = false,
                isVerified = true,
                successMessage = "Hardware token verification requires FIDO2 API integration. " +
                        "Use your registered security key to authenticate."
            )
        }
    }

    /**
     * Called from the Android screen after a successful FIDO2 registration.
     */
    fun onRegistrationComplete(
        credentialId: String,
        publicKeyAlgorithm: String = "ES256",
        attestationFormat: String = "packed",
        transports: List<String> = listOf("usb", "nfc", "ble")
    ) {
        _uiState.update {
            it.copy(
                isRegistering = false,
                isRegistered = true,
                credential = HardwareTokenCredential(
                    credentialId = credentialId,
                    publicKeyAlgorithm = publicKeyAlgorithm,
                    attestationFormat = attestationFormat,
                    transports = transports,
                    registeredAt = "Just now"
                ),
                successMessage = "Hardware token registered successfully!"
            )
        }
    }

    /**
     * Called from the Android screen after a successful FIDO2 assertion.
     */
    fun onVerificationComplete() {
        _uiState.update {
            it.copy(
                isVerifying = false,
                isVerified = true,
                successMessage = "Hardware token verified successfully!"
            )
        }
    }

    /**
     * Called when FIDO2 operation fails.
     */
    fun onError(message: String) {
        _uiState.update {
            it.copy(
                isRegistering = false,
                isVerifying = false,
                errorMessage = message
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun reset() {
        _uiState.value = HardwareTokenUiState()
    }
}
