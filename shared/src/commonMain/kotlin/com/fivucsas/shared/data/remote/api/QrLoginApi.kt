package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.QrLoginApproveRequestDto
import com.fivucsas.shared.data.remote.dto.QrLoginCreateSessionRequestDto
import com.fivucsas.shared.data.remote.dto.QrLoginSessionResponseDto

interface QrLoginApi {
    suspend fun createSession(request: QrLoginCreateSessionRequestDto): QrLoginSessionResponseDto
    suspend fun getSession(sessionId: String): QrLoginSessionResponseDto
    suspend fun approveSession(sessionId: String, request: QrLoginApproveRequestDto)
}
