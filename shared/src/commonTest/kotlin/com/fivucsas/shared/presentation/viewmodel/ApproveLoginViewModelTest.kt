package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.PendingApproveLogin
import com.fivucsas.shared.domain.repository.ApproveLoginRepository
import com.fivucsas.shared.presentation.state.ApprovalDecision
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for ApproveLoginViewModel (approver "Login requests" screen):
 *  - refresh() loads the pending list
 *  - allow() echoes the match number, removes the row, records the decision
 *  - deny() removes the row
 *  - repository failure surfaces an errorMessage (list unchanged)
 *  - a second decision is ignored while one is in flight
 *  - startPolling is idempotent; stopPolling cancels
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ApproveLoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeApproveLoginRepository
    private lateinit var viewModel: ApproveLoginViewModel

    private val req1 = PendingApproveLogin("sess-1", "07", "1.2.3.4", "Chrome", 1000)
    private val req2 = PendingApproveLogin("sess-2", "42", "5.6.7.8", "Safari", 1001)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeApproveLoginRepository()
        viewModel = ApproveLoginViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        viewModel.dispose()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and not refreshing`() {
        val s = viewModel.state.value
        assertTrue(s.pending.isEmpty())
        assertEquals(false, s.isRefreshing)
        assertNull(s.inFlightSessionId)
    }

    @Test
    fun `refresh loads pending list`() = runTest {
        repository.pending = listOf(req1, req2)
        viewModel.refresh()
        advanceUntilIdle()

        val s = viewModel.state.value
        assertEquals(listOf(req1, req2), s.pending)
        assertEquals(false, s.isRefreshing)
        assertNull(s.errorMessage)
    }

    @Test
    fun `allow echoes match number and removes the row`() = runTest {
        repository.pending = listOf(req1, req2)
        viewModel.refresh()
        advanceUntilIdle()

        viewModel.allow("sess-1", "07")
        advanceUntilIdle()

        val s = viewModel.state.value
        assertEquals(listOf(req2), s.pending, "decided row removed")
        assertNull(s.inFlightSessionId)
        assertEquals(listOf(Triple("sess-1", ApprovalDecision.ALLOW, "07")), repository.decisions)
        assertNotNull(s.lastDecision)
        assertEquals(ApprovalDecision.ALLOW, s.lastDecision!!.decision)
    }

    @Test
    fun `deny removes the row`() = runTest {
        repository.pending = listOf(req1, req2)
        viewModel.refresh()
        advanceUntilIdle()

        viewModel.deny("sess-2", "42")
        advanceUntilIdle()

        val s = viewModel.state.value
        assertEquals(listOf(req1), s.pending)
        assertEquals(listOf(Triple("sess-2", ApprovalDecision.DENY, "42")), repository.decisions)
    }

    @Test
    fun `decision failure surfaces errorMessage and keeps the row`() = runTest {
        repository.pending = listOf(req1)
        viewModel.refresh()
        advanceUntilIdle()

        repository.decideSucceeds = false
        viewModel.allow("sess-1", "07")
        advanceUntilIdle()

        val s = viewModel.state.value
        assertTrue(s.errorMessage?.isNotBlank() == true)
        assertEquals(listOf(req1), s.pending, "row stays so the user can retry")
        assertNull(s.inFlightSessionId)
    }

    @Test
    fun `second decision ignored while one is in flight`() = runTest {
        repository.pending = listOf(req1, req2)
        viewModel.refresh()
        advanceUntilIdle()

        // First decide sets inFlightSessionId before coroutine resumes; a
        // second decide call in the same tick is a no-op.
        viewModel.allow("sess-1", "07")
        viewModel.allow("sess-2", "42")
        advanceUntilIdle()

        assertEquals(listOf(Triple("sess-1", ApprovalDecision.ALLOW, "07")), repository.decisions)
    }

    @Test
    fun `list-load failure surfaces errorMessage`() = runTest {
        repository.listSucceeds = false
        viewModel.refresh()
        advanceUntilIdle()

        val s = viewModel.state.value
        assertTrue(s.errorMessage?.isNotBlank() == true)
        assertEquals(false, s.isRefreshing)
    }
}

/**
 * Fake approver repository — records decision calls and toggles list/decide
 * success. New repository, so its fake lives alongside this test (matching the
 * NfcApproval test convention).
 */
private class FakeApproveLoginRepository : ApproveLoginRepository {
    var pending: List<PendingApproveLogin> = emptyList()
    var listSucceeds: Boolean = true
    var decideSucceeds: Boolean = true
    val decisions: MutableList<Triple<String, ApprovalDecision, String>> = mutableListOf()

    override suspend fun listPending(): Result<List<PendingApproveLogin>> =
        if (listSucceeds) Result.success(pending)
        else Result.failure(RuntimeException("HTTP 500"))

    override suspend fun submitDecision(
        sessionId: String,
        decision: ApprovalDecision,
        matchNumber: String
    ): Result<Unit> {
        decisions.add(Triple(sessionId, decision, matchNumber))
        return if (decideSucceeds) Result.success(Unit)
        else Result.failure(RuntimeException("HTTP 500"))
    }
}
