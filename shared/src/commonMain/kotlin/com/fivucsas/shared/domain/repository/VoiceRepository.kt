package com.fivucsas.shared.domain.repository

interface VoiceRepository {
    suspend fun enroll(userId: String, voiceBase64: String): Result<VoiceEnrollResult>
    suspend fun verify(userId: String, voiceBase64: String): Result<VoiceVerifyResult>
    suspend fun search(voiceBase64: String): Result<VoiceSearchResult>
}

data class VoiceEnrollResult(
    val success: Boolean,
    val message: String,
    val qualityScore: Float = 0f
)

data class VoiceVerifyResult(
    val verified: Boolean,
    val confidence: Float,
    val message: String
)

data class VoiceSearchMatch(
    val userId: String,
    val similarity: Float,
    val userName: String? = null,
    val userEmail: String? = null
)

data class VoiceSearchResult(
    val found: Boolean,
    val userId: String?,
    val confidence: Float,
    val message: String,
    val matches: List<VoiceSearchMatch> = emptyList()
)
