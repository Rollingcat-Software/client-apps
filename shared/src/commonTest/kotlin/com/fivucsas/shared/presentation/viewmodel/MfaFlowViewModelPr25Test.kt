package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.data.remote.dto.MfaSwitchMethodResponse
import com.fivucsas.shared.platform.IPushNotificationService
import com.fivucsas.shared.platform.ISecureStorage
import com.fivucsas.shared.presentation.viewmodel.auth.MfaFlowUiState
import com.fivucsas.shared.presentation.viewmodel.auth.MfaFlowViewModel
import com.fivucsas.shared.test.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for MfaFlowViewModel covering the PR #25 endpoints:
 *  - cancelSession() → DELETE /auth/mfa/session/{token}, transitions to Cancelled
 *  - switchMethod()  → POST /auth/mfa/switch-method, applies new step snapshot
 *  - handleNeedsEnrollment() → surfaces NeedsEnrollment state
 *  - 400 NEEDS_ENROLLMENT envelope parsing in verifyStep failure path
 */
class MfaFlowViewModelPr25Test {

    private lateinit var fakeRepo: FakeAuthRepository
    private lateinit var offlineCache: OfflineCache
    private lateinit var viewModel: MfaFlowViewModel

    @BeforeTest
    fun setup() {
        fakeRepo = FakeAuthRepository()
        offlineCache = OfflineCache(Pr25InMemorySecureStorage())
        viewModel = MfaFlowViewModel(fakeRepo, offlineCache, FakeNoopPushService())
        viewModel.initialize(
            sessionToken = "session-token-xyz",
            methods = emptyList(),
            step = 1,
            total = 2
        )
    }

    @AfterTest
    fun tearDown() {
        // no-op (no Dispatchers.setMain — we use plain runTest)
    }

    // ────── cancelSession() ──────

    @Test
    fun `cancelSession calls repository with current sessionToken and transitions to Cancelled`() = runTest {
        viewModel.cancelSession()

        assertTrue(fakeRepo.cancelMfaSessionCalled)
        assertEquals("session-token-xyz", fakeRepo.lastCancelledSessionToken)
        assertIs<MfaFlowUiState.Cancelled>(viewModel.uiState.value)
    }

    @Test
    fun `cancelSession transitions to Cancelled even when network fails`() = runTest {
        fakeRepo.shouldSucceed = false
        viewModel.cancelSession()
        // Best-effort cancel — UI must always end up in Cancelled.
        assertIs<MfaFlowUiState.Cancelled>(viewModel.uiState.value)
    }

    // ────── switchMethod() ──────

    @Test
    fun `switchMethod success applies METHOD_SWITCHED snapshot and lands in StepInput`() = runTest {
        fakeRepo.mockSwitchMfaResponse = MfaSwitchMethodResponse(
            status = "METHOD_SWITCHED",
            currentStep = 1,
            totalSteps = 2,
            expectedMethod = "EMAIL_OTP",
            availableMethods = emptyList(),
            alternativeMethods = emptyList(),
            completedMethods = emptyList()
        )

        viewModel.switchMethod("EMAIL_OTP")

        assertTrue(fakeRepo.switchMfaMethodCalled)
        assertEquals("EMAIL_OTP", fakeRepo.lastSwitchedMethod)
        val state = viewModel.uiState.value
        assertIs<MfaFlowUiState.StepInput>(state)
        assertEquals("EMAIL_OTP", state.method)
    }

    @Test
    fun `switchMethod with NEEDS_ENROLLMENT envelope surfaces NeedsEnrollment state`() = runTest {
        fakeRepo.mockSwitchMfaResponse = MfaSwitchMethodResponse(
            status = "ERROR",
            errorCode = "NEEDS_ENROLLMENT",
            enrollmentUrl = "/enroll/totp",
            message = "Please set up TOTP first"
        )

        viewModel.switchMethod("TOTP")

        val state = viewModel.uiState.value
        assertIs<MfaFlowUiState.NeedsEnrollment>(state)
        assertEquals("TOTP", state.method)
        assertEquals("/enroll/totp", state.enrollmentUrl)
        assertEquals("Please set up TOTP first", state.description)
    }

