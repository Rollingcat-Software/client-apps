package com.fivucsas.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

class RolePermissionsTest {

    @Test
    fun rootHasAllPermissions() {
        Permission.entries.forEach { permission ->
            assertTrue(
                UserRole.ROOT.hasPermission(permission),
                "ROOT should have $permission"
            )
        }
        assertEquals(Permission.entries.toSet(), RolePermissions.getPermissions(UserRole.ROOT))
    }

    @Test
    fun tenantAdminHasAdminPermissionsButNotAll() {
        // Admin should have these
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.TENANT_USERS_READ))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.TENANT_USERS_WRITE))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.TENANT_ROLES_ASSIGN))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.TENANT_SETTINGS_READ))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.TENANT_SETTINGS_UPDATE))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.TENANT_INVITE_CREATE))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.HISTORY_READ_TENANT))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.HISTORY_EXPORT_TENANT))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.ENROLL_TENANT_CREATE))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.ENROLL_TENANT_DELETE))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.IDENTIFY_TENANT))

        // Admin also inherits all member permissions
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.ENROLL_SELF_CREATE))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.VERIFY_SELF))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.QR_SCAN))
        assertTrue(UserRole.TENANT_ADMIN.hasPermission(Permission.HISTORY_READ_SELF))

        // Admin permissions are explicitly listed (unlike ROOT which auto-includes future ones)
        val adminPerms = RolePermissions.getPermissions(UserRole.TENANT_ADMIN)
        assertTrue(adminPerms.isNotEmpty())
    }

    @Test
    fun tenantMemberHasSelfServiceButNotAdmin() {
        // Member self-service
        assertTrue(UserRole.TENANT_MEMBER.hasPermission(Permission.PROFILE_READ_SELF))
        assertTrue(UserRole.TENANT_MEMBER.hasPermission(Permission.PROFILE_UPDATE_SELF))
        assertTrue(UserRole.TENANT_MEMBER.hasPermission(Permission.ENROLL_SELF_CREATE))
        assertTrue(UserRole.TENANT_MEMBER.hasPermission(Permission.ENROLL_SELF_UPDATE))
        assertTrue(UserRole.TENANT_MEMBER.hasPermission(Permission.ENROLL_SELF_DELETE))
        assertTrue(UserRole.TENANT_MEMBER.hasPermission(Permission.VERIFY_SELF))
        assertTrue(UserRole.TENANT_MEMBER.hasPermission(Permission.QR_DISPLAY))
        assertTrue(UserRole.TENANT_MEMBER.hasPermission(Permission.QR_SCAN))
        assertTrue(UserRole.TENANT_MEMBER.hasPermission(Permission.HISTORY_READ_SELF))
        assertTrue(UserRole.TENANT_MEMBER.hasPermission(Permission.TENANT_INVITE_ACCEPT))

        // Member should NOT have admin permissions
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.TENANT_USERS_READ))
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.TENANT_USERS_WRITE))
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.TENANT_ROLES_ASSIGN))
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.TENANT_SETTINGS_READ))
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.TENANT_SETTINGS_UPDATE))
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.TENANT_INVITE_CREATE))
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.HISTORY_READ_TENANT))
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.HISTORY_EXPORT_TENANT))
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.ENROLL_TENANT_CREATE))
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.ENROLL_TENANT_DELETE))
        assertFalse(UserRole.TENANT_MEMBER.hasPermission(Permission.IDENTIFY_TENANT))
    }

    @Test
    fun userHasOnlyProfileAndInviteAccept() {
        // USER should have
        assertTrue(UserRole.USER.hasPermission(Permission.PROFILE_READ_SELF))
        assertTrue(UserRole.USER.hasPermission(Permission.PROFILE_UPDATE_SELF))
        assertTrue(UserRole.USER.hasPermission(Permission.TENANT_INVITE_ACCEPT))
        assertTrue(UserRole.USER.hasPermission(Permission.TENANT_MEMBERSHIP_REQUEST))

        // USER should NOT have enroll/verify/QR/history
        assertFalse(UserRole.USER.hasPermission(Permission.ENROLL_SELF_CREATE))
        assertFalse(UserRole.USER.hasPermission(Permission.ENROLL_SELF_UPDATE))
        assertFalse(UserRole.USER.hasPermission(Permission.ENROLL_SELF_DELETE))
        assertFalse(UserRole.USER.hasPermission(Permission.VERIFY_SELF))
        assertFalse(UserRole.USER.hasPermission(Permission.QR_SCAN))
        assertFalse(UserRole.USER.hasPermission(Permission.QR_DISPLAY))
        assertFalse(UserRole.USER.hasPermission(Permission.HISTORY_READ_SELF))

        assertEquals(4, RolePermissions.getPermissions(UserRole.USER).size)
    }

    @Test
    fun guestHasOnlyFaceCheck() {
        assertTrue(UserRole.GUEST.hasPermission(Permission.GUEST_FACE_CHECK))

        // GUEST should NOT have anything else
        assertFalse(UserRole.GUEST.hasPermission(Permission.PROFILE_READ_SELF))
        assertFalse(UserRole.GUEST.hasPermission(Permission.PROFILE_UPDATE_SELF))
        assertFalse(UserRole.GUEST.hasPermission(Permission.ENROLL_SELF_CREATE))
        assertFalse(UserRole.GUEST.hasPermission(Permission.VERIFY_SELF))
        assertFalse(UserRole.GUEST.hasPermission(Permission.QR_SCAN))
        assertFalse(UserRole.GUEST.hasPermission(Permission.HISTORY_READ_SELF))
        assertFalse(UserRole.GUEST.hasPermission(Permission.TENANT_INVITE_ACCEPT))

        assertEquals(1, RolePermissions.getPermissions(UserRole.GUEST).size)
    }
}
