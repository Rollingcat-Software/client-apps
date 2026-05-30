package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.AccountLinkingRepository
import com.fivucsas.shared.presentation.state.AccountLinkingUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the linked-accounts section + workspace switcher — the mobile
 * port of the web `useLinkedAccounts` + `useAccountSwitcher` hooks. Both
 * read the single `GET /identity/me` person view.
 *
 * <p>The switcher changes WHO you are (a different membership / role /
 * tenant); it is deliberately distinct from any SUPER_ADMIN data-scoping
 * switcher. After a successful switch the new login-shaped tokens are
 * persisted in the repository and `switchSucceeded` flips so the UI can
 * re-navigate to the post-login home.</p>
 */
class AccountLinkingViewModel(
    private val repository: AccountLinkingRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(AccountLinkingUiState())
    val uiState: StateFlow<AccountLinkingUiState> = _uiState.asStateFlow()

    fun load() {
        scope.launch { loadInternal() }
    }

    private suspend fun loadInternal() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.getIdentityMe().fold(
            onSuccess = { identity ->
                _uiState.update { it.copy(isLoading = false, identity = identity) }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ErrorMapper.mapToUserMessage(error, "load linked accounts")
                    )
                }
            }
        )
    }

    // ── Link dialog ────────────────────────────────────────────────

    fun showLinkDialog() {
        _uiState.update {
            it.copy(showLinkDialog = true, linkOtpSent = false, linkError = null)
        }
    }

    fun hideLinkDialog() {
        _uiState.update {
            it.copy(showLinkDialog = false, linkOtpSent = false, linkError = null, linkInProgress = false)
        }
    }

    /** Step 1 — send an OTP to the target email. */
    fun initiateLink(email: String) {
        scope.launch {
            _uiState.update { it.copy(linkInProgress = true, linkError = null) }
            repository.initiateLink(email).fold(
                onSuccess = {
                    _uiState.update { it.copy(linkInProgress = false, linkOtpSent = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            linkInProgress = false,
                            linkError = ErrorMapper.mapToUserMessage(error, "send link code")
                        )
                    }
                }
            )
        }
    }

    /** Step 2 — verify OTP + step-up password, then link + refetch. */
    fun confirmLink(email: String, otp: String, password: String) {
        scope.launch {
            _uiState.update { it.copy(linkInProgress = true, linkError = null) }
            repository.confirmLink(email, otp, password).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(linkInProgress = false, showLinkDialog = false, linkOtpSent = false)
                    }
                    loadInternal()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            linkInProgress = false,
                            linkError = ErrorMapper.mapToUserMessage(error, "confirm link")
                        )
                    }
                }
            )
        }
    }

    // ── Unlink ─────────────────────────────────────────────────────

    fun unlink(membershipUserId: String) {
        scope.launch {
            _uiState.update { it.copy(unlinkingUserId = membershipUserId, errorMessage = null) }
            repository.unlink(membershipUserId).fold(
                onSuccess = {
                    _uiState.update { it.copy(unlinkingUserId = null) }
                    loadInternal()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            unlinkingUserId = null,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "unlink account")
                        )
                    }
                }
            )
        }
    }

    // ── Switch membership ──────────────────────────────────────────

    fun switchMembership(targetUserId: String) {
        scope.launch {
            _uiState.update { it.copy(switchingUserId = targetUserId, errorMessage = null) }
            repository.switchMembership(targetUserId).fold(
                onSuccess = {
                    _uiState.update { it.copy(switchingUserId = null, switchSucceeded = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            switchingUserId = null,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "switch membership")
                        )
                    }
                }
            )
        }
    }

    /** Consume the one-shot switch-success signal after the UI re-navigates. */
    fun consumeSwitchSucceeded() {
        _uiState.update { it.copy(switchSucceeded = false) }
    }
}
