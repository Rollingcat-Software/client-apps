package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.QrLoginSession
import com.fivucsas.shared.domain.model.QrLoginSessionStatus
import com.fivucsas.shared.domain.usecase.auth.qr.ApproveQrLoginSessionUseCase
import com.fivucsas.shared.domain.usecase.auth.qr.GetQrLoginSessionUseCase
import com.fivucsas.shared.domain.usecase.auth.qr.StartQrLoginSessionUseCase
import com.fivucsas.shared.presentation.state.QrLoginStatus
import com.fivucsas.shared.presentation.viewmodel.auth.QrLoginViewModel
import com.fivucsas.shared.test.mocks.FakeQrLoginRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class QrLoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeQrLoginRepository
    private lateinit var viewModel: QrLoginViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeQrLoginRepository()
        viewModel = QrLoginViewModel(
            startQrLoginSessionUseCase = StartQrLoginSessionUseCase(repository),
            getQrLoginSessionUseCase = GetQrLoginSessionUseCase(repository),
            approveQrLoginSessionUseCase = ApproveQrLoginSessionUseCase(repository)
        )
    }

    @AfterTest
    fun tearDown() {
        viewModel.dispose()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(QrLoginStatus.IDLE, state.status)
        assertNull(state.sessionId)
        assertNull(state.qrPayload)
        assertNull(state.error)
    }

    // ========== Start Desktop Session ==========

    @Test
    fun `startDesktopSession should set QR payload on success`() = runTest {
        // Set session as APPROVED to avoid infinite polling
        repository.mockSession = QrLoginSession(
            sessionId = "qr-sess-1",
            qrContent = "fivucsas://qr-login?session=qr-sess-1",
            status = QrLoginSessionStatus.APPROVED,
            accessToken = "token-123",
            refreshToken = "refresh-123",
            expiresIn = 3600,
            role = "USER"
        )

        viewModel.startDesktopSession()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("qr-sess-1", state.sessionId)
        assertEquals(QrLoginStatus.APPROVED, state.status)
        assertNotNull(state.tokens)
    }

    @Test
    fun `startDesktopSession should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.startDesktopSession()
        advanceUntilIdle()

        assertEquals(QrLoginStatus.ERROR, viewModel.state.value.status)
        assertNotNull(viewModel.state.value.error)
    }

    // ========== Mobile Scan ==========

    @Test
    fun `submitMobileScan with valid payload should approve`() = runTest {
        viewModel.submitMobileScan("fivucsas://qr-login?session=qr-sess-1")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(QrLoginStatus.APPROVED, state.status)
        assertEquals("qr-sess-1", state.sessionId)
    }

    @Test
    fun `submitMobileScan with plain sessionId should work`() = runTest {
        viewModel.submitMobileScan("qr-sess-1")
        advanceUntilIdle()

        assertEquals(QrLoginStatus.APPROVED, viewModel.state.value.status)
    }

    @Test
    fun `submitMobileScan with empty payload should error`() {
        viewModel.submitMobileScan("")

        assertEquals(QrLoginStatus.ERROR, viewModel.state.value.status)
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun `submitMobileScan should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.submitMobileScan("qr-sess-1")
        advanceUntilIdle()

        assertEquals(QrLoginStatus.ERROR, viewModel.state.value.status)
    }

    // ========== Utility ==========

    @Test
    fun `clearError should clear error`() {
        viewModel.submitMobileScan("")
        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `stopPolling can be called safely`() {
        viewModel.stopPolling()
        // No exception means success
    }
}
