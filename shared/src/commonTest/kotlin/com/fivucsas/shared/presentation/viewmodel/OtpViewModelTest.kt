package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.test.mocks.FakeOtpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class OtpViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeOtpRepository
    private lateinit var viewModel: OtpViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeOtpRepository()
        viewModel = OtpViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.otpSent)
        assertFalse(state.otpVerified)
        assertNull(state.errorMessage)
    }

    // ========== Email OTP ==========

    @Test
    fun `sendEmailOtp should set otpSent on success`() = runTest {
        viewModel.sendEmailOtp("user-1", "test@test.com")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.otpSent)
        assertNotNull(state.successMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `sendEmailOtp should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.sendEmailOtp("user-1", "test@test.com")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `verifyEmailOtp should set otpVerified on success`() = runTest {
        viewModel.verifyEmailOtp("user-1", "123456")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.otpVerified)
    }

    @Test
    fun `verifyEmailOtp should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.verifyEmailOtp("user-1", "123456")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== SMS OTP ==========

    @Test
    fun `sendSmsOtp should set otpSent on success`() = runTest {
        viewModel.sendSmsOtp("user-1", "+1234567890")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.otpSent)
    }

    @Test
    fun `sendSmsOtp should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.sendSmsOtp("user-1", "+1234567890")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `verifySmsOtp should set otpVerified on success`() = runTest {
        viewModel.verifySmsOtp("user-1", "654321")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.otpVerified)
    }

    // ========== Utility ==========

    @Test
    fun `clearMessages should reset all messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `reset should return to default state`() = runTest {
        viewModel.sendEmailOtp("user-1", "test@test.com")
        advanceUntilIdle()

        viewModel.reset()

        val state = viewModel.uiState.value
        assertFalse(state.otpSent)
        assertFalse(state.otpVerified)
        assertNull(state.errorMessage)
    }
}
