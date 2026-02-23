package com.fivucsas.shared.domain.usecase.verification

import com.fivucsas.shared.domain.exception.BiometricErrorCode
import com.fivucsas.shared.domain.exception.BiometricException
import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.FacialAction
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.repository.BiometricRepository

/**
 * Use case for checking liveness (anti-spoofing)
 *
 * Business logic:
 * 1. Validate facial actions list
 * 2. Perform liveness detection
 * 3. Ensure minimum number of actions performed
 * 4. Return liveness result with confidence score
 *
 * Liveness detection prevents spoofing attacks:
 * - Photo attacks (holding a photo)
 * - Video replay attacks
 * - Mask attacks (3D masks)
 *
 * Example usage:
 * ```
 * val useCase = CheckLivenessUseCase(biometricRepo)
 * val actions = listOf(FacialAction.SMILE, FacialAction.BLINK, FacialAction.LOOK_LEFT)
 * val result = useCase(actions)
 * ```
 */
open class CheckLivenessUseCase(
    private val biometricRepository: BiometricRepository
) {
    /**
     * Execute liveness check
     *
     * @param actions List of facial actions performed
     * @return Result with liveness result or error
     */
    open suspend operator fun invoke(actions: List<FacialAction>): Result<LivenessResult> {
        // Validate actions list
        if (actions.isEmpty()) {
            return Result.failure(
                ValidationException("At least one facial action is required for liveness check")
            )
        }

        if (actions.size < MIN_ACTIONS_REQUIRED) {
            return Result.failure(
                BiometricException(
                    message = "Please perform at least $MIN_ACTIONS_REQUIRED different actions",
                    errorCode = BiometricErrorCode.LIVENESS_CHECK_FAILED
                )
            )
        }

        if (actions.size > MAX_ACTIONS_ALLOWED) {
            return Result.failure(
                ValidationException("Too many actions (max $MAX_ACTIONS_ALLOWED)")
            )
        }

        // Check for duplicate actions
        val uniqueActions = actions.distinct()
        if (uniqueActions.size < actions.size) {
            // Has duplicates - might be acceptable but note it
            // For now, we allow it
        }

        // Perform liveness check
        return biometricRepository.checkLiveness(actions)
    }

    companion object {
        private const val MIN_ACTIONS_REQUIRED = 2
        private const val MAX_ACTIONS_ALLOWED = 5
    }
}
