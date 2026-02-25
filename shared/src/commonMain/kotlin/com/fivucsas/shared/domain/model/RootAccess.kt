package com.fivucsas.shared.domain.model

enum class RootPermission {
    TENANT_READ,
    TENANT_CREATE,
    TENANT_UPDATE,
    TENANT_DELETE,
    USER_READ,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
    ROLE_READ,
    ROLE_UPDATE,
    AUDIT_READ,
    AUDIT_EXPORT,
    SECURITY_EVENT_READ,
    SETTINGS_READ,
    SETTINGS_UPDATE
}

data class Capabilities(
    val permissions: Set<RootPermission>,
    val tenantScoped: Boolean,
    val canImpersonate: Boolean
) {
    fun can(permission: RootPermission): Boolean = permissions.contains(permission)
}

object CapabilityPolicy {
    val rootCapabilities = Capabilities(
        permissions = RootPermission.entries.toSet(),
        tenantScoped = false,
        canImpersonate = true
    )

    val tenantAdminCapabilities = Capabilities(
        permissions = setOf(
            RootPermission.USER_READ,
            RootPermission.USER_CREATE,
            RootPermission.USER_UPDATE,
            RootPermission.USER_DELETE,
            RootPermission.ROLE_READ,
            RootPermission.ROLE_UPDATE,
            RootPermission.AUDIT_READ,
            RootPermission.AUDIT_EXPORT,
            RootPermission.SECURITY_EVENT_READ,
            RootPermission.SETTINGS_READ,
            RootPermission.SETTINGS_UPDATE
        ),
        tenantScoped = true,
        canImpersonate = false
    )

    val tenantMemberCapabilities = Capabilities(
        permissions = setOf(
            RootPermission.USER_READ
        ),
        tenantScoped = true,
        canImpersonate = false
    )

    val userCapabilities = Capabilities(
        permissions = emptySet(),
        tenantScoped = true,
        canImpersonate = false
    )

    fun fromRole(role: UserRole): Capabilities {
        return when (role) {
            UserRole.ROOT -> rootCapabilities
            UserRole.TENANT_ADMIN -> tenantAdminCapabilities
            UserRole.TENANT_MEMBER -> tenantMemberCapabilities
            UserRole.USER, UserRole.GUEST -> userCapabilities
        }
    }
}
