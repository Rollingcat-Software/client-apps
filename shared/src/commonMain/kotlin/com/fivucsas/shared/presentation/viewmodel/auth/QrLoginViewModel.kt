package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.domain.model.QrLoginSessionStatus
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.domain.usecase.auth.qr.ApproveQrLoginSessionUseCase
import com.fivucsas.shared.domain.usecase.auth.qr.GetQrLoginSessionUseCase
import com.fivucsas.shared.domain.usecase.auth.qr.StartQrLoginSessionUseCase
import com.fivucsas.shared.presentation.state.QrLoginState
import com.fivucsas.shared.presentation.state.QrLoginStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QrLoginViewModel(
    private val startQrLoginSessionUseCase: StartQrLoginSessionUseCase,
    private val getQrLoginSessionUseCase: GetQrLoginSessionUseCase,
    private val approveQrLoginSessionUseCase: ApproveQrLoginSessionUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(QrLoginState())
    val state: StateFlow<QrLoginState> = _state.asStateFlow()

    private var pollJob: Job? = null

    fun startDesktopSession() {
        pollJob?.cancel()
        _state.value = QrLoginState(isLoading = true)

        scope.launch {
            startQrLoginSessionUseCase(platform = "DESKTOP").fold(
                onSuccess = { session ->
                    _state.value = QrLoginState(
                        isLoading = false,
                        status = mapSessionStatus(session.status),
                        sessionId = session.sessionId,
                        qrPayload = session.qrContent,
                        error = null,
                        role = sessionRole(session),
                        tokens = sessionTokens(session)
                    )
                    if (session.status != QrLoginSessionStatus.APPROVED) {
                        startPolling(session.sessionId)
                    }
                },
                onFailure = { error ->
                    _state.value = QrLoginState(
                        isLoading = false,
                        status = QrLoginStatus.ERROR,
                        error = error.message ?: "Could not start QR login session"
                    )
                }
            )
        }
    }

    fun submitMobileScan(rawPayload: String) {
        val sessionId = extractSessionId(rawPayload)
        if (sessionId == null) {
            _state.value = _state.value.copy(
                status = QrLoginStatus.ERROR,
                error = "Invalid QR payload"
            )
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            approveQrLoginSessionUseCase(
                sessionId = sessionId,
                approverPlatform = "MOBILE"
            ).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        status = QrLoginStatus.APPROVED,
                        sessionId = sessionId,
                        error = null
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        status = QrLoginStatus.ERROR,
                        error = error.message ?: "Could not approve QR session"
                    )
                }
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun dispose() {
        stopPolling()
        scope.coroutineContext[Job]?.cancel()
    }

    private fun startPolling(sessionId: String) {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (true) {
                delay(2000)
                val result = getQrLoginSessionUseCase(sessionId)
                result.fold(
                    onSuccess = { session ->
                        _state.value = _state.value.copy(
                            status = mapSessionStatus(session.status),
                            sessionId = session.sessionId,
                            qrPayload = session.qrContent,
                            error = session.message,
                            role = sessionRole(session),
                            tokens = sessionTokens(session)
                        )

                        if (session.status == QrLoginSessionStatus.APPROVED ||
                            session.status == QrLoginSessionStatus.EXPIRED ||
                            session.status == QrLoginSessionStatus.REJECTED ||
                            session.status == QrLoginSessionStatus.FAILED
                        ) {
                            return@launch
                        }
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(
                            status = QrLoginStatus.ERROR,
                            error = error.message ?: "Could not refresh QR session status"
                        )
                        return@launch
                    }
                )
            }
        }
    }

    private fun mapSessionStatus(status: QrLoginSessionStatus): QrLoginStatus {
        return when (status) {
            QrLoginSessionStatus.PENDING_SCAN -> QrLoginStatus.WAITING_FOR_MOBILE_SCAN
            QrLoginSessionStatus.PENDING_APPROVAL -> QrLoginStatus.WAITING_FOR_DESKTOP_APPROVAL
            QrLoginSessionStatus.APPROVED -> QrLoginStatus.APPROVED
            QrLoginSessionStatus.EXPIRED,
            QrLoginSessionStatus.REJECTED,
            QrLoginSessionStatus.FAILED -> QrLoginStatus.ERROR
        }
    }

    private fun extractSessionId(payload: String): String? {
        val key = "session="
        val idx = payload.indexOf(key)
        if (idx < 0) {
            return payload.trim().takeIf { it.isNotBlank() }
        }
        val value = payload.substring(idx + key.length)
        return value.substringBefore('&').takeIf { it.isNotBlank() }
    }

    private fun sessionRole(session: com.fivucsas.shared.domain.model.QrLoginSession): UserRole? {
        val roleValue = session.role?.takeIf { it.isNotBlank() } ?: return null
        return UserRole.fromString(roleValue)
    }

    private fun sessionTokens(session: com.fivucsas.shared.domain.model.QrLoginSession): AuthTokens? {
        val accessToken = session.accessToken?.takeIf { it.isNotBlank() } ?: return null
        val refreshToken = session.refreshToken?.takeIf { it.isNotBlank() } ?: return null
        val expiresIn = session.expiresIn ?: return null
        val role = session.role?.takeIf { it.isNotBlank() } ?: return null
        return AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = expiresIn,
            role = role
        )
    }
}
