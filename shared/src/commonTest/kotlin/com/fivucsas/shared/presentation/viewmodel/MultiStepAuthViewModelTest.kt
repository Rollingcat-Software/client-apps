package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.AuthSessionDetail
import com.fivucsas.shared.domain.model.SessionStep
import com.fivucsas.shared.domain.model.StepResult
import com.fivucsas.shared.domain.repository.AuthSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MultiStepAuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthSessionRepository
    private lateinit var viewModel: MultiStepAuthViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthSessionRepository()
        viewModel = MultiStepAuthViewModel(repository)
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
        assertFalse(state.isSubmitting)
        assertNull(state.errorMessage)
        assertTrue(state.steps.isEmpty())
        assertFalse(state.flowComplete)
        assertFalse(state.flowCancelled)
        assertEquals("", state.sessionId)
    }

    // ========== Init With Session ID ==========

    @Test
    fun `initWithSessionId should load session and set steps`() = runTest {
        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("sess-1", state.sessionId)
        assertEquals(2, state.steps.size)
        assertEquals("user-1", state.userId)
        assertEquals(0, state.currentStepIndex) // First step is PENDING
        assertNull(state.errorMessage)
    }

    @Test
    fun `initWithSessionId should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    // ========== Start New Session ==========

    @Test
    fun `startNewSession should create session and set steps`() = runTest {
        viewModel.startNewSession("tenant-1", "user-1", "APP_LOGIN")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("sess-1", state.sessionId)
        assertEquals(2, state.steps.size)
        assertEquals("user-1", state.userId)
    }

    @Test
    fun `startNewSession should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.startNewSession("tenant-1", "user-1", "APP_LOGIN")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    // ========== Init With Steps ==========

    @Test
    fun `initWithSteps should set steps directly without backend call`() {
        val steps = listOf(
            SessionStep(stepOrder = 1, authMethodType = "PASSWORD", isRequired = true, status = "PENDING"),
            SessionStep(stepOrder = 2, authMethodType = "TOTP", isRequired = false, status = "PENDING")
        )

        viewModel.initWithSteps("manual-sess", steps, "user-2")

        val state = viewModel.uiState.value
        assertEquals("manual-sess", state.sessionId)
        assertEquals(2, state.steps.size)
        assertEquals("user-2", state.userId)
        assertEquals(0, state.currentStepIndex)
        assertFalse(state.isLoading)
    }

    // ========== Complete Current Step ==========

    @Test
    fun `completeCurrentStep should advance to next step`() = runTest {
        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()

        repository.mockStepResult = StepResult(
            sessionId = "sess-1",
            stepOrder = 1,
            status = "COMPLETED",
            sessionCompleted = false,
            nextStepOrder = 2
        )

        viewModel.completeCurrentStep(mapOf("password" to "Test@123"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals(1, state.currentStepIndex) // Advanced to step 2
    }

    @Test
    fun `completeCurrentStep should mark flow complete when session completes`() = runTest {
        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()

        repository.mockStepResult = StepResult(
            sessionId = "sess-1",
            stepOrder = 1,
            status = "COMPLETED",
            sessionCompleted = true,
            data = mapOf("accessToken" to "jwt-token-123")
        )

        viewModel.completeCurrentStep(mapOf("password" to "Test@123"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.flowComplete)
        assertEquals("jwt-token-123", state.accessToken)
    }

    @Test
    fun `completeCurrentStep should set error on failure`() = runTest {
        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()

        repository.shouldSucceedStep = false

        viewModel.completeCurrentStep(mapOf("password" to "wrong"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertNotNull(state.errorMessage)
    }

    // ========== Skip Current Step ==========

    @Test
    fun `skipCurrentStep should do nothing for required steps`() = runTest {
        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()

        // First step is required
        viewModel.skipCurrentStep()
        advanceUntilIdle()

        // Should not have changed (completeStep should not have been called for skip)
        val state = viewModel.uiState.value
        assertEquals(0, state.currentStepIndex)
    }

    // ========== Cancel Flow ==========

    @Test
    fun `cancelFlow should set flowCancelled`() = runTest {
        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()

        viewModel.cancelFlow()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.flowCancelled)
    }

    @Test
    fun `cancelFlow with empty sessionId sets flowCancelled immediately`() {
        viewModel.cancelFlow()

        assertTrue(viewModel.uiState.value.flowCancelled)
    }

    // ========== Clear Error ==========

    @Test
    fun `clearError should reset error message`() = runTest {
        repository.shouldSucceed = false
        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Computed Properties ==========

    @Test
    fun `totalSteps returns correct count`() = runTest {
        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.totalSteps)
    }

    @Test
    fun `currentStepNumber returns 1-based index`() = runTest {
        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentStepNumber)
    }

    @Test
    fun `currentStep returns correct step`() = runTest {
        viewModel.initWithSessionId("sess-1")
        advanceUntilIdle()

        val currentStep = viewModel.uiState.value.currentStep
        assertNotNull(currentStep)
        assertEquals("PASSWORD", currentStep.authMethodType)
    }
}

// ── Fake ────────────────────────────────────────────────────────────────────

private class FakeAuthSessionRepository : AuthSessionRepository {
    var shouldSucceed = true
    var shouldSucceedStep = true
    var errorMessage = "Test error"

    var mockSession = AuthSessionDetail(
        sessionId = "sess-1",
        tenantId = "tenant-1",
        userId = "user-1",
        operationType = "APP_LOGIN",
        status = "IN_PROGRESS",
        currentStepOrder = 1,
        totalSteps = 2,
        steps = listOf(
            SessionStep(stepOrder = 1, authMethodType = "PASSWORD", isRequired = true, status = "PENDING"),
            SessionStep(stepOrder = 2, authMethodType = "TOTP", isRequired = false, status = "PENDING")
        ),
        expiresAt = "2026-04-05T10:00:00Z",
        createdAt = "2026-04-05T09:00:00Z"
    )

    var mockStepResult = StepResult(
        sessionId = "sess-1",
        stepOrder = 1,
        status = "COMPLETED",
        sessionCompleted = false,
        nextStepOrder = 2
    )

    override suspend fun startSession(
        tenantId: String,
        userId: String,
        operationType: String
    ): Result<AuthSessionDetail> {
        return if (shouldSucceed) Result.success(mockSession)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun getSession(sessionId: String): Result<AuthSessionDetail> {
        return if (shouldSucceed) Result.success(mockSession)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun completeStep(
        sessionId: String,
        stepOrder: Int,
        data: Map<String, Any?>
    ): Result<StepResult> {
        return if (shouldSucceedStep) Result.success(mockStepResult)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun skipStep(sessionId: String, stepOrder: Int): Result<StepResult> {
        return if (shouldSucceedStep) Result.success(mockStepResult)
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun cancelSession(sessionId: String): Result<Unit> {
        return if (shouldSucceed) Result.success(Unit)
        else Result.failure(RuntimeException(errorMessage))
    }
}
