package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.VerificationFlow
import com.fivucsas.shared.domain.model.VerificationSession
import com.fivucsas.shared.domain.model.VerificationStepResult
import com.fivucsas.shared.domain.model.VerificationStepSpec
import kotlinx.serialization.Serializable

/**
 * Verification pipeline DTOs — server returns camelCase JSON (Spring Boot / Jackson)
 */

@Serializable
data class VerificationStepSpecDto(
    val stepOrder: Int = 0,
    val stepType: String = "",
    val isRequired: Boolean = true,
    val confidenceThreshold: Double = 0.0,
    val timeoutSeconds: Int = 0
)

@Serializable
data class VerificationFlowDto(
    val id: String = "",
    val tenantId: String = "",
    val name: String = "",
    val flowType: String = "",
    val templateId: String? = null,
    val templateName: String? = null,
    val steps: List<VerificationStepSpecDto> = emptyList(),
    val status: String = "active",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class VerificationStepResultDto(
    val stepOrder: Int = 0,
    val stepType: String = "",
    val status: String = "pending",
    val confidenceScore: Double? = null,
    val completedAt: String? = null,
    val failureReason: String? = null
)

@Serializable
data class VerificationSessionDto(
    val id: String = "",
    val userId: String = "",
    val flowId: String = "",
    val flowName: String = "",
    val status: String = "pending",
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val steps: List<VerificationStepResultDto> = emptyList(),
    val verificationLevel: String? = null,
    val startedAt: String = "",
    val completedAt: String? = null
)

@Serializable
data class CreateVerificationSessionRequest(
    val flowId: String,
    val userId: String
)

// ── Mappers ────────────────────────────────────────────────────────────────

fun VerificationStepSpecDto.toDomain(): VerificationStepSpec = VerificationStepSpec(
    stepOrder = stepOrder,
    stepType = stepType,
    isRequired = isRequired,
    confidenceThreshold = confidenceThreshold,
    timeoutSeconds = timeoutSeconds
)

fun VerificationFlowDto.toDomain(): VerificationFlow = VerificationFlow(
    id = id,
    tenantId = tenantId,
    name = name,
    flowType = flowType,
    templateId = templateId,
    templateName = templateName,
    steps = steps.map { it.toDomain() },
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun VerificationStepResultDto.toDomain(): VerificationStepResult = VerificationStepResult(
    stepOrder = stepOrder,
    stepType = stepType,
    status = status,
    confidenceScore = confidenceScore,
    completedAt = completedAt,
    failureReason = failureReason
)

fun VerificationSessionDto.toDomain(): VerificationSession = VerificationSession(
    id = id,
    userId = userId,
    flowId = flowId,
    flowName = flowName,
    status = status,
    currentStep = currentStep,
    totalSteps = totalSteps,
    steps = steps.map { it.toDomain() },
    verificationLevel = verificationLevel,
    startedAt = startedAt,
    completedAt = completedAt
)
