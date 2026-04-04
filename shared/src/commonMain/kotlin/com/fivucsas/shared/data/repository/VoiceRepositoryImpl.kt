package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.VoiceApi
import com.fivucsas.shared.data.remote.dto.VoiceEnrollRequestDto
import com.fivucsas.shared.domain.repository.VoiceEnrollResult
import com.fivucsas.shared.domain.repository.VoiceRepository
import com.fivucsas.shared.domain.repository.VoiceSearchMatch
import com.fivucsas.shared.domain.repository.VoiceSearchResult
import com.fivucsas.shared.domain.repository.VoiceVerifyResult

class VoiceRepositoryImpl(
    private val voiceApi: VoiceApi
) : VoiceRepository {

    override suspend fun enroll(userId: String, voiceBase64: String): Result<VoiceEnrollResult> {
        return runCatching {
            val response = voiceApi.enroll(userId, VoiceEnrollRequestDto(voiceData = voiceBase64))
            VoiceEnrollResult(
                success = response.success,
                message = response.message,
                qualityScore = response.qualityScore
            )
        }
    }

    override suspend fun verify(userId: String, voiceBase64: String): Result<VoiceVerifyResult> {
        return runCatching {
            val response = voiceApi.verify(userId, VoiceEnrollRequestDto(voiceData = voiceBase64))
            VoiceVerifyResult(
                verified = response.verified,
                confidence = response.confidence,
                message = response.message
            )
        }
    }

    override suspend fun search(voiceBase64: String): Result<VoiceSearchResult> {
        return runCatching {
            val response = voiceApi.search(VoiceEnrollRequestDto(voiceData = voiceBase64))
            val matches = response.matches.map { m ->
                VoiceSearchMatch(
                    userId = m.userId,
                    similarity = m.similarity
                )
            }
            VoiceSearchResult(
                found = response.found,
                userId = response.userId,
                confidence = response.confidence,
                message = response.message,
                matches = matches
            )
        }
    }
}
