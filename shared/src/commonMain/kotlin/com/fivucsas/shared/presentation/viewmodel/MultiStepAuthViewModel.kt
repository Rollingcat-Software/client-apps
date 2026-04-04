package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.SessionStep
import com.fivucsas.shared.domain.model.StepResult
import com.fivucsas.shared.domain.repository.AuthSessionRepository
import com.fivucsas.shared.presentation.state.MultiStepAuthUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing the multi-step authentication flow.
 *
 * Orchestrates sequential auth steps (e.g., Password -> Face -> TOTP),
 * communicates with the backend auth session API, and accumulates step results.
 */
class MultiStepAuthViewModel(
    private val authSessionRepository: AuthSessionRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(MultiStepAuthUiState())
    val uiState: StateFlow<MultiStepAuthUiState> = _uiState.asStateFlow()

    /**
     * Initialize the flow with an existing session ID.
     * Fetches session details from the backend and sets up step list.
     */
    fun initWithSessionId(sessionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authSessionRepository.getSession(sessionId).fold(
                onSuccess = { session ->
                    val activeIndex = findActiveStepIndex(session.steps)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sessionId = session.sessionId,
                            steps = session.steps.sortedBy { s -> s.stepOrder },
                            currentStepIndex = activeIndex,
                            userId = session.userId,
                            flowComplete = session.status == "COMPLETED"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load auth session")
                        )
                    }
                }
            )
        }
    }

    /**
     * Initialize the flow by starting a new session.
     */
    fun startNewSession(tenantId: String, userId: String, operationType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authSessionRepository.startSession(tenantId, userId, operationType).fold(
                onSuccess = { session ->
                    val activeIndex = findActiveStepIndex(session.steps)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sessionId = session.sessionId,
                            steps = session.steps.sortedBy { s -> s.stepOrder },
                            currentStepIndex = activeIndex,
                            userId = session.userId
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "start auth session")
                        )
                    }
                }
            )
        }
    }

    /**
     * Initialize the flow with a pre-loaded list of steps (no backend call).
     * Useful when the caller already has step data from an auth flow definition.
     */
    fun initWithSteps(sessionId: String, steps: List<SessionStep>, userId: String? = null) {
        val sorted = steps.sortedBy { it.stepOrder }
        val activeIndex = findActiveStepIndex(sorted)
        _uiState.update {
            it.copy(
                sessionId = sessionId,
                steps = sorted,
                currentStepIndex = activeIndex,
                userId = userId,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    /**
     * Submit data for the current step.
     */
    fun completeCurrentStep(data: Map<String, Any?>) {
        val state = _uiState.value
        val currentStep = state.currentStep ?: return
        val sessionId = state.sessionId
        if (sessionId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            authSessionRepository.completeStep(sessionId, currentStep.stepOrder, data).fold(
                onSuccess = { result ->
                    processStepResult(result)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "complete step")
                        )
                    }
                }
            )
        }
    }

    /**
     * Skip the current step (only allowed for optional steps).
     */
    fun skipCurrentStep() {
        val state = _uiState.value
        val currentStep = state.currentStep ?: return
        if (currentStep.isRequired) return
        val sessionId = state.sessionId
        if (sessionId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            authSessionRepository.skipStep(sessionId, currentStep.stepOrder).fold(
                onSuccess = { result ->
                    processStepResult(result)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "skip step")
                        )
                    }
                }
            )
        }
    }

    /**
     * Cancel the entire auth flow.
     */
    fun cancelFlow() {
        val sessionId = _uiState.value.sessionId
        if (sessionId.isBlank()) {
            _uiState.update { it.copy(flowCancelled = true) }
            return
        }

        viewModelScope.launch {
            // Best-effort cancellation
            authSessionRepository.cancelSession(sessionId)
            _uiState.update { it.copy(flowCancelled = true) }
        }
    }

    /**
     * Clear the current error to allow retry.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Process the result of a step completion or skip.
     */
    private fun processStepResult(result: StepResult) {
        _uiState.update { state ->
            val updatedSteps = state.steps.map { step ->
                if (step.stepOrder == result.stepOrder) {
                    step.copy(status = result.status)
                } else {
                    step
                }
            }

            val updatedResults = state.stepResults + (result.stepOrder to result.status)

            if (result.sessionCompleted) {
                state.copy(
                    isSubmitting = false,
                    steps = updatedSteps,
                    stepResults = updatedResults,
                    flowComplete = true,
                    accessToken = result.data?.get("accessToken"),
                )
            } else {
                val newIndex = findActiveStepIndex(updatedSteps)
                state.copy(
                    isSubmitting = false,
                    steps = updatedSteps,
                    stepResults = updatedResults,
                    currentStepIndex = newIndex
                )
            }
        }
    }

    /**
     * Find the index of the first step that is not completed, skipped, or failed.
     */
    private fun findActiveStepIndex(steps: List<SessionStep>): Int {
        val idx = steps.indexOfFirst { step ->
            step.status != "COMPLETED" && step.status != "SKIPPED" && step.status != "FAILED"
        }
        return if (idx == -1) steps.size else idx
    }
}
