package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.NfcApprovalRepository
import com.fivucsas.shared.presentation.state.ApprovalDecision
import com.fivucsas.shared.presentation.state.NfcApprovalUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Drives the NFC push-approval screen.
 *
 * Wire-up:
 * - `onDeepLinkArrived(sessionId)` is called from MainActivity.onNewIntent / onCreate
 *   when a `fivucsas://nfc-session/{sessionId}` deep-link fires, or from the
 *   FCM notification body tap.
 * - `submitDecision(decision)` is called by the Allow / Deny buttons on the
 *   approval screen (or from the notification action buttons, which route back
 *   into the app and re-enter via the deep-link).
 *
 * State transitions:
 *   Idle → AwaitingDecision → Submitting → Approved | Denied | Error
 */
class NfcApprovalViewModel(
    private val repository: NfcApprovalRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow<NfcApprovalUiState>(NfcApprovalUiState.Idle)
    val state: StateFlow<NfcApprovalUiState> = _state.asStateFlow()

    /**
     * Called when a `fivucsas://nfc-session/{sessionId}` URI is opened.
     * Transitions Idle / terminal → AwaitingDecision(sessionId).
     */
    fun onDeepLinkArrived(sessionId: String) {
        if (sessionId.isBlank()) {
            _state.value = NfcApprovalUiState.Error(sessionId = null, message = "Invalid session id")
            return
        }
        _state.value = NfcApprovalUiState.AwaitingDecision(sessionId)
    }

    /**
     * Called by the Allow / Deny buttons. Submits the decision and waits for
     * the backend to acknowledge.
     */
    fun submitDecision(decision: ApprovalDecision) {
        val current = _state.value
        val sessionId = when (current) {
            is NfcApprovalUiState.AwaitingDecision -> current.sessionId
            is NfcApprovalUiState.Error -> current.sessionId
            else -> null
        }
        if (sessionId == null) {
            _state.value = NfcApprovalUiState.Error(
                sessionId = null,
                message = "No pending session"
            )
            return
        }

        _state.value = NfcApprovalUiState.Submitting(sessionId, decision)

        scope.launch {
            repository.submitDecision(sessionId, decision).fold(
                onSuccess = {
                    _state.value = when (decision) {
                        ApprovalDecision.ALLOW -> NfcApprovalUiState.Approved(sessionId)
                        ApprovalDecision.DENY -> NfcApprovalUiState.Denied(sessionId)
                    }
                },
                onFailure = { error ->
                    _state.value = NfcApprovalUiState.Error(
                        sessionId = sessionId,
                        message = ErrorMapper.mapToUserMessage(error, "submit NFC approval decision")
                    )
                }
            )
        }
    }

    fun reset() {
        _state.value = NfcApprovalUiState.Idle
    }

    fun dispose() {
        scope.coroutineContext[Job]?.cancel()
    }
}
