package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.SessionStep

/**
 * Status of an individual auth step in the multi-step flow.
 */
enum class StepDisplayStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    SKIPPED
}

/**
 * UI state for the multi-step authentication flow screen.
 */
data class MultiStepAuthUiState(
    val sessionId: String = "",
    val steps: List<SessionStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val flowComplete: Boolean = false,
    val flowCancelled: Boolean = false,
    val accessToken: String? = null,
    val userId: String? = null,
    val stepResults: Map<Int, String> = emptyMap()
) {
    val currentStep: SessionStep?
        get() = steps.getOrNull(currentStepIndex)

    val totalSteps: Int
        get() = steps.size

    val currentStepNumber: Int
        get() = currentStepIndex + 1

    /**
     * Get the display status for a step at a given index.
     */
    fun getStepDisplayStatus(index: Int): StepDisplayStatus {
        val step = steps.getOrNull(index) ?: return StepDisplayStatus.PENDING
        return when (step.status) {
            "COMPLETED" -> StepDisplayStatus.COMPLETED
            "SKIPPED" -> StepDisplayStatus.SKIPPED
            "FAILED" -> StepDisplayStatus.FAILED
            "IN_PROGRESS" -> StepDisplayStatus.IN_PROGRESS
            else -> if (index == currentStepIndex && !flowComplete) {
                StepDisplayStatus.IN_PROGRESS
            } else {
                StepDisplayStatus.PENDING
            }
        }
    }
}
