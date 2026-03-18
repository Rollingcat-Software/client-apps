package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.Device
import com.fivucsas.shared.domain.model.WebAuthnCredential
import kotlinx.serialization.Serializable

/**
 * Device DTO — server returns camelCase JSON (Spring Boot / Jackson)
 */
@Serializable
data class DeviceDto(
    val id: String = "",
    val userId: String = "",
    val deviceName: String = "",
    val platform: String = "",
    val publicKey: String? = null,
    val isStepUpEnabled: Boolean = false,
    val registeredAt: String = "",
    val lastUsedAt: String = ""
)

/**
 * WebAuthn credential DTO — server returns camelCase JSON
 */
@Serializable
data class WebAuthnCredentialDto(
    val id: String = "",
    val credentialId: String = "",
    val publicKey: String = "",
    val signCount: Long = 0,
    val createdAt: String = ""
)

fun DeviceDto.toDomain(): Device = Device(
    id = id,
    userId = userId,
    deviceName = deviceName,
    platform = platform,
    publicKey = publicKey,
    isStepUpEnabled = isStepUpEnabled,
    registeredAt = registeredAt,
    lastUsedAt = lastUsedAt
)

fun WebAuthnCredentialDto.toDomain(): WebAuthnCredential = WebAuthnCredential(
    id = id,
    credentialId = credentialId,
    publicKey = publicKey,
    signCount = signCount,
    createdAt = createdAt
)
