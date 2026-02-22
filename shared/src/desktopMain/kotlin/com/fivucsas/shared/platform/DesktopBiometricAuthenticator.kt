package com.fivucsas.shared.platform

import com.fivucsas.shared.domain.biometric.BiometricAuthenticator
import com.fivucsas.shared.domain.model.BiometricCapability
import com.fivucsas.shared.domain.model.PublicKeyJwk

class DesktopBiometricAuthenticator : BiometricAuthenticator {
    override suspend fun canAuthenticate(): BiometricCapability = BiometricCapability.Unsupported

    override suspend fun ensureKeyPair(keyId: String): PublicKeyJwk {
        throw UnsupportedOperationException("Biometric step-up is not supported on desktop.")
    }

    override suspend fun signNonceWithBiometric(keyId: String, nonce: ByteArray, reason: String): ByteArray {
        throw UnsupportedOperationException("Biometric step-up is not supported on desktop.")
    }
}
