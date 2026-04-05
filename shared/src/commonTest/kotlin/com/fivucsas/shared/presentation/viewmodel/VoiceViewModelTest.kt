package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.VoiceEnrollResult
import com.fivucsas.shared.domain.repository.VoiceRepository
import com.fivucsas.shared.domain.repository.VoiceSearchMatch
import com.fivucsas.shared.domain.repository.VoiceSearchResult
import com.fivucsas.shared.domain.repository.VoiceVerifyResult
import com.fivucsas.shared.presentation.state.VoiceMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeVoiceRepository
    private lateinit var viewModel: VoiceViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeVoiceRepository()
        viewModel = VoiceViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Initial State ==========

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
        assertFalse(state.isProcessing)
        assertEquals(0, state.recordingSeconds)
        assertNull(state.enrollSuccess)
        assertNull(state.verifyResult)
        assertNull(state.searchResult)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
        assertEquals(VoiceMode.ENROLL, state.selectedMode)
    }

    // ========== Mode Switching ==========

    @Test
    fun `setMode should update selected mode`() {
        viewModel.setMode(VoiceMode.VERIFY)
        assertEquals(VoiceMode.VERIFY, viewModel.uiState.value.selectedMode)

        viewModel.setMode(VoiceMode.SEARCH)
        assertEquals(VoiceMode.SEARCH, viewModel.uiState.value.selectedMode)
    }

    // ========== Recording State ==========

    @Test
    fun `setRecording should toggle recording state`() {
        viewModel.setRecording(true)
        assertTrue(viewModel.uiState.value.isRecording)

        viewModel.setRecording(false)
        assertFalse(viewModel.uiState.value.isRecording)
    }

    @Test
    fun `updateRecordingSeconds should update counter`() {
        viewModel.updateRecordingSeconds(5)
        assertEquals(5, viewModel.uiState.value.recordingSeconds)
    }

    // ========== Enroll ==========

    @Test
    fun `enroll should set enrollSuccess on success`() = runTest {
        viewModel.enroll("user-1", "base64voice")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertEquals(true, state.enrollSuccess)
        assertNotNull(state.successMessage)
        assertNull(state.errorMessage)
    }

    @Test
    fun `enroll should set error on failure`() = runTest {
        repository.shouldSucceedEnroll = false

        viewModel.enroll("user-1", "base64voice")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `enroll should set error when result is not success`() = runTest {
        repository.enrollResultSuccess = false

        viewModel.enroll("user-1", "base64voice")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.successMessage)
        assertNotNull(state.errorMessage)
    }

    // ========== Verify ==========

    @Test
    fun `verify should set verifyResult on success`() = runTest {
        viewModel.verify("user-1", "base64voice")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertNotNull(state.verifyResult)
        assertTrue(state.verifyResult!!.verified)
        assertEquals(0.92f, state.verifyResult!!.confidence)
        assertNotNull(state.successMessage)
    }

    @Test
    fun `verify should set error on failure`() = runTest {
        repository.shouldSucceedVerify = false

        viewModel.verify("user-1", "base64voice")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `verify should set error when not verified`() = runTest {
        repository.verifyResultVerified = false

        viewModel.verify("user-1", "base64voice")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.verifyResult)
        assertFalse(state.verifyResult!!.verified)
        assertNull(state.successMessage)
        assertNotNull(state.errorMessage)
    }

    // ========== Search ==========

    @Test
    fun `search should set searchResult on success`() = runTest {
        viewModel.search("base64voice")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertNotNull(state.searchResult)
        assertTrue(state.searchResult!!.found)
        assertEquals(1, state.searchResult!!.matches.size)
        assertEquals("user-1", state.searchResult!!.matches[0].userId)
    }

    @Test
    fun `search should set error on failure`() = runTest {
        repository.shouldSucceedSearch = false

        viewModel.search("base64voice")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertNotNull(state.errorMessage)
    }

    // ========== Clear & Reset ==========

    @Test
    fun `clearMessages should reset error and success messages`() = runTest {
        repository.shouldSucceedEnroll = false
        viewModel.enroll("user-1", "base64voice")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `reset should restore default state`() = runTest {
        viewModel.setMode(VoiceMode.SEARCH)
        viewModel.setRecording(true)
        viewModel.enroll("user-1", "base64voice")
        advanceUntilIdle()

        viewModel.reset()

        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
        assertFalse(state.isProcessing)
        assertEquals(0, state.recordingSeconds)
        assertEquals(VoiceMode.ENROLL, state.selectedMode)
        assertNull(state.enrollSuccess)
        assertNull(state.verifyResult)
        assertNull(state.searchResult)
    }
}

// ── Fake ────────────────────────────────────────────────────────────────────

private class FakeVoiceRepository : VoiceRepository {
    var shouldSucceedEnroll = true
    var shouldSucceedVerify = true
    var shouldSucceedSearch = true
    var enrollResultSuccess = true
    var verifyResultVerified = true

    override suspend fun enroll(userId: String, voiceBase64: String): Result<VoiceEnrollResult> {
        return if (shouldSucceedEnroll) {
            Result.success(
                VoiceEnrollResult(
                    success = enrollResultSuccess,
                    message = if (enrollResultSuccess) "Voice enrolled" else "Quality too low",
                    qualityScore = 0.88f
                )
            )
        } else {
            Result.failure(RuntimeException("Enroll failed"))
        }
    }

    override suspend fun verify(userId: String, voiceBase64: String): Result<VoiceVerifyResult> {
        return if (shouldSucceedVerify) {
            Result.success(
                VoiceVerifyResult(
                    verified = verifyResultVerified,
                    confidence = 0.92f,
                    message = if (verifyResultVerified) "Match" else "No match"
                )
            )
        } else {
            Result.failure(RuntimeException("Verify failed"))
        }
    }

    override suspend fun search(voiceBase64: String): Result<VoiceSearchResult> {
        return if (shouldSucceedSearch) {
            Result.success(
                VoiceSearchResult(
                    found = true,
                    userId = "user-1",
                    confidence = 0.89f,
                    message = "Found",
                    matches = listOf(
                        VoiceSearchMatch("user-1", 0.89f, "John Doe", "john@test.com")
                    )
                )
            )
        } else {
            Result.failure(RuntimeException("Search failed"))
        }
    }
}
