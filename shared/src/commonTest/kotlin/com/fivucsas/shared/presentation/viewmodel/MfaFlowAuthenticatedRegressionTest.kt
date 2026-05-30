package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.platform.IPushNotificationService
import com.fivucsas.shared.platform.ISecureStorage
import com.fivucsas.shared.presentation.viewmodel.auth.MfaFlowUiState
import com.fivucsas.shared.presentation.viewmodel.auth.MfaFlowViewModel
import com.fivucsas.shared.test.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

/**
 * Regression test for the v5.2.2 "Verification failed despite server 200"
 * bug: the server returned `AUTHENTICATED` (tokens minted) but the app
 * showed `MFA_GENERIC_ERROR` and stranded the user.
 *
 * Root cause: `offlineCache.cacheLoginData(...)` (an encrypted-prefs write)
 * ran INSIDE the verify `try` BEFORE `_authResult` was set, so a throw there
 * was swallowed by the outer `catch` and flipped a committed success into an
 * error. The fix commits the auth result first and makes the side effects
 * best-effort; the outer catch no longer overrides a committed auth.
 */
class MfaFlowAuthenticatedRegressionTest {

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

    private fun newViewModel(): MfaFlowViewModel {
        val repo = FakeAuthRepository() // verifyMfaStep returns AUTHENTICATED + tokens
        val vm = MfaFlowViewModel(repo, OfflineCache(ThrowingSecureStorage()), ThrowingPushService())
        vm.initialize(sessionToken = "tok", methods = emptyList(), step = 1, total = 1)
        return vm
    }

    @Test
    fun `AUTHENTICATED stays authenticated even when offline-cache write throws`() = runTest {
        val vm = newViewModel()

        vm.verifyStep(method = "EMAIL_OTP", data = mapOf("code" to "123456"))

        // The committed success must survive the failing side effects.
        assertIs<MfaFlowUiState.Authenticated>(
            vm.uiState.value,
            "AUTHENTICATED must not flip to Error when offline cache / push throws",
        )
    }

    @Test
    fun `AUTHENTICATED publishes the auth result for navigation`() = runTest {
        val vm = newViewModel()

        vm.verifyStep(method = "EMAIL_OTP", data = mapOf("code" to "123456"))

        val result = vm.authResult.value
        assertNotNull(result, "authResult must be set so the host can navigate to the dashboard")
        assertEquals("fake-access-token", result.tokens.accessToken)
    }
}
