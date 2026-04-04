package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.AuthSession
import com.fivucsas.shared.test.mocks.FakeSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSessionRepository
    private lateinit var viewModel: SessionViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSessionRepository()
        viewModel = SessionViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(state.sessions.isEmpty())
        assertFalse(state.showRevokeDialog)
    }

    @Test
    fun `loadSessions should populate sessions on success`() = runTest {
        viewModel.loadSessions()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.sessions.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadSessions should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadSessions()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `showRevokeDialog should set dialog state`() {
        val session = AuthSession(id = "sess-1", userId = "user-1")

        viewModel.showRevokeDialog(session)

        assertTrue(viewModel.uiState.value.showRevokeDialog)
        assertEquals(session, viewModel.uiState.value.sessionToRevoke)
    }

    @Test
    fun `hideRevokeDialog should clear dialog state`() {
        val session = AuthSession(id = "sess-1", userId = "user-1")

        viewModel.showRevokeDialog(session)
        viewModel.hideRevokeDialog()

        assertFalse(viewModel.uiState.value.showRevokeDialog)
        assertNull(viewModel.uiState.value.sessionToRevoke)
    }

    @Test
    fun `confirmRevoke should revoke session and refresh`() = runTest {
        val session = AuthSession(id = "sess-1", userId = "user-1")
        viewModel.showRevokeDialog(session)

        viewModel.confirmRevoke()
        advanceUntilIdle()

        assertEquals("sess-1", repository.revokedSessionId)
        assertFalse(viewModel.uiState.value.showRevokeDialog)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `confirmRevoke should set error on failure`() = runTest {
        val session = AuthSession(id = "sess-1", userId = "user-1")
        viewModel.showRevokeDialog(session)
        repository.shouldSucceed = false

        viewModel.confirmRevoke()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `confirmRevoke without session does nothing`() = runTest {
        viewModel.confirmRevoke()
        advanceUntilIdle()

        assertNull(repository.revokedSessionId)
    }

    @Test
    fun `clearMessages should reset all messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
