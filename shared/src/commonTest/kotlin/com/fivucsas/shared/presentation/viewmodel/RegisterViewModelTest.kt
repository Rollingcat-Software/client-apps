package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.usecase.auth.RegisterUseCase
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import com.fivucsas.shared.test.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthRepository()
        viewModel = RegisterViewModel(RegisterUseCase(repository))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.tokens)
        assertFalse(state.isSuccess)
        assertNull(state.role)
    }

    @Test
    fun `register with valid data should succeed`() = runTest {
        viewModel.register("test@test.com", "Password123!", "John", "Doe")

        val state = viewModel.state.value
        assertTrue(state.isSuccess)
        assertNotNull(state.tokens)
        assertEquals(UserRole.USER, state.role)
    }

    @Test
    fun `register with admin role should set correct role`() = runTest {
        repository.mockRole = "TENANT_ADMIN"

        viewModel.register("admin@test.com", "Password123!", "Jane", "Smith")

        assertEquals(UserRole.TENANT_ADMIN, viewModel.state.value.role)
    }

    @Test
    fun `register with failed repo should set error`() = runTest {
        repository.shouldSucceed = false

        viewModel.register("test@test.com", "Password123!", "John", "Doe")

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `register with blank firstName should fail validation`() = runTest {
        viewModel.register("test@test.com", "Password123!", "", "Doe")

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `register with blank lastName should fail validation`() = runTest {
        viewModel.register("test@test.com", "Password123!", "John", "")

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `register with invalid email should fail validation`() = runTest {
        viewModel.register("invalid-email", "Password123!", "John", "Doe")

        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSuccess)
    }

    @Test
    fun `clearError should clear error`() = runTest {
        repository.shouldSucceed = false
        viewModel.register("test@test.com", "Password123!", "John", "Doe")

        viewModel.clearError()
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `clearError on default state is safe`() {
        viewModel.clearError()
        assertNull(viewModel.state.value.error)
    }
}
