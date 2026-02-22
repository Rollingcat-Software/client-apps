package com.fivucsas.shared.domain.model

enum class Permission {
    VIEW_DASHBOARD,
    VIEW_ADMIN_DASHBOARD,
    MANAGE_USERS,
    MANAGE_ORGANIZATION,
    OPERATE_KIOSK,
    ENROLL_FACE,
    VERIFY_FACE,
    VIEW_OWN_PROFILE,
    VIEW_STATISTICS,
    PLATFORM_SETTINGS
}

object RolePermissions {
    private val permissionMap: Map<UserRole, Set<Permission>> = mapOf(
        UserRole.SUPERADMIN to Permission.entries.toSet(),
        UserRole.ORG_ADMIN to setOf(
            Permission.VIEW_DASHBOARD,
            Permission.VIEW_ADMIN_DASHBOARD,
            Permission.MANAGE_USERS,
            Permission.MANAGE_ORGANIZATION,
            Permission.OPERATE_KIOSK,
            Permission.ENROLL_FACE,
            Permission.VERIFY_FACE,
            Permission.VIEW_OWN_PROFILE,
            Permission.VIEW_STATISTICS
        ),
        UserRole.OPERATOR to setOf(
            Permission.VIEW_DASHBOARD,
            Permission.OPERATE_KIOSK,
            Permission.ENROLL_FACE,
            Permission.VERIFY_FACE,
            Permission.VIEW_OWN_PROFILE,
            Permission.VIEW_STATISTICS
        ),
        UserRole.ENROLLED_USER to setOf(
            Permission.VIEW_DASHBOARD,
            Permission.VERIFY_FACE,
            Permission.VIEW_OWN_PROFILE
        ),
        UserRole.USER to setOf(
            Permission.VIEW_DASHBOARD,
            Permission.ENROLL_FACE,
            Permission.VERIFY_FACE,
            Permission.VIEW_OWN_PROFILE
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
