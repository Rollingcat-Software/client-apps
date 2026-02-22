package com.fivucsas.shared.domain.biometric

import com.fivucsas.shared.domain.model.BiometricCapability
import com.fivucsas.shared.domain.model.PublicKeyJwk

interface BiometricAuthenticator {
    suspend fun canAuthenticate(): BiometricCapability
    suspend fun ensureKeyPair(keyId: String): PublicKeyJwk
    suspend fun signNonceWithBiometric(keyId: String, nonce: ByteArray, reason: String): ByteArray
}
