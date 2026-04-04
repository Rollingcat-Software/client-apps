package com.fivucsas.mobile.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.domain.usecase.auth.LoginUseCase
import com.fivucsas.shared.platform.ISecureStorage
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.ui.screen.LoginScreen
import org.junit.Rule
import org.junit.Test

/**
 * E2E / instrumented tests for the Login screen.
 *
 * These tests render the real LoginScreen composable and verify that the
 * form elements are present, navigation callbacks fire, and the submit
 * button triggers the login flow correctly.
 *
 * A stub AuthRepository is used so tests run without a real backend.
 */
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Stubs ──────────────────────────────────────────────────────

    /** In-memory ISecureStorage for tests. */
    private class InMemorySecureStorage : ISecureStorage {
        private val strings = mutableMapOf<String, String>()
        private val booleans = mutableMapOf<String, Boolean>()
        private val ints = mutableMapOf<String, Int>()
        private val longs = mutableMapOf<String, Long>()

        override fun saveString(key: String, value: String) { strings[key] = value }
        override fun getString(key: String): String? = strings[key]
        override fun saveBoolean(key: String, value: Boolean) { booleans[key] = value }
        override fun getBoolean(key: String, defaultValue: Boolean) = booleans[key] ?: defaultValue
        override fun saveInt(key: String, value: Int) { ints[key] = value }
        override fun getInt(key: String, defaultValue: Int) = ints[key] ?: defaultValue
        override fun saveLong(key: String, value: Long) { longs[key] = value }
        override fun getLong(key: String, defaultValue: Long) = longs[key] ?: defaultValue
        override fun remove(key: String) { strings.remove(key); booleans.remove(key); ints.remove(key); longs.remove(key) }
        override fun clear() { strings.clear(); booleans.clear(); ints.clear(); longs.clear() }
        override fun contains(key: String) = key in strings || key in booleans || key in ints || key in longs
    }

    /** AuthRepository that always fails with a controlled error. */
    private class FailingAuthRepository : AuthRepository {
        override suspend fun login(email: String, password: String): Result<AuthTokens> =
            Result.failure(RuntimeException("stub: no backend"))
        override suspend fun register(email: String, password: String, firstName: String, lastName: String): Result<AuthTokens> =
            Result.failure(RuntimeException("stub"))
        override suspend fun logout(): Result<Unit> = Result.success(Unit)
        override suspend fun refreshToken(refreshToken: String): Result<AuthTokens> =
            Result.failure(RuntimeException("stub"))
        override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
            Result.failure(RuntimeException("stub"))
        override suspend fun isAuthenticated(): Boolean = false
        override suspend fun getAccessToken(): String? = null
    }

    private fun viewModel(): LoginViewModel {
        val loginUseCase = LoginUseCase(FailingAuthRepository())
        val offlineCache = OfflineCache(InMemorySecureStorage())
        return LoginViewModel(loginUseCase, offlineCache)
    }

    private fun setLoginScreen(vm: LoginViewModel = viewModel()) {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = vm,
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                onNavigateToGuestFaceCheck = {},
                onLoginSuccess = {}
            )
        }
    }

    // ── Tests ──────────────────────────────────────────────────────

    @Test
    fun loginScreen_rendersAllFormElements() {
        setLoginScreen()

        composeTestRule.onNodeWithText("FIVUCSAS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot password?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account? Register").assertIsDisplayed()
    }

    @Test
    fun loginScreen_emailAndPasswordFieldsAcceptInput() {
        setLoginScreen()

        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("secret123")

        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
    }

    @Test
    fun loginScreen_loginButtonIsEnabled() {
        setLoginScreen()

        composeTestRule.onNodeWithText("Login").assertIsEnabled()
    }

    @Test
    fun loginScreen_forgotPasswordNavigationWorks() {
        var clicked = false
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel(),
                onNavigateToRegister = {},
                onNavigateToForgotPassword = { clicked = true },
                onNavigateToGuestFaceCheck = {},
                onLoginSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Forgot password?").performClick()
        assert(clicked) { "Forgot password callback was not invoked" }
    }

    @Test
    fun loginScreen_registerNavigationWorks() {
        var navigated = false
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel(),
                onNavigateToRegister = { navigated = true },
                onNavigateToForgotPassword = {},
                onNavigateToGuestFaceCheck = {},
                onLoginSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        assert(navigated) { "Register navigation callback was not invoked" }
    }

    @Test
    fun loginScreen_guestFaceCheckNavigationWorks() {
        var navigated = false
        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel(),
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {},
                onNavigateToGuestFaceCheck = { navigated = true },
                onLoginSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Continue as Guest (Face Check)").performClick()
        assert(navigated) { "Guest face check navigation callback was not invoked" }
    }

    @Test
    fun loginScreen_submitTriggersLoginAndShowsError() {
        setLoginScreen()

        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("wrong")
        composeTestRule.onNodeWithText("Login").performClick()

        // Wait for the coroutine to complete -- the stub always fails
        composeTestRule.waitForIdle()

        // The error message mapped from "stub: no backend" should appear
        composeTestRule.onNodeWithText("Login failed. Please try again.").assertIsDisplayed()
    }
}
