package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.CreateBiometricChallengeResponseDto
import com.fivucsas.shared.data.remote.dto.RegisterBiometricDeviceRequestDto
import com.fivucsas.shared.data.remote.dto.VerifyBiometricSignatureRequestDto
import com.fivucsas.shared.data.remote.dto.VerifyBiometricSignatureResponseDto

interface AuthBiometricApi {
    suspend fun registerDevice(request: RegisterBiometricDeviceRequestDto)
    suspend fun createChallenge(): CreateBiometricChallengeResponseDto
    suspend fun verifySignature(request: VerifyBiometricSignatureRequestDto): VerifyBiometricSignatureResponseDto
}

