package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.usecase.auth.LoginUseCase
import com.fivucsas.shared.presentation.viewmodel.auth.LoginState
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.test.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

/**
 * Unit tests for LoginViewModel.
 *
 * Tests:
 * - Initial state
 * - Successful login (dev mock behavior)
 * - Error clearing
 * - Role handling via devMockRole
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var loginUseCase: LoginUseCase
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        loginUseCase = LoginUseCase(fakeRepository)
        viewModel = LoginViewModel(loginUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        LoginViewModel.devMockRole = UserRole.USER
    }

    // ============== INITIAL STATE TESTS ==============

    @Test
    fun `initial state should be default LoginState`() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.tokens)
        assertFalse(state.isSuccess)
        assertNull(state.role)
    }

    // ============== SUCCESSFUL LOGIN TESTS ==============

    @Test
    fun `login with valid credentials sets success state`() = runTest {
        val email = "test@fivucsas.com"
        val password = "SecurePassword123!"

        viewModel.login(email, password)

        val state = viewModel.state.value
        assertTrue(state.isSuccess)
        assertNotNull(state.tokens)
        assertEquals("dev-token", state.tokens?.accessToken)
        assertEquals("dev-refresh", state.tokens?.refreshToken)
    }

    @Test
    fun `login sets default USER role`() = runTest {
        LoginViewModel.devMockRole = UserRole.USER

        viewModel.login("test@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertEquals(UserRole.USER, state.role)
    }

    @Test
    fun `login respects devMockRole for TENANT_ADMIN`() = runTest {
        LoginViewModel.devMockRole = UserRole.TENANT_ADMIN

        viewModel.login("admin@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertTrue(state.isSuccess)
        assertEquals(UserRole.TENANT_ADMIN, state.role)
        assertEquals("TENANT_ADMIN", state.tokens?.role)
    }

    @Test
    fun `login respects devMockRole for ROOT`() = runTest {
        LoginViewModel.devMockRole = UserRole.ROOT

        viewModel.login("root@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertTrue(state.isSuccess)
        assertEquals(UserRole.ROOT, state.role)
        assertEquals("ROOT", state.tokens?.role)
    }

    @Test
    fun `login respects devMockRole for TENANT_MEMBER`() = runTest {
        LoginViewModel.devMockRole = UserRole.TENANT_MEMBER

        viewModel.login("member@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertTrue(state.isSuccess)
        assertEquals(UserRole.TENANT_MEMBER, state.role)
    }

    @Test
    fun `login sets tokens with correct expiry`() = runTest {
        viewModel.login("test@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertNotNull(state.tokens)
        assertEquals(3600L, state.tokens?.expiresIn)
    }

    @Test
    fun `login is not in loading state after completion`() = runTest {
        viewModel.login("test@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
    }

    // ============== ERROR CLEARING TESTS ==============

    @Test
    fun `clearError clears error field`() = runTest {
        // Manually set an error state
        viewModel.login("test@fivucsas.com", "Password123!")
        viewModel.clearError()

        val state = viewModel.state.value
        assertNull(state.error)
    }

    @Test
    fun `clearError can be called multiple times safely`() = runTest {
        viewModel.clearError()
        viewModel.clearError()
        viewModel.clearError()

        val state = viewModel.state.value
        assertNull(state.error)
    }

    @Test
    fun `clearError on default state keeps error null`() {
        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }

    // ============== MULTIPLE LOGIN TESTS ==============

    @Test
    fun `consecutive logins update state`() = runTest {
        LoginViewModel.devMockRole = UserRole.USER
        viewModel.login("first@test.com", "Password1!")

        assertTrue(viewModel.state.value.isSuccess)
        assertEquals(UserRole.USER, viewModel.state.value.role)

        LoginViewModel.devMockRole = UserRole.TENANT_ADMIN
        viewModel.login("second@test.com", "Password2!")

        assertTrue(viewModel.state.value.isSuccess)
        assertEquals(UserRole.TENANT_ADMIN, viewModel.state.value.role)
    }

    @Test
    fun `login with any credentials succeeds in dev mode`() = runTest {
        viewModel.login("any@email.com", "anypassword")

        assertTrue(viewModel.state.value.isSuccess)
        assertNotNull(viewModel.state.value.tokens)
    }
}
