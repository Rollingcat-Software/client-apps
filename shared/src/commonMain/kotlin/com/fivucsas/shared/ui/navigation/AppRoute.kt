package com.fivucsas.shared.ui.navigation

sealed class AppRoute(val id: String) {
    object Splash : AppRoute("splash")
    object Onboarding : AppRoute("onboarding")
    object Login : AppRoute("login")
    object Register : AppRoute("register")
    object ForgotPassword : AppRoute("forgot-password")
    object Dashboard : AppRoute("dashboard")
    object ActivityHistory : AppRoute("activity-history")
    object Profile : AppRoute("profile")
    object EditProfile : AppRoute("edit-profile")
    object ChangePassword : AppRoute("change-password")
    object Settings : AppRoute("settings")
    object Notifications : AppRoute("notifications")
    object Help : AppRoute("help")
    object About : AppRoute("about")
    object QrLoginScan : AppRoute("qr-login-scan")
    object QrLoginDisplay : AppRoute("qr-login-display")

    data class FingerprintGate(val targetRouteId: String) : AppRoute(FINGERPRINT_GATE)
    object FingerprintSuccess : AppRoute(FINGERPRINT_SUCCESS)
    object FingerprintFailure : AppRoute(FINGERPRINT_FAILURE)

    data class BiometricEnroll(val userId: String) : AppRoute(BIOMETRIC_ENROLL)
    data class BiometricVerify(val userId: String) : AppRoute(BIOMETRIC_VERIFY)

    data class Platform(val key: String) : AppRoute(key)

    companion object {
        const val BIOMETRIC_ENROLL = "biometric/enroll"
        const val BIOMETRIC_VERIFY = "biometric/verify"
        const val FINGERPRINT_GATE = "fingerprint/gate"
        const val FINGERPRINT_SUCCESS = "fingerprint/success"
        const val FINGERPRINT_FAILURE = "fingerprint/failure"
    }
}
