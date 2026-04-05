package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.EnrollmentResult
import com.fivucsas.shared.domain.model.IdentifyResult
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.VerificationResult
import com.fivucsas.shared.domain.repository.BiometricRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CardDetectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var biometricRepository: FakeCardBiometricRepository
    private lateinit var viewModel: CardDetectionViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        biometricRepository = FakeCardBiometricRepository()
        viewModel = CardDetectionViewModel(biometricRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Initial State ==========

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertNull(state.result)
        assertNull(state.errorMessage)
        assertNull(state.capturedImageBytes)
    }

    // ========== Detect Card ==========

    @Test
    fun `detectCard should set result on success`() = runTest {
        val imageBytes = byteArrayOf(1, 2, 3)
        viewModel.detectCard(imageBytes)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertNotNull(state.result)
        assertEquals("id_card", state.result!!.cardType)
        assertEquals("ID Card", state.result!!.cardTypeLabel)
        assertNull(state.errorMessage)
    }

    @Test
    fun `detectCard should set error on failure`() = runTest {
        biometricRepository.shouldSucceed = false

        viewModel.detectCard(byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertNull(state.result)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `detectCard should store captured image bytes`() = runTest {
        val imageBytes = byteArrayOf(10, 20, 30)
        viewModel.detectCard(imageBytes)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.capturedImageBytes)
        assertTrue(imageBytes.contentEquals(state.capturedImageBytes!!))
    }

    // ========== CardTypeLabels ==========

    @Test
    fun `CardTypeLabels should return correct English label`() {
        assertEquals("Turkish ID Card", CardTypeLabels.getLabel("tc_kimlik"))
        assertEquals("Passport", CardTypeLabels.getLabel("pasaport"))
        assertEquals("Unknown Card", CardTypeLabels.getLabel("nonexistent"))
    }

    @Test
    fun `CardTypeLabels should return correct Turkish label`() {
        assertEquals("TC Kimlik Karti", CardTypeLabels.getLabel("tc_kimlik", turkish = true))
        assertEquals("Pasaport", CardTypeLabels.getLabel("pasaport", turkish = true))
    }

    @Test
    fun `CardTypeLabels should be case insensitive`() {
        assertEquals("Turkish ID Card", CardTypeLabels.getLabel("TC_KIMLIK"))
        assertEquals("Passport", CardTypeLabels.getLabel("PASAPORT"))
    }

    // ========== Clear & Reset ==========

    @Test
    fun `clearMessages should reset error`() = runTest {
        biometricRepository.shouldSucceed = false
        viewModel.detectCard(byteArrayOf(1))
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `reset should restore default state`() = runTest {
        viewModel.detectCard(byteArrayOf(1, 2, 3))
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.result)

        viewModel.reset()
        val state = viewModel.uiState.value
        assertFalse(state.isProcessing)
        assertNull(state.result)
        assertNull(state.errorMessage)
        assertNull(state.capturedImageBytes)
    }
}

// ── Fake ────────────────────────────────────────────────────────────────────

private class FakeCardBiometricRepository : BiometricRepository {
    var shouldSucceed = true

    override suspend fun enrollFace(userId: String, imageData: ByteArray): Result<EnrollmentResult> =
        Result.success(EnrollmentResult(true, userId, 0.95f, "OK"))

    override suspend fun verifyFace(userId: String, imageData: ByteArray): Result<VerificationResult> =
        Result.success(VerificationResult(true, 0.95f, message = "OK"))

    override suspend fun checkLiveness(imageData: ByteArray): Result<LivenessResult> {
        return if (shouldSucceed) {
            Result.success(LivenessResult(true, 0.98f, message = "Card detected"))
        } else {
            Result.failure(RuntimeException("Detection failed"))
        }
    }

    override suspend fun deleteBiometricData(userId: String): Result<Unit> = Result.success(Unit)

    override suspend fun identifyFace(imageData: ByteArray): Result<IdentifyResult> =
        Result.failure(RuntimeException("Not implemented"))
}
