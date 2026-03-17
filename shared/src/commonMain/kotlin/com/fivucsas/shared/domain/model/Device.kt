package com.fivucsas.shared.domain.model

/**
 * Device domain model
 * Represents a registered user device
 */
data class Device(
    val id: String,
    val userId: String,
    val deviceName: String = "",
    val platform: String = "",
    val publicKey: String? = null,
    val isStepUpEnabled: Boolean = false,
    val registeredAt: String = "",
    val lastUsedAt: String = ""
)

/**
 * WebAuthn Credential domain model
 */
data class WebAuthnCredential(
    val id: String,
    val credentialId: String,
    val publicKey: String = "",
    val signCount: Long = 0,
    val createdAt: String = ""
)
