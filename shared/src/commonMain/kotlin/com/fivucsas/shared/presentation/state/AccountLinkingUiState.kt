package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.IdentityMe
import com.fivucsas.shared.domain.model.IdentityMembership

/**
 * UI state for the linked-accounts section + the workspace (account)
 * switcher — both driven off the single `GET /identity/me` person view,
 * mirroring the web `useLinkedAccounts` / `useAccountSwitcher` hooks.
 */
data class AccountLinkingUiState(
    val isLoading: Boolean = true,
    val identity: IdentityMe? = null,
    val errorMessage: String? = null,

    // Link dialog (initiate → OTP sent → confirm).
    val showLinkDialog: Boolean = false,
    val linkOtpSent: Boolean = false,
    val linkInProgress: Boolean = false,
    val linkError: String? = null,

    // Unlink + switch in-flight markers.
    val unlinkingUserId: String? = null,
    val switchingUserId: String? = null,
    val switchSucceeded: Boolean = false
) {
    val memberships: List<IdentityMembership> get() = identity?.memberships ?: emptyList()

    /** Show the switcher only when the person actually has >1 membership. */
    val canSwitch: Boolean get() = memberships.size > 1
}
