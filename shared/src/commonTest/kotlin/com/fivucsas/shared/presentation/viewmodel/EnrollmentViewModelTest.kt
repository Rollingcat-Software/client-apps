package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.test.mocks.FakeEnrollmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class EnrollmentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeEnrollmentRepository
    private lateinit var viewModel: EnrollmentViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeEnrollmentRepository()
        viewModel = EnrollmentViewModel(repository)
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
        assertNull(state.errorMessage)
        assertTrue(state.enrollments.isEmpty())
    }

    // ========== Load Enrollments ==========

    @Test
    fun `loadEnrollments should populate enrollments on success`() = runTest {
        viewModel.loadEnrollments("user-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.enrollments.size)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadEnrollments should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadEnrollments("user-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    // ========== Clear Messages ==========

    @Test
    fun `clearMessages should reset all messages`() {
        viewModel.clearMessages()

        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
