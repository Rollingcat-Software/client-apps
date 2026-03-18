package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.VoiceEnrollRequestDto
import com.fivucsas.shared.data.remote.dto.VoiceEnrollResponseDto
import com.fivucsas.shared.data.remote.dto.VoiceVerifyResponseDto
import com.fivucsas.shared.data.remote.dto.VoiceSearchResponseDto

/**
 * Voice biometric API interface
 *
 * Endpoints:
 * - POST /biometric/voice/enroll/{userId}
 * - POST /biometric/voice/verify/{userId}
 * - POST /biometric/voice/search
 */
interface VoiceApi {
    suspend fun enroll(userId: String, request: VoiceEnrollRequestDto): VoiceEnrollResponseDto
    suspend fun verify(userId: String, request: VoiceEnrollRequestDto): VoiceVerifyResponseDto
    suspend fun search(request: VoiceEnrollRequestDto): VoiceSearchResponseDto
}
