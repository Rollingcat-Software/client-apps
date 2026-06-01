package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.domain.usecase.auth.LoginUseCase
import com.fivucsas.shared.platform.IPushNotificationService
import com.fivucsas.shared.platform.ISecureStorage
import com.fivucsas.shared.test.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Regression test for the Phase-0 "200 AUTHENTICATED → stuck/failed" bug:
 * the server returned tokens (prod logs confirm) but the app kept the login
 * button spinning or showed a false failure.
 *
 * Root cause: `offlineCache.cacheLoginData(...)` (an encrypted-prefs write)
 * ran in `login()` BEFORE the success state was published; a throw there
 * left `isLoading` stuck (and was never mapped to a clean verdict). The fix
 * publishes the Authenticated state first and makes the side effects
 * best-effort; a late throw can no longer override a committed success.
 * This mirrors the v5.2.3 fix already in [MfaFlowViewModel].
 */
class LoginAuthenticatedRegressionTest {

    /** A secure storage whose writes throw — simulates EncryptedSharedPreferences failing. */
    private class ThrowingSecureStorage : ISecureStorage {
        override fun saveString(key: String, value: String) {
            throw RuntimeException("keystore write failed")
        }
        override fun getString(key: String): String? = null
        override fun saveBoolean(key: String, value: Boolean) { throw RuntimeException("boom") }
        override fun getBoolean(key: String, defaultValue: Boolean): Boolean = defaultValue
        override fun saveInt(key: String, value: Int) { throw RuntimeException("boom") }
        override fun getInt(key: String, defaultValue: Int): Int = defaultValue
        override fun saveLong(key: String, value: Long) { throw RuntimeException("boom") }
        override fun getLong(key: String, defaultValue: Long): Long = defaultValue
        override fun remove(key: String) {}
        override fun contains(key: String): Boolean = false
        override fun clear() {}
    }

    /** A push service that throws on every call — must not affect the verdict. */
    private class ThrowingPushService : IPushNotificationService {
        override suspend fun registerToken(userId: String, token: String) {
            throw RuntimeException("fcm failed")
        }
        override suspend fun getToken(): String = throw RuntimeException("fcm token failed")
        override fun isSupported(): Boolean = true
    }

    private fun newViewModel(): LoginViewModel {
        val repo = FakeAuthRepository() // login() returns AUTHENTICATED + tokens
        return LoginViewModel(
            loginUseCase = LoginUseCase(repo),
            offlineCache = OfflineCache(ThrowingSecureStorage()),
            pushService = ThrowingPushService(),
            authRepository = repo
        )
    }

    @Test
    fun `login reaches success even when offline-cache write throws`() = runTest {
        val vm = newViewModel()

        vm.login(email = "user@example.com", password = "Sup3rSecret!")

        val state = vm.state.value
        assertTrue(
            state.isSuccess,
            "AUTHENTICATED must reach isSuccess=true even when offline cache / push throws"
        )
        assertFalse(state.isLoading, "isLoading must be cleared on a committed success")
        assertNull(state.error, "a committed success must not carry an error")
        assertNotNull(state.tokens, "tokens must be published for navigation")
    }
}
