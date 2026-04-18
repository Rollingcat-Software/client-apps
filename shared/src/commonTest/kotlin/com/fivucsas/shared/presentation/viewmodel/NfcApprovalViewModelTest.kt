package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.NfcApprovalRepository
import com.fivucsas.shared.presentation.state.ApprovalDecision
import com.fivucsas.shared.presentation.state.NfcApprovalUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for NfcApprovalViewModel — covers:
 *  - Idle → AwaitingDecision on deep-link arrival
 *  - AwaitingDecision → Submitting → Approved on ALLOW
 *  - AwaitingDecision → Submitting → Denied on DENY
 *  - Error path (repository failure)
 *  - Invalid session id guard
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NfcApprovalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeNfcApprovalRepository
    private lateinit var viewModel: NfcApprovalViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeNfcApprovalRepository()
        viewModel = NfcApprovalViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        viewModel.dispose()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(NfcApprovalUiState.Idle, viewModel.state.value)
    }

    @Test
    fun `onDeepLinkArrived moves state to AwaitingDecision`() {
        viewModel.onDeepLinkArrived("sess-123")

        val current = viewModel.state.value
        assertIs<NfcApprovalUiState.AwaitingDecision>(current)
        assertEquals("sess-123", current.sessionId)
    }

    @Test
    fun `onDeepLinkArrived with blank id yields Error`() {
        viewModel.onDeepLinkArrived("")

        val current = viewModel.state.value
        assertIs<NfcApprovalUiState.Error>(current)
    }

    @Test
    fun `submitDecision allow transitions Submitting then Approved`() = runTest {
        viewModel.onDeepLinkArrived("sess-1")
        viewModel.submitDecision(ApprovalDecision.ALLOW)

        // Before advancing, should be Submitting.
        val midState = viewModel.state.value
        assertIs<NfcApprovalUiState.Submitting>(midState)
        assertEquals(ApprovalDecision.ALLOW, midState.decision)
        assertEquals("sess-1", midState.sessionId)

        advanceUntilIdle()

        val finalState = viewModel.state.value
        assertIs<NfcApprovalUiState.Approved>(finalState)
        assertEquals("sess-1", finalState.sessionId)
        assertEquals(listOf("sess-1" to ApprovalDecision.ALLOW), repository.calls)
    }

    @Test
    fun `submitDecision deny transitions to Denied`() = runTest {
        viewModel.onDeepLinkArrived("sess-2")
        viewModel.submitDecision(ApprovalDecision.DENY)
        advanceUntilIdle()

        val finalState = viewModel.state.value
        assertIs<NfcApprovalUiState.Denied>(finalState)
        assertEquals("sess-2", finalState.sessionId)
        assertEquals(listOf("sess-2" to ApprovalDecision.DENY), repository.calls)
    }

    @Test
    fun `submitDecision surfaces repository failure as Error`() = runTest {
        repository.shouldSucceed = false
        repository.errorMessage = "HTTP 500"
        viewModel.onDeepLinkArrived("sess-3")

        viewModel.submitDecision(ApprovalDecision.ALLOW)
        advanceUntilIdle()

        val finalState = viewModel.state.value
        assertIs<NfcApprovalUiState.Error>(finalState)
        assertEquals("sess-3", finalState.sessionId)
        assertTrue(finalState.message.isNotBlank())
    }

    @Test
    fun `submitDecision from Idle yields Error without calling repository`() = runTest {
        viewModel.submitDecision(ApprovalDecision.ALLOW)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertIs<NfcApprovalUiState.Error>(state)
        assertTrue(repository.calls.isEmpty(), "Repository should not be called from Idle")
    }

    @Test
    fun `reset returns to Idle`() {
        viewModel.onDeepLinkArrived("sess-9")
        viewModel.reset()
        assertEquals(NfcApprovalUiState.Idle, viewModel.state.value)
    }
}

/**
 * Fake repository that records calls and toggles success/failure for tests.
 * Defined here (not in RepositoryMocks.kt) because 20C adds a brand-new
 * repository that other agents' mocks do not know about.
 */
private class FakeNfcApprovalRepository : NfcApprovalRepository {
    var shouldSucceed: Boolean = true
    var errorMessage: String = "Test error"
    val calls: MutableList<Pair<String, ApprovalDecision>> = mutableListOf()

    override suspend fun submitDecision(
        sessionId: String,
        decision: ApprovalDecision
    ): Result<Unit> {
        calls.add(sessionId to decision)
        return if (shouldSucceed) Result.success(Unit)
        else Result.failure(RuntimeException(errorMessage))
    }
}
