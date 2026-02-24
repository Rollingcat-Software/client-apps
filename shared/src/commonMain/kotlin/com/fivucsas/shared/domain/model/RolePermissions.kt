package com.fivucsas.shared.domain.model

/**
 * Permission constants for the FIVUCSAS platform.
 *
 * Design principles:
 * - AUTH_LOGIN / AUTH_LOGOUT are implicit (public endpoints), not listed here.
 * - NFC is a platform capability (future work).
 * - Liveness requirements are tenant policies, not permissions.
 * - Notification and app-settings permissions are reserved for future work.
 */
enum class Permission {
    // ── Guest (pre-auth) ──────────────────────────────────────────
    GUEST_FACE_CHECK,                // 1:N "am I in this tenant DB?"

    // ── Profile ───────────────────────────────────────────────────
    PROFILE_READ_SELF,
    PROFILE_UPDATE_SELF,

    // ── Enrollment ────────────────────────────────────────────────
    ENROLL_SELF_CREATE,              // enroll own biometric data
    ENROLL_SELF_UPDATE,              // re-enroll (new face photo)
    ENROLL_SELF_DELETE,              // delete own enrollment (GDPR)
    ENROLL_TENANT_CREATE,            // enroll another user (kiosk/admin)
    ENROLL_TENANT_DELETE,            // remove another user's enrollment

    // ── Verification ──────────────────────────────────────────────
    VERIFY_SELF,                     // main verification flow (1:1)
    IDENTIFY_TENANT,                 // 1:N identify within tenant (admin/security)

    // ── Card Scan ─────────────────────────────────────────────────
    CARD_ADD_SELF,                   // scan & add own ID card
    CARD_ADD_TENANT,                 // scan & add card for another user (admin)

    // ── QR ────────────────────────────────────────────────────────
    QR_DISPLAY,
    QR_SCAN,

    // ── History ───────────────────────────────────────────────────
    HISTORY_READ_SELF,
    HISTORY_READ_TENANT,
    HISTORY_EXPORT_TENANT,

    // ── Tenant Admin ──────────────────────────────────────────────
    TENANT_SETTINGS_READ,
    TENANT_SETTINGS_UPDATE,
    TENANT_USERS_READ,
    TENANT_USERS_WRITE,
    TENANT_ROLES_ASSIGN,
    TENANT_INVITE_CREATE,            // invite users to tenant

    // ── Tenant Membership ─────────────────────────────────────────
    TENANT_INVITE_ACCEPT,            // accept a tenant invitation
    TENANT_MEMBERSHIP_REQUEST,       // request membership to another tenant

    // ── Root (platform scope) ─────────────────────────────────────
    TENANT_MANAGE,                   // create/delete/configure tenants
    PLATFORM_SETTINGS_UPDATE,
    PLATFORM_HEALTH_READ,
    PLATFORM_AUDIT_READ
}

/**
 * Role-to-permission mapping.
 *
 * Role hierarchy (ascending privilege):
 *   GUEST → USER → TENANT_MEMBER → TENANT_ADMIN → ROOT
 *
 * - GUEST: pre-auth face check only
 * - USER: registered but not in any tenant (profile only + accept invites)
 * - TENANT_MEMBER: self-service enrollment, verification, QR, history
 * - TENANT_ADMIN: everything in MEMBER + tenant management + enroll others
 * - ROOT: all permissions (platform-level admin)
 */
object RolePermissions {

    private val guestPermissions = setOf(
        Permission.GUEST_FACE_CHECK
    )

    private val userPermissions = setOf(
        Permission.PROFILE_READ_SELF,
        Permission.PROFILE_UPDATE_SELF,
        Permission.TENANT_INVITE_ACCEPT,
        Permission.TENANT_MEMBERSHIP_REQUEST
    )

    private val tenantMemberPermissions = userPermissions + setOf(
        Permission.ENROLL_SELF_CREATE,
        Permission.ENROLL_SELF_UPDATE,
        Permission.ENROLL_SELF_DELETE,
        Permission.VERIFY_SELF,
        Permission.CARD_ADD_SELF,
        Permission.QR_DISPLAY,
        Permission.QR_SCAN,
        Permission.HISTORY_READ_SELF,
        Permission.TENANT_INVITE_ACCEPT
    )

    private val tenantAdminPermissions = tenantMemberPermissions + setOf(
        Permission.ENROLL_TENANT_CREATE,
        Permission.ENROLL_TENANT_DELETE,
        Permission.CARD_ADD_TENANT,
        Permission.IDENTIFY_TENANT,
        Permission.HISTORY_READ_TENANT,
        Permission.HISTORY_EXPORT_TENANT,
        Permission.TENANT_SETTINGS_READ,
        Permission.TENANT_SETTINGS_UPDATE,
        Permission.TENANT_USERS_READ,
        Permission.TENANT_USERS_WRITE,
        Permission.TENANT_ROLES_ASSIGN,
        Permission.TENANT_INVITE_CREATE
    )

    // ROOT has every permission
    private val rootPermissions = Permission.entries.toSet()

    private val permissionMap: Map<UserRole, Set<Permission>> = mapOf(
        UserRole.GUEST to guestPermissions,
        UserRole.USER to userPermissions,
        UserRole.TENANT_MEMBER to tenantMemberPermissions,
        UserRole.TENANT_ADMIN to tenantAdminPermissions,
        UserRole.ROOT to rootPermissions
    )

    fun hasPermission(role: UserRole, permission: Permission): Boolean {
        return permissionMap[role]?.contains(permission) == true
    }

    fun getPermissions(role: UserRole): Set<Permission> {
        return permissionMap[role] ?: emptySet()
    }
}

/**
 * Extension function for convenient permission checking.
 *
 * Usage: if (userRole.hasPermission(Permission.VERIFY_SELF)) { ... }
 */
fun UserRole.hasPermission(permission: Permission): Boolean {
    return RolePermissions.hasPermission(this, permission)
}
