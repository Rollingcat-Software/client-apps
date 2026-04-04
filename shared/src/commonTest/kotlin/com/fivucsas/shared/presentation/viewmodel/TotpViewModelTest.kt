package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.test.mocks.FakeTotpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class TotpViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTotpRepository
    private lateinit var viewModel: TotpViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTotpRepository()
        viewModel = TotpViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isEnabled)
        assertNull(state.otpAuthUri)
        assertNull(state.secret)
    }

    // ========== Check Status ==========

    @Test
    fun `checkStatus should set isEnabled when TOTP is enabled`() = runTest {
        repository.mockEnabled = true

        viewModel.checkStatus("user-1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEnabled)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `checkStatus should set isEnabled false when disabled`() = runTest {
        repository.mockEnabled = false

        viewModel.checkStatus("user-1")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEnabled)
    }

    @Test
    fun `checkStatus should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.checkStatus("user-1")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Setup ==========

    @Test
    fun `setup should set otpAuthUri and secret on success`() = runTest {
        viewModel.setup("user-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.otpAuthUri)
        assertNotNull(state.secret)
        assertTrue(state.otpAuthUri!!.contains("otpauth://"))
        assertNotNull(state.successMessage)
    }

    @Test
    fun `setup should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.setup("user-1")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Verify Setup ==========

    @Test
    fun `verifySetup should set setupComplete and isEnabled on success`() = runTest {
        viewModel.verifySetup("user-1", "123456")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.setupComplete)
        assertTrue(state.isEnabled)
        assertNotNull(state.successMessage)
    }

    @Test
    fun `verifySetup should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.verifySetup("user-1", "123456")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Utility ==========

    @Test
    fun `clearMessages should reset messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `reset should return to default state`() = runTest {
        viewModel.setup("user-1")
        advanceUntilIdle()

        viewModel.reset()

        val state = viewModel.uiState.value
        assertFalse(state.isEnabled)
        assertNull(state.otpAuthUri)
        assertNull(state.secret)
        assertFalse(state.setupComplete)
    }
}
