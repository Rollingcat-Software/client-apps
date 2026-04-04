package com.fivucsas.shared.presentation.state

/**
 * Available step-up methods the user can choose from.
 */
enum class StepUpMethod {
    FINGERPRINT,
    FACE,
    TOTP
}

data class StepUpAuthUiState(
    val reason: String = "",
    val availableMethods: List<StepUpMethod> = listOf(
        StepUpMethod.FINGERPRINT,
        StepUpMethod.FACE,
        StepUpMethod.TOTP
    ),
    val selectedMethod: StepUpMethod? = null,
    val isVerifying: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val stepUpToken: String? = null
)
