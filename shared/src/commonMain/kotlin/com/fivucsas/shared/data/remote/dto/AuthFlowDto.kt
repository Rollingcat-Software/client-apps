package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.AuthFlow
import com.fivucsas.shared.domain.model.AuthFlowStep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthFlowDto(
    val id: String = "",
    @SerialName("tenant_id") val tenantId: String = "",
    val name: String = "",
    @SerialName("operation_type") val operationType: String = "",
    @SerialName("is_active") val isActive: Boolean = true,
    val steps: List<AuthFlowStepDto> = emptyList(),
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class AuthFlowStepDto(
    val id: String = "",
    @SerialName("step_order") val stepOrder: Int = 0,
    @SerialName("auth_method") val authMethod: String = "",
    @SerialName("is_required") val isRequired: Boolean = true,
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
