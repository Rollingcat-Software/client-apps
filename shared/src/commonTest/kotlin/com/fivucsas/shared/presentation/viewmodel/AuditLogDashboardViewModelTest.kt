package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.platform.IFileSaver
import com.fivucsas.shared.test.mocks.FakeAuditLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

internal class AuditLogFakeFileSaver : IFileSaver {
    override suspend fun saveTextFile(content: String, suggestedFileName: String, mimeType: String): Result<String> {
        return Result.success("/fake/path/$suggestedFileName")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuditLogDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuditLogRepository
    private lateinit var viewModel: AuditLogDashboardViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuditLogRepository()
        viewModel = AuditLogDashboardViewModel(repository, AuditLogFakeFileSaver())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.logs.isEmpty())
        assertEquals("", state.filterAction)
        assertEquals("", state.filterUserId)
        assertEquals(0, state.currentPage)
    }

    // ========== Load Logs ==========

    @Test
    fun `loadLogs should populate logs on success`() = runTest {
        viewModel.loadLogs()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(20, state.logs.size) // PAGE_SIZE = 20
        assertTrue(state.hasMorePages)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadLogs should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadLogs()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Load More ==========

    @Test
    fun `loadMore should append logs`() = runTest {
        viewModel.loadLogs()
        advanceUntilIdle()

        val firstPageSize = viewModel.uiState.value.logs.size

        viewModel.loadMore()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.logs.size >= firstPageSize)
        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `loadMore should not load when no more pages`() = runTest {
        // Set mock to return fewer than PAGE_SIZE so hasMorePages = false
        repository.mockLogs = (1..5).map { i ->
            com.fivucsas.shared.domain.model.AuditLog(id = "log-$i", action = "LOGIN")
        }

        viewModel.loadLogs()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasMorePages)

        viewModel.loadMore()
        advanceUntilIdle()

        // Page should not have changed
        assertEquals(0, viewModel.uiState.value.currentPage)
    }

    // ========== Filters ==========

    @Test
    fun `updateFilterAction should update state`() {
        viewModel.updateFilterAction("LOGIN")
        assertEquals("LOGIN", viewModel.uiState.value.filterAction)
    }

    @Test
    fun `updateFilterUserId should update state`() {
        viewModel.updateFilterUserId("user-1")
        assertEquals("user-1", viewModel.uiState.value.filterUserId)
    }

    @Test
    fun `applyFilters should reload logs`() = runTest {
        viewModel.updateFilterAction("LOGIN")
        viewModel.applyFilters()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        // All mock logs have action = "LOGIN" so all should be returned
        assertTrue(viewModel.uiState.value.logs.isNotEmpty())
    }

    @Test
    fun `clearFilters should reset filters and reload`() = runTest {
        viewModel.updateFilterAction("LOGIN")
        viewModel.updateFilterUserId("user-1")

        viewModel.clearFilters()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.filterAction)
        assertEquals("", viewModel.uiState.value.filterUserId)
    }
}
