package com.fivucsas.shared.ui.navigation

import com.fivucsas.shared.domain.model.UserRole

sealed class HomeDestination {
    data object UserHome : HomeDestination()
    data object MemberHome : HomeDestination()
    data object TenantAdminHome : HomeDestination()
    data object RootHome : HomeDestination()
}

fun homeDestinationFor(role: UserRole): HomeDestination {
    return when (role) {
        UserRole.USER -> HomeDestination.UserHome
        UserRole.TENANT_MEMBER -> HomeDestination.MemberHome
        UserRole.TENANT_ADMIN -> HomeDestination.TenantAdminHome
        UserRole.ROOT -> HomeDestination.RootHome
        UserRole.GUEST -> HomeDestination.UserHome
    }
}

