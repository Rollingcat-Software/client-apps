package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.FacialAction
import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Liveness challenge step with action to perform and completion status.
 */
data class LivenessChallengeStep(
    val action: FacialAction,
    val label: String,
    val completed: Boolean = false
)

/**
 * UI state for the liveness puzzle screen.
 */
data class LivenessUiState(
    val challengeSteps: List<LivenessChallengeStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val isLoading: Boolean = false,
    val isVerifying: Boolean = false,
    val isComplete: Boolean = false,
    val clientScore: Float = 0f,
    val serverScore: Float = 0f,
    val serverLive: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val currentStep: LivenessChallengeStep?
        get() = challengeSteps.getOrNull(currentStepIndex)

    val completedSteps: Int
        get() = challengeSteps.count { it.completed }

    val totalSteps: Int
        get() = challengeSteps.size

    val allStepsCompleted: Boolean
        get() = challengeSteps.isNotEmpty() && challengeSteps.all { it.completed }
}

/**
 * ViewModel for the face liveness puzzle.
 *
 * Manages challenge steps (blink, smile, turn head, nod, open mouth),
 * tracks completion, captures frames, and verifies with the server.
 */
class LivenessViewModel(
    private val biometricRepository: BiometricRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(LivenessUiState())
    val uiState: StateFlow<LivenessUiState> = _uiState.asStateFlow()

    /**
     * Initialize a new liveness challenge with random steps.
     */
    fun startChallenge() {
        val allActions = listOf(
            LivenessChallengeStep(FacialAction.BLINK, "Blink your eyes"),
            LivenessChallengeStep(FacialAction.SMILE, "Smile"),
            LivenessChallengeStep(FacialAction.LOOK_LEFT, "Turn your head left"),
            LivenessChallengeStep(FacialAction.LOOK_RIGHT, "Turn your head right"),
            LivenessChallengeStep(FacialAction.LOOK_UP, "Nod your head up"),
            LivenessChallengeStep(FacialAction.OPEN_MOUTH, "Open your mouth")
        )
        // Pick 3-4 random challenges
        val selected = allActions.shuffled().take(4)
        _uiState.value = LivenessUiState(challengeSteps = selected)
    }

    /**
     * Mark the current step as completed and advance to the next.
     */
    fun completeCurrentStep() {
        _uiState.update { state ->
            val steps = state.challengeSteps.toMutableList()
            if (state.currentStepIndex < steps.size) {
                steps[state.currentStepIndex] = steps[state.currentStepIndex].copy(completed = true)
            }
            val nextIndex = state.currentStepIndex + 1
            val score = steps.count { it.completed }.toFloat() / steps.size.toFloat()
            state.copy(
                challengeSteps = steps,
                currentStepIndex = if (nextIndex < steps.size) nextIndex else state.currentStepIndex,
                clientScore = score
            )
        }
    }

    /**
     * Verify liveness with the server using a captured frame.
     */
    fun verifyWithServer(imageBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, errorMessage = null) }

            biometricRepository.checkLiveness(imageBytes).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            isComplete = true,
                            serverScore = result.livenessScore,
                            serverLive = result.isLive,
                            successMessage = if (result.isLive) "Liveness verified!" else "Liveness check failed"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "verify liveness")
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun reset() {
        _uiState.value = LivenessUiState()
    }
}
