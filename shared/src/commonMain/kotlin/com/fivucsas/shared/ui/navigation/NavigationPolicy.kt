package com.fivucsas.shared.ui.navigation

import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission

/**
 * Shared navigation policy for route access and role-based route decisions.
 * Platform renderers (Android/Desktop) should delegate decisions here.
 */
object NavigationPolicy {
    fun loginSuccessRoute(role: UserRole?): String {
        // Hosted-first / thin-companion architecture (2026-06-02 lock): the mobile
        // app no longer ships admin/root management surfaces — those live only on
        // the web dashboard (app.fivucsas.com). Every role lands on the personal
        // DASHBOARD; admins use the web for management tools.
        return RouteIds.DASHBOARD
    }

    fun postQrApprovalRoute(role: UserRole): String {
        // Same thin-companion rule as loginSuccessRoute: no admin/root home on
        // mobile, so every role returns to the personal DASHBOARD after approving
        // a cross-device QR login.
        return RouteIds.DASHBOARD
    }

    fun canAccessRoute(role: UserRole, routeId: String): Boolean {
        return when {
            routeId == RouteIds.ADMIN_DASHBOARD -> role == UserRole.ROOT || role == UserRole.TENANT_ADMIN
            routeId == RouteIds.USERS_MANAGEMENT -> canAccess(role, Permission.TENANT_USERS_READ)
            routeId == RouteIds.ACTIVITY_HISTORY -> canAccess(role, Permission.HISTORY_READ_SELF)
            routeId == RouteIds.TENANT_HISTORY -> canAccess(role, Permission.HISTORY_READ_TENANT)
            routeId == RouteIds.TENANT_SETTINGS -> canAccess(role, Permission.TENANT_SETTINGS_READ)
            routeId == RouteIds.QR_LOGIN_SCAN -> canAccessAny(role, setOf(Permission.QR_SCAN, Permission.QR_DISPLAY))
            routeId == RouteIds.GUEST_FACE_CHECK_CAPTURE -> canAccess(role, Permission.GUEST_FACE_CHECK)
            routeId == RouteIds.INVITE_ACCEPT -> canAccess(role, Permission.TENANT_INVITE_ACCEPT)
            routeId == RouteIds.MY_INVITATIONS -> canAccess(role, Permission.TENANT_INVITE_ACCEPT)
            routeId == RouteIds.REQUEST_MEMBERSHIP -> canAccess(role, Permission.TENANT_MEMBERSHIP_REQUEST)
            routeId == RouteIds.CARD_SCAN -> canAccess(role, Permission.CARD_ADD_SELF)
            routeId == RouteIds.IDENTIFY_TENANT -> canAccess(role, Permission.IDENTIFY_TENANT)
            routeId == RouteIds.INVITE_MANAGEMENT -> canAccess(role, Permission.TENANT_INVITE_CREATE)
            routeId == RouteIds.BIOMETRIC_ENROLL -> canAccess(role, Permission.ENROLL_SELF_CREATE)
            routeId == RouteIds.BIOMETRIC_VERIFY -> canAccess(role, Permission.VERIFY_SELF)
            routeId == RouteIds.PROFILE || routeId == RouteIds.DESKTOP_PROFILE -> canAccess(role, Permission.PROFILE_READ_SELF)
            routeId == RouteIds.EDIT_PROFILE || routeId == RouteIds.DESKTOP_EDIT_PROFILE -> canAccess(role, Permission.PROFILE_UPDATE_SELF)
            routeId == RouteIds.DESKTOP_SETTINGS_HELP -> true
            routeId == RouteIds.DESKTOP_ADMIN_INVITE_MANAGEMENT -> canAccess(role, Permission.TENANT_INVITE_CREATE)
            routeId == RouteIds.DESKTOP_EXAM_ENTRY || routeId == RouteIds.EXAM_ENTRY -> true
            routeId == RouteIds.DESKTOP_ANALYTICS -> canAccess(role, Permission.HISTORY_READ_TENANT)
            routeId == RouteIds.DESKTOP_MY_INVITATIONS -> canAccess(role, Permission.TENANT_INVITE_ACCEPT)
            routeId == RouteIds.DESKTOP_REQUEST_MEMBERSHIP -> canAccess(role, Permission.TENANT_MEMBERSHIP_REQUEST)
            routeId == RouteIds.ROOT_CONSOLE -> role == UserRole.ROOT
            routeId == RouteIds.ROOT_TENANT_MANAGEMENT -> canAccess(role, Permission.TENANT_MANAGE)
            routeId == RouteIds.ROOT_TENANT_DETAIL -> canAccess(role, Permission.TENANT_MANAGE)
            routeId == RouteIds.ROOT_GLOBAL_USER_DIRECTORY -> canAccess(role, Permission.TENANT_USERS_READ)
            routeId == RouteIds.ROOT_USERS -> canAccess(role, Permission.TENANT_USERS_READ)
            routeId == RouteIds.ROOT_TENANT_MEMBERS -> canAccess(role, Permission.TENANT_USERS_READ)
            routeId == RouteIds.ROOT_TENANT_ADMINS -> canAccess(role, Permission.TENANT_USERS_READ)
            routeId == RouteIds.ROOT_INVITE_MANAGEMENT -> canAccess(role, Permission.TENANT_INVITE_CREATE)
            routeId == RouteIds.ROOT_ROLES_PERMISSIONS -> canAccess(role, Permission.TENANT_ROLES_ASSIGN)
            routeId == RouteIds.ROOT_AUDIT_EXPLORER -> canAccess(role, Permission.PLATFORM_AUDIT_READ)
            routeId == RouteIds.ROOT_SECURITY_EVENTS -> canAccess(role, Permission.PLATFORM_HEALTH_READ)
            routeId == RouteIds.ROOT_SYSTEM_SETTINGS -> canAccess(role, Permission.PLATFORM_SETTINGS_UPDATE)
            // Explicit gating for sensitive routes that previously fell through to
            // the fail-open `else -> true` default. Mirrors the DESKTOP_ANALYTICS
            // case (tenant analytics require tenant-history read).
            routeId == RouteIds.ANALYTICS -> canAccess(role, Permission.HISTORY_READ_TENANT)
            // Hardware security-key (FIDO2/WebAuthn) registration is a self-service
            // enrollment action — same gate as biometric self-enrollment.
            routeId == RouteIds.HARDWARE_TOKEN -> canAccess(role, Permission.ENROLL_SELF_CREATE)
            // Operator console is an elevated surface — restrict to ROOT / TENANT_ADMIN
            // (same set as ADMIN_DASHBOARD).
            routeId == RouteIds.OPERATOR_DASHBOARD -> role == UserRole.ROOT || role == UserRole.TENANT_ADMIN

            // ── Permission-gated route ids that share a name with a Permission ──
            // These RouteIds string constants (e.g. "tenant-manage") are platform
            // capability surfaces, not the Permission enum values. Gate each to the
            // matching platform/root permission so they cannot fall through.
            routeId == RouteIds.TENANT_MANAGE -> canAccess(role, Permission.TENANT_MANAGE)
            routeId == RouteIds.PLATFORM_HEALTH -> canAccess(role, Permission.PLATFORM_HEALTH_READ)
            routeId == RouteIds.PLATFORM_AUDIT -> canAccess(role, Permission.PLATFORM_AUDIT_READ)
            routeId == RouteIds.PLATFORM_SETTINGS -> canAccess(role, Permission.PLATFORM_SETTINGS_UPDATE)

            // ── Voice / face-search surfaces ────────────────────────────────────
            // 1:N voice search across the tenant is an identify-tenant action;
            // single-user voice auth (VOICE_AUTH) is self-service (handled below).
            routeId == RouteIds.VOICE_SEARCH -> canAccess(role, Permission.IDENTIFY_TENANT)

            // ── Benign authenticated / pre-auth screens (default = allow) ────────
            // Every screen below is reachable by any user who has gotten this far;
            // access is governed by the composable's own isAuthenticated() check
            // (see fail-closed note at the end). The policy must not DENY these, or
            // legitimate users get locked out. Listing them explicitly keeps the
            // `else` branch a true fail-closed catch-all for genuinely-unknown ids.
            routeId == RouteIds.SPLASH -> true
            routeId == RouteIds.ONBOARDING -> true
            routeId == RouteIds.LOGIN -> true
            routeId == RouteIds.REGISTER -> true
            routeId == RouteIds.FORGOT_PASSWORD -> true
            routeId == RouteIds.DASHBOARD -> true
            routeId == RouteIds.CHANGE_PASSWORD -> true
            routeId == RouteIds.SETTINGS -> true
            routeId == RouteIds.NOTIFICATIONS -> true
            routeId == RouteIds.HELP -> true
            routeId == RouteIds.ABOUT -> true
            routeId == RouteIds.QR_LOGIN_DISPLAY -> true
            routeId == RouteIds.APPROVE_LOGIN -> true
            routeId == RouteIds.UNAUTHORIZED -> true
            routeId == RouteIds.GUEST_FACE_CHECK_RESULT -> true
            routeId == RouteIds.NFC_READ -> true

            // Second-factor enrollment / verification screens — self-service, the
            // user is mid-flow (often pre-full-auth) so the policy stays permissive.
            routeId == RouteIds.VOICE_AUTH -> true
            routeId == RouteIds.EMAIL_OTP -> true
            routeId == RouteIds.SMS_OTP -> true
            routeId == RouteIds.TOTP_ENROLL -> true
            routeId == RouteIds.BIOMETRIC_BACKUP -> true
            routeId == RouteIds.LIVENESS_PUZZLE -> true
            routeId == RouteIds.CARD_DETECTION -> true
            routeId == RouteIds.MFA_FLOW -> true
            routeId == RouteIds.AUTHENTICATOR -> true

            // Self-service account-management screens.
            routeId == RouteIds.AUTH_FLOWS -> true
            routeId == RouteIds.LINKED_ACCOUNTS -> true
            routeId == RouteIds.SESSIONS -> true
            routeId == RouteIds.DEVICES -> true
            routeId == RouteIds.ENROLLMENTS_LIST -> true

            // Fingerprint biometric-gate sub-flow (both android-suffixed and common ids).
            routeId == RouteIds.FINGERPRINT_GATE_ANDROID -> true
            routeId == RouteIds.FINGERPRINT_SUCCESS_ANDROID -> true
            routeId == RouteIds.FINGERPRINT_FAILURE_ANDROID -> true
            routeId == RouteIds.FINGERPRINT_GATE_COMMON -> true
            routeId == RouteIds.FINGERPRINT_SUCCESS_COMMON -> true
            routeId == RouteIds.FINGERPRINT_FAILURE_COMMON -> true

            // Desktop shell / home / auth routes — desktop renderer routes users to
            // the correct home by role itself; the policy must not block the shell.
            routeId == RouteIds.DESKTOP_LAUNCHER -> true
            routeId == RouteIds.DESKTOP_KIOSK -> true
            routeId == RouteIds.DESKTOP_ADMIN -> true
            routeId == RouteIds.DESKTOP_QR_LOGIN -> true
            routeId == RouteIds.DESKTOP_GUEST_FACE_CHECK -> true
            routeId == RouteIds.DESKTOP_LOGIN -> true
            routeId == RouteIds.DESKTOP_REGISTER -> true
            routeId == RouteIds.DESKTOP_FORGOT_PASSWORD -> true
            routeId == RouteIds.DESKTOP_USER_HOME -> true
            routeId == RouteIds.DESKTOP_MEMBER_HOME -> true
            routeId == RouteIds.DESKTOP_TENANT_ADMIN_HOME -> true
            routeId == RouteIds.DESKTOP_ROOT_HOME -> true

            // Embeddable widget / verification / developer surfaces — reachable by
            // any authenticated user; the screens guard their own data access.
            routeId == RouteIds.AUTH_WIDGET -> true
            routeId == RouteIds.VERIFICATION_DASHBOARD -> true
            routeId == RouteIds.VERIFICATION_SESSION_DETAIL -> true
            routeId == RouteIds.WIDGET_DEMO -> true
            routeId == RouteIds.DEVELOPER_PORTAL -> true

            // ── FAIL-CLOSED DEFAULT ─────────────────────────────────────────────
            // Anything that reaches this branch is a genuinely-unknown route id
            // (typo, removed screen, or a newly-added RouteIds constant that has
            // not yet been classified here). Deny by default — an unrecognized
            // route must never be silently allowed.
            //
            // Auth-vs-permission is LAYERED, not collapsed into this method:
            //   1. This policy answers "may this ROLE reach this route id?" — it is
            //      a permission/role gate, returning `true` for benign screens that
            //      any role may legitimately reach.
            //   2. The destination composable still calls isAuthenticated() (and,
            //      for sensitive data, scopes queries to the caller). A `true` here
            //      is NOT a grant of an unauthenticated session.
            // When adding a new RouteIds constant, add an explicit case above:
            // map it to the right Permission if sensitive, or to `true` if it is a
            // benign authenticated screen. Do NOT rely on this default to allow it.
            else -> false
        }
    }

    private fun canAccess(role: UserRole, permission: Permission): Boolean {
        return role.hasPermission(permission)
    }

    private fun canAccessAny(role: UserRole, permissions: Set<Permission>): Boolean {
        return permissions.any { role.hasPermission(it) }
    }
}
