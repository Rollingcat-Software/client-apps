package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.FingerprintRepository
import com.fivucsas.shared.domain.repository.FingerprintStep
import com.fivucsas.shared.platform.FingerprintAuthException
import com.fivucsas.shared.presentation.state.FingerprintUiState
import com.fivucsas.shared.presentation.viewmodel.auth.FingerprintViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FingerprintViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeFingerprintRepository
    private lateinit var viewModel: FingerprintViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeFingerprintRepository()
        viewModel = FingerprintViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Initial State ==========

    @Test
    fun `initial state should be Idle`() {
        assertIs<FingerprintUiState.Idle>(viewModel.state.value)
    }

    // ========== Start Step Up ==========

    @Test
    fun `startStepUp should reach Success on successful flow`() = runTest {
        viewModel.startStepUp()

        val state = viewModel.state.value
        assertIs<FingerprintUiState.Success>(state)
        assertEquals("step-up-token-123", state.stepUpToken)
    }

    @Test
    fun `startStepUp should set Error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.startStepUp()

        val state = viewModel.state.value
        assertIs<FingerprintUiState.Error>(state)
        assertFalse(state.message.isBlank())
    }

    @Test
    fun `startStepUp should set Error with recoverable=false for non-recoverable exception`() = runTest {
        repository.shouldSucceed = false
        repository.throwNonRecoverable = true

        viewModel.startStepUp()

        val state = viewModel.state.value
        assertIs<FingerprintUiState.Error>(state)
        assertFalse(state.recoverable)
    }

    @Test
    fun `startStepUp should report step progress`() = runTest {
        val reportedSteps = mutableListOf<FingerprintStep>()
        repository.onStepCallback = { reportedSteps.add(it) }

        viewModel.startStepUp()

        // The fake reports all 4 steps before returning success
        assertEquals(4, reportedSteps.size)
        assertEquals(FingerprintStep.RegisteringDevice, reportedSteps[0])
        assertEquals(FingerprintStep.RequestingChallenge, reportedSteps[1])
        assertEquals(FingerprintStep.ScanningBiometric, reportedSteps[2])
        assertEquals(FingerprintStep.VerifyingSignature, reportedSteps[3])
    }

    // ========== Reset ==========

    @Test
    fun `reset should restore Idle state`() = runTest {
        viewModel.startStepUp()
        assertIs<FingerprintUiState.Success>(viewModel.state.value)

        viewModel.reset()
        assertIs<FingerprintUiState.Idle>(viewModel.state.value)
    }
}

// ── Fake ────────────────────────────────────────────────────────────────────

private class FakeFingerprintRepository : FingerprintRepository {
    var shouldSucceed = true
    var throwNonRecoverable = false
    var onStepCallback: ((FingerprintStep) -> Unit)? = null

    override suspend fun performStepUp(onStep: (FingerprintStep) -> Unit): Result<String> {
        onStep(FingerprintStep.RegisteringDevice)
        onStepCallback?.invoke(FingerprintStep.RegisteringDevice)

        onStep(FingerprintStep.RequestingChallenge)
        onStepCallback?.invoke(FingerprintStep.RequestingChallenge)

        onStep(FingerprintStep.ScanningBiometric)
        onStepCallback?.invoke(FingerprintStep.ScanningBiometric)

        onStep(FingerprintStep.VerifyingSignature)
        onStepCallback?.invoke(FingerprintStep.VerifyingSignature)

        return if (shouldSucceed) {
            Result.success("step-up-token-123")
        } else {
            if (throwNonRecoverable) {
                Result.failure(FingerprintAuthException("Hardware not available", recoverable = false))
            } else {
                Result.failure(FingerprintAuthException("Scan timed out", recoverable = true))
            }
        }
    }
}
