package com.fivucsas.shared.domain.model

/**
 * Verification pipeline domain models.
 *
 * Mirrors the web-app VerificationRepository types
 * and the backend verification API (V26-V28).
 */

data class VerificationFlow(
    val id: String = "",
    val tenantId: String = "",
    val name: String = "",
    val flowType: String = "",
    val templateId: String? = null,
    val templateName: String? = null,
    val steps: List<VerificationStepSpec> = emptyList(),
    val status: String = "active",
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class VerificationStepSpec(
    val stepOrder: Int = 0,
    val stepType: String = "",
    val isRequired: Boolean = true,
    val confidenceThreshold: Double = 0.0,
    val timeoutSeconds: Int = 0
)

data class VerificationSession(
    val id: String = "",
    val userId: String = "",
    val flowId: String = "",
    val flowName: String = "",
    val status: String = "pending",
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val steps: List<VerificationStepResult> = emptyList(),
    val verificationLevel: String? = null,
    val startedAt: String = "",
    val completedAt: String? = null
)

data class VerificationStepResult(
    val stepOrder: Int = 0,
    val stepType: String = "",
    val status: String = "pending",
    val confidenceScore: Double? = null,
    val completedAt: String? = null,
    val failureReason: String? = null
)
