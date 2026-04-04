package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.domain.repository.WebAuthnRepository
import com.fivucsas.shared.domain.repository.WebAuthnStep
import com.fivucsas.shared.platform.FingerprintAuthException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val successMessage: String? = null,
    val stepDescription: String? = null
)

/**
 * ViewModel for hardware token (WebAuthn cross-platform) registration and verification.
 *
 * Uses WebAuthnRepository to coordinate the full FIDO2 flow:
 * 1. Fetch challenge from server
 * 2. Invoke Android Credential Manager (platform or cross-platform authenticator)
 * 3. Send attestation/assertion back to server for verification
 *
 * The authenticatorAttachment can be set to "platform" (fingerprint/face) or
 * "cross-platform" (USB/NFC security keys like YubiKey).
 */
class HardwareTokenViewModel(
    private val webAuthnRepository: WebAuthnRepository,
    private val tokenManager: TokenManager
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(HardwareTokenUiState())
    val uiState: StateFlow<HardwareTokenUiState> = _uiState.asStateFlow()

    /**
     * Register a cross-platform hardware security key (USB/NFC/BLE).
     */
    fun register() {
        registerWithAttachment("cross-platform", "Security Key")
    }

    /**
     * Register a platform authenticator (fingerprint/face unlock).
     */
    fun registerPlatform() {
        registerWithAttachment("platform", "Android Biometric")
    }

    private fun registerWithAttachment(attachment: String, deviceName: String) {
        val userId = tokenManager.getUserId()
        if (userId.isNullOrBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Not logged in. Please sign in first.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isRegistering = true,
                errorMessage = null,
                successMessage = null,
                stepDescription = "Preparing..."
            )
        }

        viewModelScope.launch {
            val result = webAuthnRepository.registerCredential(
                userId = userId,
                authenticatorAttachment = attachment,
                deviceName = deviceName,
                onStep = { step ->
                    _uiState.update {
                        it.copy(stepDescription = stepToDescription(step))
                    }
                }
            )

            result.fold(
                onSuccess = { createResult ->
                    _uiState.update {
                        it.copy(
                            isRegistering = false,
                            isRegistered = true,
                            stepDescription = null,
                            credential = HardwareTokenCredential(
                                credentialId = createResult.credentialId.take(32) + "...",
                                publicKeyAlgorithm = createResult.publicKeyAlgorithm,
                                attestationFormat = createResult.attestationFormat,
                                transports = createResult.transports.split(",").filter { t -> t.isNotBlank() },
                                registeredAt = "Just now"
                            ),
                            successMessage = "Credential registered successfully!"
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isRegistering = false,
                            stepDescription = null,
                            errorMessage = mapErrorMessage(throwable)
                        )
                    }
                }
            )
        }
    }

    /**
     * Verify (authenticate) using an existing WebAuthn credential.
     */
    fun verify() {
        val userId = tokenManager.getUserId()
        if (userId.isNullOrBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Not logged in. Please sign in first.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isVerifying = true,
                errorMessage = null,
                successMessage = null,
                stepDescription = "Preparing..."
            )
        }

        viewModelScope.launch {
            val result = webAuthnRepository.verifyCredential(
                userId = userId,
                allowCredentialIds = emptyList(),
                onStep = { step ->
                    _uiState.update {
                        it.copy(stepDescription = stepToDescription(step))
                    }
                }
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            isVerified = true,
                            stepDescription = null,
                            successMessage = "Hardware token verified successfully!"
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            stepDescription = null,
                            errorMessage = mapErrorMessage(throwable)
                        )
                    }
                }
            )
        }
    }

    /**
     * Called from the Android screen after a successful FIDO2 registration.
     * Kept for backward compatibility with direct Credential Manager calls from UI.
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

    private fun stepToDescription(step: WebAuthnStep): String = when (step) {
        WebAuthnStep.FetchingOptions -> "Requesting challenge from server..."
        WebAuthnStep.WaitingForAuthenticator -> "Waiting for authenticator..."
        WebAuthnStep.VerifyingWithServer -> "Verifying with server..."
        WebAuthnStep.Complete -> "Complete!"
    }

    private fun mapErrorMessage(throwable: Throwable): String {
        if (throwable is FingerprintAuthException) return throwable.message ?: "WebAuthn operation failed."
        val msg = throwable.message ?: "Unknown error"
        return when {
            msg.contains("cancelled", ignoreCase = true) -> "Operation was cancelled."
            msg.contains("timeout", ignoreCase = true) -> "Operation timed out. Please try again."
            msg.contains("not supported", ignoreCase = true) -> "WebAuthn is not supported on this device."
            msg.contains("no credential", ignoreCase = true) -> "No matching credential found. Register first."
            else -> "WebAuthn error: $msg"
        }
    }
}
