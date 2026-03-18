package com.fivucsas.shared.domain.model

/**
 * Auth method info — describes an authentication method available in the system
 */
data class AuthMethodInfo(
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
 * Auth Flow domain model
 * Represents a configured authentication flow for a tenant
 */
data class AuthFlow(
    val id: String,
    val tenantId: String,
    val name: String,
    val description: String? = null,
    val operationType: String,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val stepCount: Int = 0,
    val steps: List<AuthFlowStep> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class AuthFlowStep(
    val id: String,
    val stepOrder: Int,
    val authMethod: AuthMethodInfo = AuthMethodInfo(),
    val isRequired: Boolean = true,
    val timeoutSeconds: Int = 120,
    val maxAttempts: Int = 5,
    val fallbackMethod: String? = null,
    val allowsDelegation: Boolean = false
)
