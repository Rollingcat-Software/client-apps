package com.fivucsas.shared.ui.navigation

/**
 * Single source of truth for navigation route ids across platforms.
 *
 * Note: Some flows still use platform-specific route templates (with arguments).
 * Base ids in this object are used to keep route naming consistent.
 */
object RouteIds {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot-password"
    const val DASHBOARD = "dashboard"
    const val ACTIVITY_HISTORY = "activity-history"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit-profile"
    const val CHANGE_PASSWORD = "change-password"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val HELP = "help"
    const val ABOUT = "about"
    const val QR_LOGIN_SCAN = "qr-login-scan"
    const val QR_LOGIN_DISPLAY = "qr-login-display"

    const val UNAUTHORIZED = "unauthorized"
    const val GUEST_FACE_CHECK_CAPTURE = "guest-face-check"
    const val GUEST_FACE_CHECK_RESULT = "guest-face-check-result"

    const val ADMIN_DASHBOARD = "admin-dashboard"
    const val OPERATOR_DASHBOARD = "operator-dashboard"
    const val USERS_MANAGEMENT = "users-management"
    const val EXAM_ENTRY = "exam-entry"
    const val IDENTIFY_TENANT = "identify-tenant"
    const val INVITE_ACCEPT = "invite-accept"
    const val INVITE_MANAGEMENT = "invite-management"
    const val MY_INVITATIONS = "my-invitations"
    const val REQUEST_MEMBERSHIP = "request-membership"
    const val CARD_SCAN = "card-scan"
    const val TENANT_HISTORY = "tenant-history"
    const val TENANT_SETTINGS = "tenant-settings"

    const val BIOMETRIC_ENROLL = "biometric/enroll"
    const val BIOMETRIC_VERIFY = "biometric/verify"

    const val FINGERPRINT_GATE_ANDROID = "fingerprint-gate"
    const val FINGERPRINT_SUCCESS_ANDROID = "fingerprint-success"
    const val FINGERPRINT_FAILURE_ANDROID = "fingerprint-failure"

    const val FINGERPRINT_GATE_COMMON = "fingerprint/gate"
    const val FINGERPRINT_SUCCESS_COMMON = "fingerprint/success"
    const val FINGERPRINT_FAILURE_COMMON = "fingerprint/failure"

    const val DESKTOP_LAUNCHER = "desktop/launcher"
    const val DESKTOP_KIOSK = "desktop/kiosk"
    const val DESKTOP_ADMIN = "desktop/admin"
    const val DESKTOP_QR_LOGIN = "desktop/qr-login"
    const val DESKTOP_GUEST_FACE_CHECK = "desktop/guest-face-check"
    const val DESKTOP_LOGIN = "desktop/login"
    const val DESKTOP_REGISTER = "desktop/register"
    const val DESKTOP_FORGOT_PASSWORD = "desktop/forgot-password"
    const val DESKTOP_USER_HOME = "desktop/home/user"
    const val DESKTOP_MEMBER_HOME = "desktop/home/member"
    const val DESKTOP_TENANT_ADMIN_HOME = "desktop/home/tenant-admin"
    const val DESKTOP_ROOT_HOME = "desktop/home/root"

    const val TENANT_MANAGE = "tenant-manage"
    const val PLATFORM_HEALTH = "platform-health"
    const val PLATFORM_AUDIT = "platform-audit"
    const val PLATFORM_SETTINGS = "platform-settings"
}
