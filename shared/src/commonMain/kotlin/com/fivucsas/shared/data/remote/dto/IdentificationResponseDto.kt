package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.IdentifyResult
import kotlinx.serialization.Serializable

/**
 * 1:N face-search response.
 *
 * TODO(#104)(biometric-search): the Identity `/biometric/search` endpoint returns an
 * opaque processor map ({matches:[{user_id, similarity}], ...}) rather than this
 * flat shape. All fields default so deserialization never throws on the real
 * payload; remap to the processor's `matches` array once the search UI needs
 * real results. Identify/search is NOT on the login path.
 */
@Serializable
data class IdentificationResponseDto(
    val userId: String = "",
    val name: String = "",
    val confidence: Float = 0f,
    val isMatch: Boolean = false
)

fun IdentificationResponseDto.toModel(): IdentifyResult {
    return IdentifyResult(
        userId = userId,
        name = name,
        confidence = confidence,
        isMatch = isMatch
    )
}
