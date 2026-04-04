package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Auth session response from the backend.
 * Endpoint: GET /auth/sessions/{sessionId}
 */
@Serializable
data class AuthSessionDetailDto(
    val sessionId: String = "",
    val tenantId: String = "",
    val userId: String = "",
    val operationType: String = "",
    val status: String = "",
    val currentStepOrder: Int = 0,
    val totalSteps: Int = 0,
    val steps: List<SessionStepDto> = emptyList(),
    val expiresAt: String = "",
    val createdAt: String = "",
    val completedAt: String? = null
)

/**
 * Individual step within an auth session.
 */
@Serializable
data class SessionStepDto(
    val stepOrder: Int = 0,
    val authMethodType: String = "",
    val isRequired: Boolean = true,
    val status: String = "PENDING",
    val completedAt: String? = null,
    val delegated: Boolean = false
)

/**
 * Response from completing or skipping a step.
 * Endpoint: POST /auth/sessions/{sessionId}/steps/{stepOrder}
 */
@Serializable
data class StepResultDto(
    val sessionId: String = "",
    val stepOrder: Int = 0,
    val status: String = "",
    val message: String? = null,
    val nextStepOrder: Int? = null,
    val sessionCompleted: Boolean = false,
    val data: Map<String, String>? = null
)

/**
 * Command to start a new auth session.
 * Endpoint: POST /auth/sessions
 */
@Serializable
data class StartSessionCommand(
    val tenantId: String,
    val userId: String,
    val operationType: String,
    val deviceFingerprint: String? = null,
    val ipAddress: String? = null
)
