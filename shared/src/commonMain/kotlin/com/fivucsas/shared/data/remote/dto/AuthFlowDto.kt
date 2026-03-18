package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.AuthFlow
import com.fivucsas.shared.domain.model.AuthFlowStep
import kotlinx.serialization.Serializable

/**
 * Auth flow DTO — server returns camelCase JSON (Spring Boot / Jackson)
 */
@Serializable
data class AuthFlowDto(
    val id: String = "",
    val tenantId: String = "",
    val name: String = "",
    val operationType: String = "",
    val isActive: Boolean = true,
    val steps: List<AuthFlowStepDto> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Auth flow step DTO — server returns camelCase JSON
 */
@Serializable
data class AuthFlowStepDto(
    val id: String = "",
    val stepOrder: Int = 0,
    val authMethod: String = "",
    val isRequired: Boolean = true,
    val config: Map<String, String> = emptyMap()
)

fun AuthFlowDto.toDomain(): AuthFlow = AuthFlow(
    id = id,
    tenantId = tenantId,
    name = name,
    operationType = operationType,
    isActive = isActive,
    steps = steps.map { it.toDomain() },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AuthFlowStepDto.toDomain(): AuthFlowStep = AuthFlowStep(
    id = id,
    stepOrder = stepOrder,
    authMethod = authMethod,
    isRequired = isRequired,
    config = config
)
