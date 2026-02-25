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
        return when (role) {
            UserRole.ROOT, UserRole.TENANT_ADMIN -> RouteIds.ADMIN_DASHBOARD
            else -> RouteIds.DASHBOARD
        }
    }

    fun postQrApprovalRoute(role: UserRole): String {
        return if (canAccess(role, Permission.TENANT_USERS_READ)) {
            RouteIds.ADMIN_DASHBOARD
        } else {
            RouteIds.DASHBOARD
        }
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
            else -> true
        }
    }

    private fun canAccess(role: UserRole, permission: Permission): Boolean {
        return role.hasPermission(permission)
    }

    private fun canAccessAny(role: UserRole, permissions: Set<Permission>): Boolean {
        return permissions.any { role.hasPermission(it) }
    }
}

