package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.presentation.state.StepUpMethod
import com.fivucsas.shared.test.mocks.FakeFingerprintRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class StepUpAuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeFingerprintRepository
    private lateinit var viewModel: StepUpAuthViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeFingerprintRepository()
        viewModel = StepUpAuthViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isVerifying)
        assertFalse(state.isSuccess)
        assertNull(state.selectedMethod)
        assertNull(state.stepUpToken)
        assertNull(state.errorMessage)
        assertEquals("", state.reason)
    }

    @Test
    fun `setReason should update reason`() {
        viewModel.setReason("delete_account")
        assertEquals("delete_account", viewModel.uiState.value.reason)
    }

    @Test
    fun `selectMethod should update selectedMethod and clear error`() {
        viewModel.selectMethod(StepUpMethod.FINGERPRINT)

        assertEquals(StepUpMethod.FINGERPRINT, viewModel.uiState.value.selectedMethod)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `selectMethod can switch methods`() {
        viewModel.selectMethod(StepUpMethod.FINGERPRINT)
        viewModel.selectMethod(StepUpMethod.TOTP)

        assertEquals(StepUpMethod.TOTP, viewModel.uiState.value.selectedMethod)
    }

    // ========== Verify ==========

    @Test
    fun `verify without selected method does nothing`() = runTest {
        viewModel.verify()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isVerifying)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `verify with FINGERPRINT should succeed`() = runTest {
        viewModel.selectMethod(StepUpMethod.FINGERPRINT)

        viewModel.verify()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertNotNull(state.stepUpToken)
        assertEquals("step-up-token-123", state.stepUpToken)
    }

    @Test
    fun `verify with FINGERPRINT should set error on failure`() = runTest {
        repository.shouldSucceed = false
        viewModel.selectMethod(StepUpMethod.FINGERPRINT)

        viewModel.verify()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `verify with FACE should show not yet available`() = runTest {
        viewModel.selectMethod(StepUpMethod.FACE)

        viewModel.verify()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isVerifying)
    }

    @Test
    fun `verify with TOTP should prompt for code`() = runTest {
        viewModel.selectMethod(StepUpMethod.TOTP)

        viewModel.verify()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isVerifying)
    }

    // ========== Reset ==========

    @Test
    fun `reset should clear verification state but keep reason`() = runTest {
        viewModel.setReason("important_action")
        viewModel.selectMethod(StepUpMethod.FINGERPRINT)
        viewModel.verify()
        advanceUntilIdle()

        viewModel.reset()

        val state = viewModel.uiState.value
        assertEquals("important_action", state.reason)
        assertNull(state.selectedMethod)
        assertFalse(state.isSuccess)
        assertNull(state.stepUpToken)
    }
}
