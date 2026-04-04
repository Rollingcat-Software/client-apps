package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.platform.IFileSaver
import com.fivucsas.shared.test.mocks.FakeDashboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

internal class AnalyticsFakeFileSaver : IFileSaver {
    override suspend fun saveTextFile(content: String, suggestedFileName: String, mimeType: String): Result<String> {
        return Result.success("/fake/path/$suggestedFileName")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeDashboardRepository
    private lateinit var viewModel: AnalyticsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeDashboardRepository()
        viewModel = AnalyticsViewModel(repository, AnalyticsFakeFileSaver())
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
    }

    @Test
    fun `loadStatistics should populate statistics on success`() = runTest {
        viewModel.loadStatistics()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.statistics)
        assertEquals(100, state.statistics?.totalUsers)
        assertEquals(85, state.statistics?.activeUsers)
        assertEquals(42, state.statistics?.verificationsToday)
    }

    @Test
    fun `loadStatistics should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadStatistics()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
