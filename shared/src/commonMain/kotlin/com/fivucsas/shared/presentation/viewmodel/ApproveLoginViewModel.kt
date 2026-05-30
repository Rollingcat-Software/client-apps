package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.ApproveLoginRepository
import com.fivucsas.shared.presentation.state.ApprovalDecision
import com.fivucsas.shared.presentation.state.ApproveLoginUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Drives the approver's "Login requests" screen for the no-Firebase
 * number-matching approve-login flow.
 *
 * No push provider: the screen polls `GET /api/v1/auth/approve-login/pending`
 * on an interval while visible. For each pending request the approver taps
 * Allow (echoing the displayed two-digit match number) or Deny.
 *
 * Wire-up:
 * - [startPolling] from the screen's onResume / LaunchedEffect; [stopPolling]
 *   from onPause.
 * - [refresh] for pull-to-refresh.
 * - [allow] / [deny] from the per-row buttons.
 *
 * The approver must already be authenticated (the endpoint is behind the JWT
 * filter); this VM does not handle login.
 */
class ApproveLoginViewModel(
    private val repository: ApproveLoginRepository,
    private val pollIntervalMillis: Long = DEFAULT_POLL_INTERVAL_MILLIS
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(ApproveLoginUiState())
    val state: StateFlow<ApproveLoginUiState> = _state.asStateFlow()

    private var pollJob: Job? = null

    /** Begins (or restarts) polling the pending list. Idempotent. */
    fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = scope.launch {
            while (isActive) {
                loadPending()
                delay(pollIntervalMillis)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    /** One-shot refresh (pull-to-refresh). */
    fun refresh() {
        scope.launch { loadPending() }
    }

    private suspend fun loadPending() {
        _state.update { it.copy(isRefreshing = true) }
        repository.listPending().fold(
            onSuccess = { list ->
                _state.update {
                    it.copy(pending = list, isRefreshing = false, errorMessage = null)
                }
            },
            onFailure = { error ->
                _state.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = ErrorMapper.mapToUserMessage(error, "load login requests")
                    )
                }
            }
        )
    }

    /** Approve a request, echoing the match number shown for it. */
    fun allow(sessionId: String, matchNumber: String) {
        decide(sessionId, ApprovalDecision.ALLOW, matchNumber)
    }

    /** Deny a request. */
    fun deny(sessionId: String, matchNumber: String) {
        decide(sessionId, ApprovalDecision.DENY, matchNumber)
    }

    private fun decide(sessionId: String, decision: ApprovalDecision, matchNumber: String) {
        if (_state.value.inFlightSessionId != null) return // one decision at a time
        _state.update { it.copy(inFlightSessionId = sessionId, errorMessage = null) }

        scope.launch {
            repository.submitDecision(sessionId, decision, matchNumber).fold(
                onSuccess = {
                    _state.update { current ->
                        current.copy(
                            // Drop the decided request from the list immediately.
                            pending = current.pending.filterNot { it.sessionId == sessionId },
                            inFlightSessionId = null,
                            lastDecision = ApproveLoginUiState.DecisionResult(sessionId, decision)
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            inFlightSessionId = null,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "submit login decision")
                        )
                    }
                }
            )
        }
    }

    /** Clears the transient decision/error banners after they've been shown. */
    fun consumeTransient() {
        _state.update { it.copy(lastDecision = null, errorMessage = null) }
    }

    fun dispose() {
        scope.coroutineContext[Job]?.cancel()
    }

    companion object {
        const val DEFAULT_POLL_INTERVAL_MILLIS: Long = 3_000
    }
}
