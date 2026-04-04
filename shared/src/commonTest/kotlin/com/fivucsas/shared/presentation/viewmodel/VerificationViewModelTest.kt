package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.test.mocks.FakeVerificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class VerificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeVerificationRepository
    private lateinit var viewModel: VerificationViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeVerificationRepository()
        viewModel = VerificationViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Initial State ==========

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(state.flows.isEmpty())
        assertTrue(state.sessions.isEmpty())
    }

    // ========== Load Flows ==========

    @Test
    fun `loadFlows should populate flows on success`() = runTest {
        viewModel.loadFlows()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.flows.size)
        assertEquals("Banking KYC", state.flows[0].name)
    }

    @Test
    fun `loadFlows should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadFlows()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ========== Load Sessions ==========

    @Test
    fun `loadSessions should populate sessions on success`() = runTest {
        viewModel.loadSessions()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.sessions.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadSessions with filter should pass status`() = runTest {
        viewModel.loadSessions("pending")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("pending", state.statusFilter)
        assertTrue(state.sessions.all { it.status == "pending" })
    }

    @Test
    fun `loadSessions should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadSessions()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Load Session Detail ==========

    @Test
    fun `loadSessionDetail should set selectedSession`() = runTest {
        viewModel.loadSessionDetail("sess-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.selectedSession)
        assertEquals("sess-1", state.selectedSession?.id)
    }

    @Test
    fun `loadSessionDetail should set error for missing session`() = runTest {
        viewModel.loadSessionDetail("nonexistent")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Start Session ==========

    @Test
    fun `startSession should set selectedSession and success`() = runTest {
        viewModel.startSession("flow-1", "user-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.selectedSession)
        assertNotNull(state.successMessage)
    }

    @Test
    fun `startSession should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.startSession("flow-1", "user-1")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Utility ==========

    @Test
    fun `filterByStatus delegates to loadSessions`() = runTest {
        viewModel.filterByStatus("completed")
        advanceUntilIdle()

        assertEquals("completed", viewModel.uiState.value.statusFilter)
    }

    @Test
    fun `clearSelectedSession should null out session`() {
        viewModel.clearSelectedSession()
        assertNull(viewModel.uiState.value.selectedSession)
    }

    @Test
    fun `clearMessages should reset all messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
