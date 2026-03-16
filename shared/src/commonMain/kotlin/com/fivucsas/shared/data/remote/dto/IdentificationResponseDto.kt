package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.IdentifyResult
import kotlinx.serialization.Serializable

@Serializable
data class IdentificationResponseDto(
    val userId: String,
    val name: String,
    val confidence: Float,
    val isMatch: Boolean
)

fun IdentificationResponseDto.toModel(): IdentifyResult {
    return IdentifyResult(
        userId = userId,
        name = name,
        confidence = confidence,
        isMatch = isMatch
    )
}
