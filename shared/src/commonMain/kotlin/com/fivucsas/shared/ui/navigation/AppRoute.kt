package com.fivucsas.shared.ui.navigation

sealed class AppRoute(val id: String) {
    object Splash : AppRoute(RouteIds.SPLASH)
    object Onboarding : AppRoute(RouteIds.ONBOARDING)
    object Login : AppRoute(RouteIds.LOGIN)
    object Register : AppRoute(RouteIds.REGISTER)
    object ForgotPassword : AppRoute(RouteIds.FORGOT_PASSWORD)
    object Dashboard : AppRoute(RouteIds.DASHBOARD)
    object ActivityHistory : AppRoute(RouteIds.ACTIVITY_HISTORY)
    object Profile : AppRoute(RouteIds.PROFILE)
    object EditProfile : AppRoute(RouteIds.EDIT_PROFILE)
    object ChangePassword : AppRoute(RouteIds.CHANGE_PASSWORD)
    object Settings : AppRoute(RouteIds.SETTINGS)
    object Notifications : AppRoute(RouteIds.NOTIFICATIONS)
    object Help : AppRoute(RouteIds.HELP)
    object About : AppRoute(RouteIds.ABOUT)
    object QrLoginScan : AppRoute(RouteIds.QR_LOGIN_SCAN)
    object QrLoginDisplay : AppRoute(RouteIds.QR_LOGIN_DISPLAY)
    object RootConsole : AppRoute(RouteIds.ROOT_CONSOLE)
    object RootTenantManagement : AppRoute(RouteIds.ROOT_TENANT_MANAGEMENT)
    data class RootTenantDetail(val tenantId: String) : AppRoute(RouteIds.ROOT_TENANT_DETAIL)
    object RootGlobalUserDirectory : AppRoute(RouteIds.ROOT_GLOBAL_USER_DIRECTORY)
    object RootUsers : AppRoute(RouteIds.ROOT_USERS)
    object RootTenantMembers : AppRoute(RouteIds.ROOT_TENANT_MEMBERS)
    object RootTenantAdmins : AppRoute(RouteIds.ROOT_TENANT_ADMINS)
    object RootInviteManagement : AppRoute(RouteIds.ROOT_INVITE_MANAGEMENT)
    object RootRolesPermissions : AppRoute(RouteIds.ROOT_ROLES_PERMISSIONS)
    object RootAuditExplorer : AppRoute(RouteIds.ROOT_AUDIT_EXPLORER)
    object RootSecurityEvents : AppRoute(RouteIds.ROOT_SECURITY_EVENTS)
    object RootSystemSettings : AppRoute(RouteIds.ROOT_SYSTEM_SETTINGS)

    data class AuthFlows(val tenantId: String) : AppRoute(RouteIds.AUTH_FLOWS)
    object Sessions : AppRoute(RouteIds.SESSIONS)
    data class Devices(val userId: String) : AppRoute(RouteIds.DEVICES)
    data class EnrollmentsList(val userId: String) : AppRoute(RouteIds.ENROLLMENTS_LIST)

    data class FingerprintGate(val targetRouteId: String) : AppRoute(FINGERPRINT_GATE)
    object FingerprintSuccess : AppRoute(FINGERPRINT_SUCCESS)
    object FingerprintFailure : AppRoute(FINGERPRINT_FAILURE)

    data class BiometricEnroll(val userId: String) : AppRoute(BIOMETRIC_ENROLL)
    data class BiometricVerify(val userId: String) : AppRoute(BIOMETRIC_VERIFY)

    data class VoiceVerify(val userId: String) : AppRoute(RouteIds.VOICE_AUTH)
    object FaceLiveness : AppRoute(RouteIds.LIVENESS_PUZZLE)
    object CardDetection : AppRoute(RouteIds.CARD_DETECTION)

    data class Platform(val key: String) : AppRoute(key)

    companion object {
        const val BIOMETRIC_ENROLL = RouteIds.BIOMETRIC_ENROLL
        const val BIOMETRIC_VERIFY = RouteIds.BIOMETRIC_VERIFY
        const val FINGERPRINT_GATE = RouteIds.FINGERPRINT_GATE_COMMON
        const val FINGERPRINT_SUCCESS = RouteIds.FINGERPRINT_SUCCESS_COMMON
        const val FINGERPRINT_FAILURE = RouteIds.FINGERPRINT_FAILURE_COMMON
    }
}
