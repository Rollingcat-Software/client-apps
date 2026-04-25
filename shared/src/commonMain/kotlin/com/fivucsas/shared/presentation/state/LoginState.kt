package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.AuthTokens

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tokens: AuthTokens? = null,
    val isSuccess: Boolean = false,
    val role: UserRole? = null,
    // MFA fields
    val mfaRequired: Boolean = false,
    val mfaSessionToken: String? = null,
    val mfaAvailableMethods: List<AvailableMethodDto>? = null,
    val mfaCurrentStep: Int = 1,
    val mfaTotalSteps: Int = 1,
    // Active auth flow discovery (PR #18 client side).
    //
    // While `flowDiscoveryLoading` is true, the LoginScreen renders a
    // shimmer instead of the credential form so the UI doesn't flash a
    // PASSWORD prompt before discovering that this tenant is actually
    // configured for a passwordless primary step.
    //
    // `primaryStepMethod` is the AuthMethodInfo.type of the FIRST step of
    // the active auth flow ("PASSWORD", "EMAIL_OTP", "FACE", "TOTP",
    // "QR_CODE", "SMS_OTP", ...). `null` means "no flow configured" or
    // "discovery failed" — the screen falls back to the legacy PASSWORD
    // form in both cases.
    val flowDiscoveryLoading: Boolean = false,
    val primaryStepMethod: String? = null
)
