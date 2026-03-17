package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.Device
import com.fivucsas.shared.domain.model.WebAuthnCredential
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("device_name") val deviceName: String = "",
    val platform: String = "",
    @SerialName("public_key") val publicKey: String? = null,
    @SerialName("is_step_up_enabled") val isStepUpEnabled: Boolean = false,
    @SerialName("registered_at") val registeredAt: String = "",
    @SerialName("last_used_at") val lastUsedAt: String = ""
)

@Serializable
data class WebAuthnCredentialDto(
    val id: String = "",
    @SerialName("credential_id") val credentialId: String = "",
    @SerialName("public_key") val publicKey: String = "",
    @SerialName("sign_count") val signCount: Long = 0,
    @SerialName("created_at") val createdAt: String = ""
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
