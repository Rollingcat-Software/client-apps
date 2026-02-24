package com.fivucsas.mobile.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission

fun canAccess(userRole: UserRole, permission: Permission): Boolean =
    userRole.hasPermission(permission)

fun canAccessAny(userRole: UserRole, vararg permissions: Permission): Boolean =
    permissions.any { userRole.hasPermission(it) }

@Composable
fun RequirePermission(
    userRole: UserRole,
    permission: Permission,
    onDeniedNavigate: () -> Unit,
    content: @Composable () -> Unit
) {
    if (canAccess(userRole, permission)) {
        content()
    } else {
        LaunchedEffect(userRole, permission) {
            onDeniedNavigate()
        }
    }
}

