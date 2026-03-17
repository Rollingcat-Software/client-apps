package com.fivucsas.shared.domain.model

/**
 * Auth Flow domain model
 * Represents a configured authentication flow for a tenant
 */
data class AuthFlow(
    val id: String,
    val tenantId: String,
    val name: String,
    val operationType: String,
    val isActive: Boolean = true,
    val steps: List<AuthFlowStep> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class AuthFlowStep(
    val id: String,
    val stepOrder: Int,
    val authMethod: String,
    val isRequired: Boolean = true,
    val config: Map<String, String> = emptyMap()
)
