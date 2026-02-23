package com.fivucsas.shared.domain.model

enum class Permission {
    // Profile
    PROFILE_READ_SELF,
    PROFILE_UPDATE_SELF,

    // Enrollment – self-service
    ENROLL_SELF_CREATE,
    ENROLL_SELF_UPDATE,
    ENROLL_SELF_DELETE,

    // Enrollment – tenant-wide (admin)
    ENROLL_TENANT_CREATE,
    ENROLL_TENANT_DELETE,

    // Verification / identification
    VERIFY_SELF,
    IDENTIFY_TENANT,

    // QR
    QR_DISPLAY,
    QR_SCAN,

    // History
    HISTORY_READ_SELF,
    HISTORY_READ_TENANT,
    HISTORY_EXPORT_TENANT,

    // Tenant settings
    TENANT_SETTINGS_READ,
    TENANT_SETTINGS_UPDATE,

    // Tenant user management
    TENANT_USERS_READ,
    TENANT_USERS_WRITE,
    TENANT_ROLES_ASSIGN,

    // Invites
    TENANT_INVITE_CREATE,
    TENANT_INVITE_ACCEPT,

    // Dashboards
    VIEW_DASHBOARD,
    VIEW_ADMIN_DASHBOARD
}

object RolePermissions {

    private val memberPermissions = setOf(
        Permission.PROFILE_READ_SELF,
        Permission.PROFILE_UPDATE_SELF,
        Permission.ENROLL_SELF_CREATE,
        Permission.ENROLL_SELF_UPDATE,
        Permission.ENROLL_SELF_DELETE,
        Permission.VERIFY_SELF,
        Permission.QR_DISPLAY,
        Permission.QR_SCAN,
        Permission.HISTORY_READ_SELF,
        Permission.TENANT_INVITE_ACCEPT,
        Permission.VIEW_DASHBOARD
    )

    private val adminPermissions = memberPermissions + setOf(
        Permission.ENROLL_TENANT_CREATE,
        Permission.ENROLL_TENANT_DELETE,
        Permission.IDENTIFY_TENANT,
        Permission.HISTORY_READ_TENANT,
        Permission.HISTORY_EXPORT_TENANT,
        Permission.TENANT_SETTINGS_READ,
        Permission.TENANT_SETTINGS_UPDATE,
        Permission.TENANT_USERS_READ,
        Permission.TENANT_USERS_WRITE,
        Permission.TENANT_ROLES_ASSIGN,
        Permission.TENANT_INVITE_CREATE,
        Permission.VIEW_ADMIN_DASHBOARD
    )

    private val permissionMap: Map<UserRole, Set<Permission>> = mapOf(
        UserRole.ROOT to Permission.entries.toSet(),
        UserRole.TENANT_ADMIN to adminPermissions,
        UserRole.TENANT_MEMBER to memberPermissions,
        UserRole.USER to setOf(
            Permission.PROFILE_READ_SELF,
            Permission.PROFILE_UPDATE_SELF,
            Permission.TENANT_INVITE_ACCEPT,
            Permission.VIEW_DASHBOARD
        ),
        UserRole.GUEST to setOf(
            Permission.PROFILE_READ_SELF,
            Permission.VIEW_DASHBOARD
        )
    )

    fun hasPermission(role: UserRole, permission: Permission): Boolean {
        return permissionMap[role]?.contains(permission) == true
    }

    fun getPermissions(role: UserRole): Set<Permission> {
        return permissionMap[role] ?: emptySet()
    }
}

fun UserRole.hasPermission(permission: Permission): Boolean {
    return RolePermissions.hasPermission(this, permission)
}
