package com.fivucsas.shared.platform

interface FingerprintAuthenticator {
    suspend fun isSupported(): Boolean
    suspend fun getOrCreateKeyId(): String
    suspend fun getPublicKeyJwk(): String
    suspend fun signNonce(nonce: String): String
}

open class FingerprintAuthException(
    message: String,
    val recoverable: Boolean = true
) : Exception(message)