    @Test
    fun `switchMethod with METHOD_ALREADY_USED surfaces Error state with localized message`() = runTest {
        fakeRepo.mockSwitchMfaResponse = MfaSwitchMethodResponse(
            status = "ERROR",
            errorCode = "METHOD_ALREADY_USED",
            message = "Already used"
        )

        viewModel.switchMethod("TOTP")

        val state = viewModel.uiState.value
        assertIs<MfaFlowUiState.Error>(state)
        // String comes from MFA_METHOD_ALREADY_USED key (en).
        assertTrue(state.message.contains("already", ignoreCase = true))
    }

    @Test
    fun `switchMethod with METHOD_NOT_PERMITTED surfaces Error state`() = runTest {
        fakeRepo.mockSwitchMfaResponse = MfaSwitchMethodResponse(
            status = "ERROR",
            errorCode = "METHOD_NOT_PERMITTED",
            message = "Nope"
        )

        viewModel.switchMethod("FACE")

        val state = viewModel.uiState.value
        assertIs<MfaFlowUiState.Error>(state)
        assertTrue(state.message.contains("not allowed", ignoreCase = true) ||
            state.message.contains("isn't allowed", ignoreCase = true))
    }

    // ────── handleNeedsEnrollment() ──────

    @Test
    fun `handleNeedsEnrollment sets NeedsEnrollment state with method and url`() {
        viewModel.handleNeedsEnrollment(
            method = "VOICE",
            enrollmentUrl = "/enroll/voice",
            description = "Voice not enrolled"
        )

        val state = viewModel.uiState.value
        assertIs<MfaFlowUiState.NeedsEnrollment>(state)
        assertEquals("VOICE", state.method)
        assertEquals("/enroll/voice", state.enrollmentUrl)
        assertEquals("Voice not enrolled", state.description)
    }

    // ────── parseErrorEnvelope() — 400 envelope from server ──────

    @Test
    fun `parseErrorEnvelope extracts error fields from 400 envelope`() {
        val raw = """
            400 {
              "timestamp":"2026-04-25T10:00:00Z",
              "status":400,
              "error":"NEEDS_ENROLLMENT",
              "message":"You need to enroll TOTP",
              "method":"TOTP",
              "enrollmentUrl":"/enroll/totp",
              "path":"/api/v1/auth/mfa/step"
            }
        """.trimIndent()

        val parsed = viewModel.parseErrorEnvelope(RuntimeException(raw))

        assertEquals("NEEDS_ENROLLMENT", parsed?.errorCode)
        assertEquals("TOTP", parsed?.method)
        assertEquals("/enroll/totp", parsed?.enrollmentUrl)
    }

    @Test
    fun `parseErrorEnvelope returns null for non-JSON message`() {
        val parsed = viewModel.parseErrorEnvelope(RuntimeException("plain text error"))
        assertEquals(null, parsed)
    }
}

private class FakeNoopPushService : IPushNotificationService {
    override suspend fun registerToken(userId: String, token: String) {}
    override suspend fun getToken(): String? = null
    override fun isSupported(): Boolean = false
}

private class Pr25InMemorySecureStorage : ISecureStorage {
    private val strs = mutableMapOf<String, String>()
    private val bools = mutableMapOf<String, Boolean>()
    private val ints = mutableMapOf<String, Int>()
    private val longs = mutableMapOf<String, Long>()
    override fun saveString(key: String, value: String) { strs[key] = value }
    override fun getString(key: String): String? = strs[key]
    override fun saveBoolean(key: String, value: Boolean) { bools[key] = value }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = bools[key] ?: defaultValue
    override fun saveInt(key: String, value: Int) { ints[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = ints[key] ?: defaultValue
    override fun saveLong(key: String, value: Long) { longs[key] = value }
    override fun getLong(key: String, defaultValue: Long): Long = longs[key] ?: defaultValue
    override fun remove(key: String) { strs.remove(key); bools.remove(key); ints.remove(key); longs.remove(key) }
    override fun contains(key: String): Boolean = strs.containsKey(key) || bools.containsKey(key) || ints.containsKey(key) || longs.containsKey(key)
    override fun clear() { strs.clear(); bools.clear(); ints.clear(); longs.clear() }
}
