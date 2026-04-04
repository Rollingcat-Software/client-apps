package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.WebAuthnRegistrationOptionsDto
import com.fivucsas.shared.data.remote.dto.WebAuthnRegistrationVerifyRequestDto
import com.fivucsas.shared.data.remote.dto.WebAuthnRegistrationVerifyResponseDto

/**
 * WebAuthn API interface for FIDO2 credential management.
 *
 * Endpoints:
 * - POST /webauthn/register/options/{userId}  -> registration challenge
 * - POST /webauthn/register/verify            -> verify attestation & store credential
 */
interface WebAuthnApi {
    /**
     * Request registration options (challenge, rpId, etc.) for creating a new credential.
     */
    suspend fun getRegistrationOptions(userId: String): WebAuthnRegistrationOptionsDto

    /**
     * Verify the attestation response and store the credential on the server.
     */
    suspend fun verifyRegistration(request: WebAuthnRegistrationVerifyRequestDto): WebAuthnRegistrationVerifyResponseDto
}
