package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.AuthFlow
import com.fivucsas.shared.domain.repository.AuthFlowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthFlowViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthFlowRepository
    private lateinit var viewModel: AuthFlowViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthFlowRepository()
        viewModel = AuthFlowViewModel(repository)
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
    }

    // ========== Load Auth Flows ==========

    @Test
    fun `loadAuthFlows should populate flows on success`() = runTest {
        viewModel.loadAuthFlows("tenant-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.flows.size)
        assertEquals("Login Flow", state.flows[0].name)
        assertEquals("Door Access", state.flows[1].name)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadAuthFlows should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadAuthFlows("tenant-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertTrue(state.flows.isEmpty())
    }

    // ========== Clear Error ==========

    @Test
    fun `clearError should reset error message`() = runTest {
        repository.shouldSucceed = false
        viewModel.loadAuthFlows("tenant-1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearError on default state is safe`() {
        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}

// ── Fake ────────────────────────────────────────────────────────────────────

private class FakeAuthFlowRepository : AuthFlowRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    var mockFlows = listOf(
        AuthFlow(
            id = "flow-1",
            tenantId = "tenant-1",
            name = "Login Flow",
            operationType = "APP_LOGIN",
            isDefault = true,
            stepCount = 2
        ),
        AuthFlow(
            id = "flow-2",
            tenantId = "tenant-1",
            name = "Door Access",
            operationType = "DOOR_ACCESS",
            isDefault = false,
            stepCount = 1
        )
    )

    override suspend fun getAuthFlows(tenantId: String): Result<List<AuthFlow>> {
        return if (shouldSucceed) Result.success(mockFlows)
        else Result.failure(RuntimeException(errorMessage))
    }
}
