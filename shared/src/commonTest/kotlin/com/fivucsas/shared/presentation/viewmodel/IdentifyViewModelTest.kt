package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.usecase.verification.IdentifyUserUseCase
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
class IdentifyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var biometricRepository: FakeBiometricRepository
    private lateinit var identifyUserUseCase: IdentifyUserUseCase
    private lateinit var viewModel: IdentifyViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        biometricRepository = FakeBiometricRepository()
        identifyUserUseCase = IdentifyUserUseCase(biometricRepository)
        viewModel = IdentifyViewModel(identifyUserUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.result)
        assertNull(state.errorMessage)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `identifyFace should set result on success`() = runTest {
        val imageBytes = ByteArray(20 * 1024) { it.toByte() } // > MIN_IMAGE_SIZE

        viewModel.identifyFace(imageBytes)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.result)
        assertTrue(state.isSuccess)
        assertEquals("user-1", state.result?.userId)
        assertFalse(state.isLoading)
    }

    @Test
    fun `identifyFace should set error on failure`() = runTest {
        biometricRepository.shouldSucceed = false
        val imageBytes = ByteArray(20 * 1024) { it.toByte() }

        viewModel.identifyFace(imageBytes)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `identifyFace with empty image should set error`() = runTest {
        viewModel.identifyFace(ByteArray(0))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `identifyFace with too small image should set error`() = runTest {
        viewModel.identifyFace(ByteArray(100)) // < 10KB
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `onCaptureError should set error message`() {
        viewModel.onCaptureError("Camera failed")
        assertEquals("Camera failed", viewModel.state.value.errorMessage)
    }

    @Test
    fun `clearState should reset to default`() = runTest {
        val imageBytes = ByteArray(20 * 1024) { it.toByte() }
        viewModel.identifyFace(imageBytes)
        advanceUntilIdle()

        viewModel.clearState()

        val state = viewModel.state.value
        assertNull(state.result)
        assertNull(state.errorMessage)
        assertFalse(state.isSuccess)
    }
}
