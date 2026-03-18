package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoiceEnrollRequestDto(
    val voiceData: String // base64-encoded audio
)

@Serializable
data class VoiceEnrollResponseDto(
    val success: Boolean,
    @SerialName("user_id")
    val userId: String = "",
    val message: String = "",
    @SerialName("quality_score")
    val qualityScore: Float = 0f
)

@Serializable
data class VoiceVerifyResponseDto(
    val verified: Boolean = false,
    val confidence: Float = 0f,
    val message: String = ""
)

@Serializable
data class VoiceSearchResponseDto(
    val found: Boolean = false,
    @SerialName("user_id")
    val userId: String? = null,
    val confidence: Float = 0f,
    val message: String = ""
)
