package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.EnrollmentData
import com.fivucsas.shared.domain.model.EnrollmentResult
import com.fivucsas.shared.domain.model.IdentifyResult
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.domain.model.VerificationResult
import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.domain.repository.UserRepository
import com.fivucsas.shared.domain.usecase.enrollment.EnrollUserUseCase
import com.fivucsas.shared.domain.usecase.verification.VerifyUserUseCase
import com.fivucsas.shared.presentation.state.BiometricResult
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class BiometricViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var biometricRepository: FakeBiometricViewModelRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var enrollUseCase: EnrollUserUseCase
    private lateinit var verifyUseCase: VerifyUserUseCase
    private lateinit var viewModel: BiometricViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        biometricRepository = FakeBiometricViewModelRepository()
        userRepository = FakeUserRepository()
        enrollUseCase = EnrollUserUseCase(userRepository, biometricRepository)
        verifyUseCase = VerifyUserUseCase(biometricRepository)
        viewModel = BiometricViewModel(enrollUseCase, verifyUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Initial State ==========

    @Test
    fun `initial state should be default`() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.result)
        assertFalse(state.isSuccess)
    }

    // ========== Enroll Face ==========

    @Test
    fun `enrollFace should set success result`() = runTest {
        val enrollmentData = EnrollmentData(
            fullName = "John Doe",
            email = "john@test.com",
            idNumber = "12345678901",
            phoneNumber = "+905551234567",
            address = "Istanbul"
        )

        viewModel.enrollFace(enrollmentData, byteArrayOf(1, 2, 3, 4, 5))

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertIs<BiometricResult.EnrollmentSuccess>(state.result)
        assertNull(state.error)
    }

    @Test
    fun `enrollFace with empty image should set error`() = runTest {
        val enrollmentData = EnrollmentData(
            fullName = "John Doe",
            email = "john@test.com"
        )

        viewModel.enrollFace(enrollmentData, byteArrayOf())

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `enrollFace should set error on repository failure`() = runTest {
        biometricRepository.shouldSucceedEnroll = false
        userRepository.shouldSucceedCreate = true

        val enrollmentData = EnrollmentData(
            fullName = "John Doe",
            email = "john@test.com",
            idNumber = "12345678901",
            phoneNumber = "+905551234567"
        )

        viewModel.enrollFace(enrollmentData, byteArrayOf(1, 2, 3, 4, 5))

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertFalse(state.isSuccess)
    }

    // ========== Verify Face ==========

    @Test
    fun `verifyFace should set verification result on success`() = runTest {
        viewModel.verifyFace("user-1", ByteArray(20_000) { 0 })

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertIs<BiometricResult.VerificationSuccess>(state.result)
    }

    @Test
    fun `verifyFace should set error on failure`() = runTest {
        biometricRepository.shouldSucceedVerify = false

        viewModel.verifyFace("user-1", ByteArray(20_000) { 0 })

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `verifyFace with empty image should set error`() = runTest {
        viewModel.verifyFace("user-1", byteArrayOf())

        val state = viewModel.state.value
        assertNotNull(state.error)
    }

    // ========== Capture Error ==========

    @Test
    fun `onCaptureError should set error in state`() {
        viewModel.onCaptureError("Camera not available")
        assertEquals("Camera not available", viewModel.state.value.error)
    }

    // ========== Clear State ==========

    @Test
    fun `clearState should reset to default`() {
        viewModel.onCaptureError("Some error")
        assertNotNull(viewModel.state.value.error)

        viewModel.clearState()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.result)
        assertFalse(state.isSuccess)
    }
}

// ── Fakes ────────────────────────────────────────────────────────────────────

private class FakeBiometricViewModelRepository : BiometricRepository {
    var shouldSucceedEnroll = true
    var shouldSucceedVerify = true

    override suspend fun enrollFace(userId: String, imageData: ByteArray): Result<EnrollmentResult> {
        return if (shouldSucceedEnroll) Result.success(EnrollmentResult(true, userId, 0.95f, "OK"))
        else Result.failure(RuntimeException("Enrollment failed"))
    }

    override suspend fun verifyFace(userId: String, imageData: ByteArray): Result<VerificationResult> {
        return if (shouldSucceedVerify) Result.success(VerificationResult(true, 0.92f, message = "Match"))
        else Result.failure(RuntimeException("Verification failed"))
    }

    override suspend fun checkLiveness(imageData: ByteArray): Result<LivenessResult> =
        Result.success(LivenessResult(true, 0.99f, message = "Live"))

    override suspend fun deleteBiometricData(userId: String): Result<Unit> = Result.success(Unit)

    override suspend fun identifyFace(imageData: ByteArray): Result<IdentifyResult> =
        Result.failure(RuntimeException("Not implemented"))
}

private class FakeUserRepository : UserRepository {
    var shouldSucceedCreate = true

    private val mockUser = User(
        id = "user-1",
        name = "John Doe",
        email = "john@test.com",
        idNumber = "12345678901",
        phoneNumber = "+905551234567",
        status = UserStatus.ACTIVE,
        enrollmentDate = "2026-04-05",
        hasBiometric = true
    )

    override suspend fun createUser(user: User): Result<User> {
        return if (shouldSucceedCreate) Result.success(mockUser)
        else Result.failure(RuntimeException("Create failed"))
    }

    override suspend fun updateUser(userId: String, user: User): Result<User> =
        Result.success(mockUser.copy(status = user.status, hasBiometric = user.hasBiometric))

    override suspend fun deleteUser(userId: String): Result<Unit> = Result.success(Unit)

    override suspend fun getUserById(id: String): Result<User> = Result.success(mockUser)
    override suspend fun getUsers(): Result<List<User>> = Result.success(listOf(mockUser))
    override suspend fun searchUsers(query: String): Result<List<User>> = Result.success(listOf(mockUser))
    override suspend fun getStatistics(): Result<Statistics> = Result.success(Statistics())
    override suspend fun getMyProfile(): Result<User> = Result.success(mockUser)
    override suspend fun healthCheck(): Result<Boolean> = Result.success(true)
}
