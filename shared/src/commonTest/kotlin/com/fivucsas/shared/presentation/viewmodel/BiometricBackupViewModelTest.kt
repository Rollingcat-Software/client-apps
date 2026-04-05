package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.Enrollment
import com.fivucsas.shared.domain.model.EnrollmentStatus
import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.domain.repository.EnrollmentRepository
import com.fivucsas.shared.domain.model.EnrollmentResult
import com.fivucsas.shared.domain.model.IdentifyResult
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.VerificationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class BiometricBackupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var enrollmentRepository: FakeEnrollmentRepository
    private lateinit var biometricRepository: FakeBiometricBackupRepository
    private lateinit var viewModel: BiometricBackupViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        enrollmentRepository = FakeEnrollmentRepository()
        biometricRepository = FakeBiometricBackupRepository()
        viewModel = BiometricBackupViewModel(enrollmentRepository, biometricRepository)
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
        assertTrue(state.enrollments.isEmpty())
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
        assertFalse(state.isDeleting)
        assertFalse(state.deleteConfirmDialogVisible)
    }

    // ========== Load Enrollments ==========

    @Test
    fun `loadEnrollments should populate enrollments on success`() = runTest {
        viewModel.loadEnrollments("user-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.enrollments.size)
        assertEquals("enroll-1", state.enrollments[0].id)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadEnrollments should set error on failure`() = runTest {
        enrollmentRepository.shouldSucceed = false

        viewModel.loadEnrollments("user-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertTrue(state.enrollments.isEmpty())
    }

    // ========== Delete Confirmation Dialog ==========

    @Test
    fun `showDeleteConfirmation should set dialog visible`() {
        viewModel.showDeleteConfirmation()
        assertTrue(viewModel.uiState.value.deleteConfirmDialogVisible)
    }

    @Test
    fun `hideDeleteConfirmation should hide dialog`() {
        viewModel.showDeleteConfirmation()
        assertTrue(viewModel.uiState.value.deleteConfirmDialogVisible)

        viewModel.hideDeleteConfirmation()
        assertFalse(viewModel.uiState.value.deleteConfirmDialogVisible)
    }

    // ========== Delete All Biometric Data ==========

    @Test
    fun `deleteAllBiometricData should clear enrollments on success`() = runTest {
        viewModel.loadEnrollments("user-1")
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.enrollments.size)

        viewModel.showDeleteConfirmation()
        viewModel.deleteAllBiometricData("user-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDeleting)
        assertFalse(state.deleteConfirmDialogVisible)
        assertTrue(state.enrollments.isEmpty())
        assertNotNull(state.successMessage)
    }

    @Test
    fun `deleteAllBiometricData should set error on failure`() = runTest {
        biometricRepository.shouldSucceedDelete = false

        viewModel.deleteAllBiometricData("user-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDeleting)
        assertNotNull(state.errorMessage)
    }

    // ========== Clear Messages ==========

    @Test
    fun `clearMessages should reset error and success`() = runTest {
        enrollmentRepository.shouldSucceed = false
        viewModel.loadEnrollments("user-1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }
}

// ── Fakes ────────────────────────────────────────────────────────────────────

private class FakeEnrollmentRepository : EnrollmentRepository {
    var shouldSucceed = true

    override suspend fun getEnrollments(userId: String): Result<List<Enrollment>> {
        return if (shouldSucceed) {
            Result.success(
                listOf(
                    Enrollment(id = "enroll-1", userId = userId, method = "FACE", status = EnrollmentStatus.ENROLLED),
                    Enrollment(id = "enroll-2", userId = userId, method = "VOICE", status = EnrollmentStatus.ENROLLED)
                )
            )
        } else {
            Result.failure(RuntimeException("Failed to load enrollments"))
        }
    }
}

private class FakeBiometricBackupRepository : BiometricRepository {
    var shouldSucceedDelete = true

    override suspend fun enrollFace(userId: String, imageData: ByteArray): Result<EnrollmentResult> =
        Result.success(EnrollmentResult(true, userId, 0.95f, "OK"))

    override suspend fun verifyFace(userId: String, imageData: ByteArray): Result<VerificationResult> =
        Result.success(VerificationResult(true, 0.95f, message = "OK"))

    override suspend fun checkLiveness(imageData: ByteArray): Result<LivenessResult> =
        Result.success(LivenessResult(true, 0.99f, message = "Live"))

    override suspend fun deleteBiometricData(userId: String): Result<Unit> {
        return if (shouldSucceedDelete) Result.success(Unit)
        else Result.failure(RuntimeException("Delete failed"))
    }

    override suspend fun identifyFace(imageData: ByteArray): Result<IdentifyResult> =
        Result.failure(RuntimeException("Not implemented"))
}
