package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.test.FakeBiometricRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class LivenessViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeBiometricRepository
    private lateinit var viewModel: LivenessViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBiometricRepository()
        viewModel = LivenessViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isVerifying)
        assertFalse(state.isComplete)
        assertTrue(state.challengeSteps.isEmpty())
        assertEquals(0, state.currentStepIndex)
    }

    // ========== Start Challenge ==========

    @Test
    fun `startChallenge should create 4 random steps`() {
        viewModel.startChallenge()

        val state = viewModel.uiState.value
        assertEquals(4, state.challengeSteps.size)
        assertEquals(0, state.currentStepIndex)
        assertTrue(state.challengeSteps.none { it.completed })
    }

    @Test
    fun `startChallenge steps should have labels`() {
        viewModel.startChallenge()

        val state = viewModel.uiState.value
        assertTrue(state.challengeSteps.all { it.label.isNotBlank() })
    }

    // ========== Complete Step ==========

    @Test
    fun `completeCurrentStep should mark step as completed`() {
        viewModel.startChallenge()

        viewModel.completeCurrentStep()

        val state = viewModel.uiState.value
        assertTrue(state.challengeSteps[0].completed)
        assertEquals(1, state.currentStepIndex)
        assertEquals(1, state.completedSteps)
    }

    @Test
    fun `completeCurrentStep should advance through all steps`() {
        viewModel.startChallenge()

        repeat(4) { viewModel.completeCurrentStep() }

        val state = viewModel.uiState.value
        assertTrue(state.allStepsCompleted)
        assertEquals(4, state.completedSteps)
        assertEquals(1.0f, state.clientScore)
    }

    @Test
    fun `clientScore should track completion ratio`() {
        viewModel.startChallenge()

        viewModel.completeCurrentStep()

        assertEquals(0.25f, viewModel.uiState.value.clientScore)
    }

    // ========== Server Verification ==========

    @Test
    fun `verifyWithServer should set complete state on success`() = runTest {
        viewModel.verifyWithServer(ByteArray(100) { it.toByte() })
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertTrue(state.serverLive)
        assertEquals(0.98f, state.serverScore)
        assertNotNull(state.successMessage)
    }

    @Test
    fun `verifyWithServer should handle failed liveness`() = runTest {
        repository.mockLivenessResult = repository.mockLivenessResult.copy(isLive = false)

        viewModel.verifyWithServer(ByteArray(100) { it.toByte() })
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertFalse(state.serverLive)
    }

    @Test
    fun `verifyWithServer should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.verifyWithServer(ByteArray(100) { it.toByte() })
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isVerifying)
    }

    // ========== Utility ==========

    @Test
    fun `clearMessages should reset messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `reset should return to default state`() {
        viewModel.startChallenge()
        viewModel.completeCurrentStep()

        viewModel.reset()

        val state = viewModel.uiState.value
        assertTrue(state.challengeSteps.isEmpty())
        assertEquals(0, state.currentStepIndex)
        assertFalse(state.isComplete)
    }

    // ========== Computed Properties ==========

    @Test
    fun `currentStep should return correct step`() {
        viewModel.startChallenge()

        assertNotNull(viewModel.uiState.value.currentStep)
        assertEquals(viewModel.uiState.value.challengeSteps[0], viewModel.uiState.value.currentStep)
    }

    @Test
    fun `totalSteps should match challengeSteps size`() {
        viewModel.startChallenge()
        assertEquals(4, viewModel.uiState.value.totalSteps)
    }
}
