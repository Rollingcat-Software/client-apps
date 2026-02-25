package com.fivucsas.mobile.android.ui.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission

data class QuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String,
    val anyPermissions: Set<Permission> = emptySet(),
    val allPermissions: Set<Permission> = emptySet()
) {
    fun isAllowed(role: UserRole): Boolean {
        val hasAll = allPermissions.isEmpty() || allPermissions.all { role.hasPermission(it) }
        val hasAny = anyPermissions.isEmpty() || anyPermissions.any { role.hasPermission(it) }
        return hasAll && hasAny
    }
}

