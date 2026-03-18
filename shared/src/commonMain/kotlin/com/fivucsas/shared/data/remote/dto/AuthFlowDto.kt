package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.AuthFlow
import com.fivucsas.shared.domain.model.AuthFlowStep
import com.fivucsas.shared.domain.model.AuthMethodInfo
import kotlinx.serialization.Serializable

/**
 * Auth flow DTO — matches Identity Core API (Spring Boot / Jackson) response:
 * GET /tenants/{tenantId}/auth-flows
 *
 * Server returns: id, tenantId, name, description, operationType, isDefault,
 * isActive, stepCount, steps[], createdAt, updatedAt
 */
@Serializable
data class AuthFlowDto(
    val id: String = "",
    val tenantId: String = "",
    val name: String = "",
    val description: String? = null,
    val operationType: String = "",
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val stepCount: Int = 0,
    val steps: List<AuthFlowStepDto> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * Auth method info DTO — nested object inside each step.
 *
 * Server returns: id, type, name, description, category, platforms[],
 * requiresEnrollment, isActive
 */
@Serializable
data class AuthMethodInfoDto(
    val id: String = "",
    val type: String = "",
    val name: String = "",
    val description: String? = null,
    val category: String = "",
    val platforms: List<String> = emptyList(),
    val requiresEnrollment: Boolean = false,
    val isActive: Boolean = true
)

/**
 * Auth flow step DTO — server returns camelCase JSON.
 *
 * Server returns: id, stepOrder, authMethod (object), isRequired,
 * timeoutSeconds, maxAttempts, fallbackMethod, allowsDelegation, config (String)
 */
@Serializable
data class AuthFlowStepDto(
    val id: String = "",
    val stepOrder: Int = 0,
    val authMethod: AuthMethodInfoDto = AuthMethodInfoDto(),
    val isRequired: Boolean = true,
    val timeoutSeconds: Int = 120,
    val maxAttempts: Int = 5,
    val fallbackMethod: String? = null,
    val allowsDelegation: Boolean = false,
    val config: String? = null
)

fun AuthFlowDto.toDomain(): AuthFlow = AuthFlow(
    id = id,
    tenantId = tenantId,
    name = name,
    description = description,
    operationType = operationType,
    isDefault = isDefault,
    isActive = isActive,
    stepCount = stepCount,
    steps = steps.map { it.toDomain() },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AuthFlowStepDto.toDomain(): AuthFlowStep = AuthFlowStep(
    id = id,
    stepOrder = stepOrder,
    authMethod = AuthMethodInfo(
        id = authMethod.id,
        type = authMethod.type,
        name = authMethod.name,
        description = authMethod.description,
        category = authMethod.category,
        platforms = authMethod.platforms,
        requiresEnrollment = authMethod.requiresEnrollment,
        isActive = authMethod.isActive
    ),
    isRequired = isRequired,
    timeoutSeconds = timeoutSeconds,
    maxAttempts = maxAttempts,
    fallbackMethod = fallbackMethod,
    allowsDelegation = allowsDelegation
)
