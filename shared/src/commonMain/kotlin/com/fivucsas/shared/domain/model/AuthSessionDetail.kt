package com.fivucsas.shared.domain.model

/**
 * Domain model for a detailed auth session with steps.
 */
data class AuthSessionDetail(
    val sessionId: String,
    val tenantId: String,
    val userId: String,
    val operationType: String,
    val status: String,
    val currentStepOrder: Int,
    val totalSteps: Int,
    val steps: List<SessionStep>,
    val expiresAt: String,
    val createdAt: String,
    val completedAt: String? = null
)

/**
 * Individual step in a multi-step auth session.
 */
data class SessionStep(
    val stepOrder: Int,
    val authMethodType: String,
    val isRequired: Boolean,
    val status: String,
    val completedAt: String? = null,
    val delegated: Boolean = false
)

/**
 * Result of completing or skipping a step.
 */
data class StepResult(
    val sessionId: String,
    val stepOrder: Int,
    val status: String,
    val message: String? = null,
    val nextStepOrder: Int? = null,
    val sessionCompleted: Boolean = false,
    val data: Map<String, String>? = null
)
