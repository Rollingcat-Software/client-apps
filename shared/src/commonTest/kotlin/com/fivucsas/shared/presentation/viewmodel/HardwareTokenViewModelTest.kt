package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.local.TokenStorage
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.domain.repository.WebAuthnRepository
import com.fivucsas.shared.domain.repository.WebAuthnStep
import com.fivucsas.shared.platform.WebAuthnAssertionResult
import com.fivucsas.shared.platform.WebAuthnCreateResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class HardwareTokenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var webAuthnRepository: FakeWebAuthnRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: HardwareTokenViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        webAuthnRepository = FakeWebAuthnRepository()
        tokenManager = TokenManager(InMemoryTokenStorage())
        // Pre-populate user ID so the VM doesn't bail early
        tokenManager.saveTokens(AuthTokens(
            accessToken = "jwt-token",
            refreshToken = "refresh",
            expiresIn = 3600L,
            role = "USER",
            userName = "Test User",
            userEmail = "test@test.com",
            userId = "user-123",
            tenantId = "tenant-1"
        ))
        viewModel = HardwareTokenViewModel(webAuthnRepository, tokenManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Initial State ==========

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isRegistering)
        assertFalse(state.isVerifying)
        assertNull(state.credential)
        assertFalse(state.isRegistered)
        assertFalse(state.isVerified)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
        assertNull(state.stepDescription)
    }

    // ========== Register ==========

    @Test
    fun `register should set isRegistered on success`() = runTest {
        viewModel.register()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRegistering)
        assertTrue(state.isRegistered)
        assertNotNull(state.credential)
        assertNotNull(state.successMessage)
        assertNull(state.errorMessage)
    }

    @Test
    fun `register should set error on failure`() = runTest {
        webAuthnRepository.shouldSucceedRegister = false

        viewModel.register()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRegistering)
        assertFalse(state.isRegistered)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `register should fail when not logged in`() {
        tokenManager.clearTokens()

        viewModel.register()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("Not logged in"))
    }

    // ========== Register Platform ==========

    @Test
    fun `registerPlatform should set isRegistered on success`() = runTest {
        viewModel.registerPlatform()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isRegistered)
        assertNotNull(state.credential)
    }

    // ========== Verify ==========

    @Test
    fun `verify should set isVerified on success`() = runTest {
        viewModel.verify()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isVerifying)
        assertTrue(state.isVerified)
        assertNotNull(state.successMessage)
        assertNull(state.errorMessage)
    }

    @Test
    fun `verify should set error on failure`() = runTest {
        webAuthnRepository.shouldSucceedVerify = false

        viewModel.verify()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isVerifying)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `verify should fail when not logged in`() {
        tokenManager.clearTokens()

        viewModel.verify()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("Not logged in"))
    }

    // ========== Manual Callbacks ==========

    @Test
    fun `onRegistrationComplete should set credential`() {
        viewModel.onRegistrationComplete(
            credentialId = "cred-abc",
            publicKeyAlgorithm = "ES256",
            attestationFormat = "packed",
            transports = listOf("usb", "nfc")
        )

        val state = viewModel.uiState.value
        assertTrue(state.isRegistered)
        assertNotNull(state.credential)
        assertEquals("cred-abc", state.credential!!.credentialId)
        assertEquals(listOf("usb", "nfc"), state.credential!!.transports)
    }

    @Test
    fun `onVerificationComplete should set verified`() {
        viewModel.onVerificationComplete()
        assertTrue(viewModel.uiState.value.isVerified)
    }

    @Test
    fun `onError should set error message`() {
        viewModel.onError("Something failed")
        assertEquals("Something failed", viewModel.uiState.value.errorMessage)
    }

    // ========== Clear & Reset ==========

    @Test
    fun `clearMessages should reset messages`() {
        viewModel.onError("error")
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `reset should restore default state`() {
        viewModel.onRegistrationComplete("cred-1")
        assertTrue(viewModel.uiState.value.isRegistered)

        viewModel.reset()
        val state = viewModel.uiState.value
        assertFalse(state.isRegistered)
        assertFalse(state.isVerified)
        assertNull(state.credential)
        assertNull(state.errorMessage)
    }
}

// ── Fakes ────────────────────────────────────────────────────────────────────

private class FakeWebAuthnRepository : WebAuthnRepository {
    var shouldSucceedRegister = true
    var shouldSucceedVerify = true

    override suspend fun registerCredential(
        userId: String,
        authenticatorAttachment: String,
        deviceName: String?,
        onStep: (WebAuthnStep) -> Unit
    ): Result<WebAuthnCreateResult> {
        onStep(WebAuthnStep.FetchingOptions)
        onStep(WebAuthnStep.WaitingForAuthenticator)
        onStep(WebAuthnStep.VerifyingWithServer)
        return if (shouldSucceedRegister) {
            onStep(WebAuthnStep.Complete)
            Result.success(
                WebAuthnCreateResult(
                    credentialId = "credential-id-12345678901234567890abcdef",
                    publicKey = "pubkey",
                    publicKeyAlgorithm = "ES256",
                    attestationFormat = "packed",
                    transports = "usb,nfc",
                    clientDataJson = "{}"
                )
            )
        } else {
            Result.failure(RuntimeException("Registration failed"))
        }
    }

    override suspend fun verifyCredential(
        userId: String,
        allowCredentialIds: List<String>,
        onStep: (WebAuthnStep) -> Unit
    ): Result<WebAuthnAssertionResult> {
        onStep(WebAuthnStep.FetchingOptions)
        onStep(WebAuthnStep.WaitingForAuthenticator)
        return if (shouldSucceedVerify) {
            onStep(WebAuthnStep.VerifyingWithServer)
            onStep(WebAuthnStep.Complete)
            Result.success(
                WebAuthnAssertionResult(
                    credentialId = "cred-1",
                    authenticatorData = "auth-data",
                    clientDataJson = "{}",
                    signature = "sig"
                )
            )
        } else {
            Result.failure(RuntimeException("Verification failed"))
        }
    }
}

private class InMemoryTokenStorage : TokenStorage {
    private val store = mutableMapOf<String, String>()
    override fun saveToken(token: String) { store["token"] = token }
    override fun getToken(): String? = store["token"]
    override fun clearToken() { store.remove("token") }
    override fun saveRefreshToken(token: String) { store["refresh"] = token }
    override fun getRefreshToken(): String? = store["refresh"]
    override fun clearRefreshToken() { store.remove("refresh") }
    override fun saveRole(role: String) { store["role"] = role }
    override fun getRole(): String? = store["role"]
    override fun clearRole() { store.remove("role") }
    override fun saveUserName(name: String) { store["userName"] = name }
    override fun getUserName(): String? = store["userName"]
    override fun clearUserName() { store.remove("userName") }
    override fun saveUserEmail(email: String) { store["userEmail"] = email }
    override fun getUserEmail(): String? = store["userEmail"]
    override fun clearUserEmail() { store.remove("userEmail") }
    override fun saveUserId(id: String) { store["userId"] = id }
    override fun getUserId(): String? = store["userId"]
    override fun clearUserId() { store.remove("userId") }
    override fun saveTenantId(tenantId: String) { store["tenantId"] = tenantId }
    override fun getTenantId(): String? = store["tenantId"]
    override fun clearTenantId() { store.remove("tenantId") }
}
