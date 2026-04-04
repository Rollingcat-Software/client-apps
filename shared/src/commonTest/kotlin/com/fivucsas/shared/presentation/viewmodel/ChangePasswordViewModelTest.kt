package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.usecase.auth.ChangePasswordUseCase
import com.fivucsas.shared.presentation.viewmodel.auth.ChangePasswordViewModel
import com.fivucsas.shared.test.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: ChangePasswordViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthRepository()
        viewModel = ChangePasswordViewModel(ChangePasswordUseCase(repository))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun `changePassword should succeed with valid data`() = runTest {
        viewModel.changePassword("OldPassword123!", "NewPassword456!", "NewPassword456!")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isSuccess)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `changePassword should fail with blank current password`() = runTest {
        viewModel.changePassword("", "NewPassword456!", "NewPassword456!")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `changePassword should fail when passwords dont match`() = runTest {
        viewModel.changePassword("OldPassword123!", "NewPassword456!", "DifferentPass789!")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `changePassword should fail when same as current`() = runTest {
        viewModel.changePassword("SamePassword123!", "SamePassword123!", "SamePassword123!")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `changePassword should fail when repo fails`() = runTest {
        repository.shouldSucceed = false

        viewModel.changePassword("OldPassword123!", "NewPassword456!", "NewPassword456!")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        viewModel.changePassword("", "New", "New")
        advanceUntilIdle()

        viewModel.clearError()
        assertNull(viewModel.state.value.errorMessage)
    }
}
