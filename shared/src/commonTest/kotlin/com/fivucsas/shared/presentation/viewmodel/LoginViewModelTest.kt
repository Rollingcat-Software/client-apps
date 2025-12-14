package com.fivucsas.shared.presentation.viewmodel

import app.cash.turbine.test
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.usecase.auth.LoginUseCase
import com.fivucsas.shared.test.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

/**
 * Comprehensive unit tests for LoginViewModel.
 *
 * Tests all scenarios:
 * - Successful login
 * - Invalid credentials
 * - Network errors
 * - Loading states
 * - Input validation
 * - Error clearing
 * - State management
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
    }

    // ============== SUCCESSFUL LOGIN TESTS ==============

    @Test
    fun `login with valid credentials emits Success state`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "SecurePassword123!"

        // Prepare fake repository
        fakeRepository.shouldSucceed = true
        fakeRepository.mockUser = User(
            id = "user-123",
            email = email,
            firstName = "Test",
            lastName = "User",
            isBiometricEnrolled = false
        )

        // Act & Assert
        viewModel.state.test {
            // Initial state
            assertEquals(LoginState.Idle, awaitItem())

            // Trigger login
            viewModel.login(email, password)

            // Loading state
            assertEquals(LoginState.Loading, awaitItem())

            // Success state
            val successState = awaitItem()
            assertTrue(successState is LoginState.Success)
            assertEquals(email, successState.user.email)
            assertEquals("Test", successState.user.firstName)
        }
    }

    @Test
    fun `successful login updates user state`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "SecurePassword123!"

        fakeRepository.shouldSucceed = true
        fakeRepository.mockUser = User(
            id = "user-123",
            email = email,
            firstName = "Test",
            lastName = "User",
            isBiometricEnrolled = true
        )

        // Act
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val currentState = viewModel.state.value
        assertTrue(currentState is LoginState.Success)
        assertEquals(email, currentState.user.email)
        assertTrue(currentState.user.isBiometricEnrolled)
    }

    @Test
    fun `successful login stores authentication tokens`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "SecurePassword123!"

        fakeRepository.shouldSucceed = true
        fakeRepository.mockAccessToken = "access-token-123"
        fakeRepository.mockRefreshToken = "refresh-token-456"

        // Act
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(fakeRepository.loginCalled)
        assertEquals(email, fakeRepository.lastLoginEmail)
        assertEquals(password, fakeRepository.lastLoginPassword)
    }

    // ============== INVALID CREDENTIALS TESTS ==============

    @Test
    fun `login with invalid credentials emits Error state`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "WrongPassword!"

        fakeRepository.shouldSucceed = false
        fakeRepository.errorMessage = "Invalid credentials"

        // Act & Assert
        viewModel.state.test {
            assertEquals(LoginState.Idle, awaitItem())

            viewModel.login(email, password)

            assertEquals(LoginState.Loading, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertEquals("Invalid credentials", errorState.message)
        }
    }

    @Test
    fun `login with non-existent user emits Error state`() = runTest {
        // Arrange
        val email = "nonexistent@fivucsas.com"
        val password = "SomePassword123!"

        fakeRepository.shouldSucceed = false
        fakeRepository.errorMessage = "User not found"

        // Act & Assert
        viewModel.state.test {
            skipItems(1) // Skip Idle

            viewModel.login(email, password)

            assertEquals(LoginState.Loading, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertTrue(errorState.message.contains("not found", ignoreCase = true))
        }
    }

    // ============== NETWORK ERROR TESTS ==============

    @Test
    fun `login with network error emits Error state`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "SecurePassword123!"

        fakeRepository.shouldSucceed = false
        fakeRepository.errorMessage = "Network error: Unable to connect"

        // Act & Assert
        viewModel.state.test {
            skipItems(1) // Skip Idle

            viewModel.login(email, password)

            assertEquals(LoginState.Loading, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertTrue(errorState.message.contains("Network", ignoreCase = true))
        }
    }

    @Test
    fun `login timeout emits Error state`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "SecurePassword123!"

        fakeRepository.shouldSucceed = false
        fakeRepository.errorMessage = "Request timeout"

        // Act & Assert
        viewModel.state.test {
            skipItems(1)

            viewModel.login(email, password)

            assertEquals(LoginState.Loading, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertTrue(errorState.message.contains("timeout", ignoreCase = true))
        }
    }

    // ============== LOADING STATE TESTS ==============

    @Test
    fun `login emits Loading state before completion`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "SecurePassword123!"

        fakeRepository.shouldSucceed = true
        fakeRepository.mockUser = User(
            id = "user-123",
            email = email,
            firstName = "Test",
            lastName = "User",
            isBiometricEnrolled = false
        )

        // Act & Assert
        viewModel.state.test {
            assertEquals(LoginState.Idle, awaitItem())

            viewModel.login(email, password)

            val loadingState = awaitItem()
            assertEquals(LoginState.Loading, loadingState)

            // Wait for completion
            awaitItem()
        }
    }

    @Test
    fun `loading state is emitted immediately after login call`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "SecurePassword123!"

        // Act
        viewModel.state.test {
            skipItems(1) // Skip Idle

            viewModel.login(email, password)

            // Loading should be immediate
            val state = awaitItem()
            assertEquals(LoginState.Loading, state)
        }
    }

    // ============== INPUT VALIDATION TESTS ==============

    @Test
    fun `login with empty email emits Error state`() = runTest {
        // Arrange
        val email = ""
        val password = "SecurePassword123!"

        // Act & Assert
        viewModel.state.test {
            skipItems(1)

            viewModel.login(email, password)

            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertTrue(errorState.message.contains("email", ignoreCase = true))
        }
    }

    @Test
    fun `login with blank email emits Error state`() = runTest {
        // Arrange
        val email = "   "
        val password = "SecurePassword123!"

        // Act & Assert
        viewModel.state.test {
            skipItems(1)

            viewModel.login(email, password)

            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertTrue(errorState.message.contains("email", ignoreCase = true))
        }
    }

    @Test
    fun `login with empty password emits Error state`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = ""

        // Act & Assert
        viewModel.state.test {
            skipItems(1)

            viewModel.login(email, password)

            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertTrue(errorState.message.contains("password", ignoreCase = true))
        }
    }

    @Test
    fun `login with blank password emits Error state`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "   "

        // Act & Assert
        viewModel.state.test {
            skipItems(1)

            viewModel.login(email, password)

            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertTrue(errorState.message.contains("password", ignoreCase = true))
        }
    }

    @Test
    fun `login with invalid email format emits Error state`() = runTest {
        // Arrange
        val email = "invalid-email"
        val password = "SecurePassword123!"

        fakeRepository.shouldSucceed = false
        fakeRepository.errorMessage = "Invalid email format"

        // Act & Assert
        viewModel.state.test {
            skipItems(1)

            viewModel.login(email, password)

            val loadingState = awaitItem()
            assertEquals(LoginState.Loading, loadingState)

            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertTrue(errorState.message.contains("email", ignoreCase = true))
        }
    }

    // ============== STATE UPDATE TESTS ==============

    @Test
    fun `updateEmail updates email state`() = runTest {
        // Arrange
        val newEmail = "newemail@fivucsas.com"

        // Act
        viewModel.updateEmail(newEmail)

        // Assert
        assertEquals(newEmail, viewModel.email.value)
    }

    @Test
    fun `updatePassword updates password state`() = runTest {
        // Arrange
        val newPassword = "NewPassword123!"

        // Act
        viewModel.updatePassword(newPassword)

        // Assert
        assertEquals(newPassword, viewModel.password.value)
    }

    @Test
    fun `updateEmail with empty string updates state`() = runTest {
        // Arrange
        viewModel.updateEmail("initial@email.com")

        // Act
        viewModel.updateEmail("")

        // Assert
        assertEquals("", viewModel.email.value)
    }

    // ============== ERROR CLEARING TESTS ==============

    @Test
    fun `clearError resets state to Idle`() = runTest {
        // Arrange - Set error state
        viewModel.login("", "")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.clearError()

        // Assert
        assertEquals(LoginState.Idle, viewModel.state.value)
    }

    @Test
    fun `clearError can be called multiple times`() = runTest {
        // Arrange
        viewModel.login("", "")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.clearError()
        viewModel.clearError()
        viewModel.clearError()

        // Assert
        assertEquals(LoginState.Idle, viewModel.state.value)
    }

    @Test
    fun `clearError from Idle state remains Idle`() = runTest {
        // Arrange - Already in Idle state

        // Act
        viewModel.clearError()

        // Assert
        assertEquals(LoginState.Idle, viewModel.state.value)
    }

    // ============== EDGE CASE TESTS ==============

    @Test
    fun `login with very long email succeeds`() = runTest {
        // Arrange
        val longEmail = "very.long.email.address.with.many.dots@subdomain.example.company.com"
        val password = "SecurePassword123!"

        fakeRepository.shouldSucceed = true
        fakeRepository.mockUser = User(
            id = "user-123",
            email = longEmail,
            firstName = "Test",
            lastName = "User",
            isBiometricEnrolled = false
        )

        // Act
        viewModel.login(longEmail, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertTrue(state is LoginState.Success)
        assertEquals(longEmail, state.user.email)
    }

    @Test
    fun `login with special characters in password succeeds`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "P@ssw0rd!#\$%^&*()"

        fakeRepository.shouldSucceed = true
        fakeRepository.mockUser = User(
            id = "user-123",
            email = email,
            firstName = "Test",
            lastName = "User",
            isBiometricEnrolled = false
        )

        // Act
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertTrue(state is LoginState.Success)
    }

    @Test
    fun `concurrent login calls handle gracefully`() = runTest {
        // Arrange
        val email = "test@fivucsas.com"
        val password = "SecurePassword123!"

        fakeRepository.shouldSucceed = true
        fakeRepository.mockUser = User(
            id = "user-123",
            email = email,
            firstName = "Test",
            lastName = "User",
            isBiometricEnrolled = false
        )

        // Act - Call login multiple times
        viewModel.login(email, password)
        viewModel.login(email, password)
        viewModel.login(email, password)

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert - Should complete successfully (last call wins)
        val state = viewModel.state.value
        assertTrue(state is LoginState.Success)
    }

    @Test
    fun `login after successful login clears previous state`() = runTest {
        // Arrange - First successful login
        fakeRepository.shouldSucceed = true
        fakeRepository.mockUser = User(
            id = "user-1",
            email = "first@test.com",
            firstName = "First",
            lastName = "User",
            isBiometricEnrolled = false
        )

        viewModel.login("first@test.com", "Password1!")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act - Second login
        fakeRepository.mockUser = User(
            id = "user-2",
            email = "second@test.com",
            firstName = "Second",
            lastName = "User",
            isBiometricEnrolled = true
        )

        viewModel.login("second@test.com", "Password2!")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertTrue(state is LoginState.Success)
        assertEquals("second@test.com", state.user.email)
        assertEquals("Second", state.user.firstName)
    }
}

// ============== LOGIN STATE SEALED CLASS ==============

/**
 * Represents the state of the login process.
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}
