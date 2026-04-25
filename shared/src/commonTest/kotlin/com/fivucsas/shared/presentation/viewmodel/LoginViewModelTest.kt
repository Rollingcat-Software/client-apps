package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.usecase.auth.LoginUseCase
import com.fivucsas.shared.platform.IPushNotificationService
import com.fivucsas.shared.platform.ISecureStorage
import com.fivucsas.shared.presentation.state.LoginState
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
 * - Successful login
 * - Error clearing
 * - Role handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var offlineCache: OfflineCache
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        loginUseCase = LoginUseCase(fakeRepository)
        offlineCache = OfflineCache(InMemorySecureStorage())
        viewModel = LoginViewModel(loginUseCase, offlineCache, FakePushService(), fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
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
        assertEquals("fake-access-token", state.tokens?.accessToken)
        assertEquals("fake-refresh-token", state.tokens?.refreshToken)
    }

    @Test
    fun `login sets default USER role`() = runTest {
        fakeRepository.mockRole = "USER"

        viewModel.login("test@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertEquals(UserRole.USER, state.role)
    }

    @Test
    fun `login respects role for TENANT_ADMIN`() = runTest {
        fakeRepository.mockRole = "TENANT_ADMIN"

        viewModel.login("admin@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertTrue(state.isSuccess)
        assertEquals(UserRole.TENANT_ADMIN, state.role)
        assertEquals("TENANT_ADMIN", state.tokens?.role)
    }

    @Test
    fun `login respects role for ROOT`() = runTest {
        fakeRepository.mockRole = "ROOT"

        viewModel.login("root@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertTrue(state.isSuccess)
        assertEquals(UserRole.ROOT, state.role)
        assertEquals("ROOT", state.tokens?.role)
    }

    @Test
    fun `login respects role for TENANT_MEMBER`() = runTest {
        fakeRepository.mockRole = "TENANT_MEMBER"

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

    // ============== ERROR HANDLING TESTS ==============

    @Test
    fun `login with failed repo sets error state`() = runTest {
        fakeRepository.shouldSucceed = false

        viewModel.login("test@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertFalse(state.isSuccess)
        assertNotNull(state.error)
    }

    // ============== ERROR CLEARING TESTS ==============

    @Test
    fun `clearError clears error field`() = runTest {
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
        fakeRepository.mockRole = "USER"
        viewModel.login("first@test.com", "Password1!")

        assertTrue(viewModel.state.value.isSuccess)
        assertEquals(UserRole.USER, viewModel.state.value.role)

        fakeRepository.mockRole = "TENANT_ADMIN"
        viewModel.login("second@test.com", "Password2!")

        assertTrue(viewModel.state.value.isSuccess)
        assertEquals(UserRole.TENANT_ADMIN, viewModel.state.value.role)
    }

    @Test
    fun `login with any credentials succeeds when repo succeeds`() = runTest {
        viewModel.login("any@email.com", "anypassword")

        assertTrue(viewModel.state.value.isSuccess)
        assertNotNull(viewModel.state.value.tokens)
    }

    // ============== MFA FLOW TESTS ==============

    @Test
    fun `login with MFA required sets mfa state`() = runTest {
        fakeRepository.mfaRequired = true

        viewModel.login("test@fivucsas.com", "Password123!")

        val state = viewModel.state.value
        assertFalse(state.isSuccess)
        assertNull(state.tokens)
        assertTrue(state.mfaRequired)
        assertNotNull(state.mfaSessionToken)
        assertEquals("fake-mfa-session-token", state.mfaSessionToken)
        assertEquals(1, state.mfaCurrentStep)
        assertEquals(2, state.mfaTotalSteps)
        assertNotNull(state.mfaAvailableMethods)
        assertTrue(state.mfaAvailableMethods!!.isNotEmpty())
    }

    @Test
    fun `login with MFA required does not set loading after completion`() = runTest {
        fakeRepository.mfaRequired = true

        viewModel.login("test@fivucsas.com", "Password123!")

        assertFalse(viewModel.state.value.isLoading)
    }

    // ============== ACTIVE FLOW DISCOVERY TESTS (PR #18 client side) ==============

    @Test
    fun `loadActiveFlow with no flow configured falls back to PASSWORD`() = runTest {
        fakeRepository.mockPrimaryStepMethodType = null

        viewModel.loadActiveFlow(tenantId = "tenant-a")

        val state = viewModel.state.value
        assertFalse(state.flowDiscoveryLoading)
        assertEquals("PASSWORD", state.primaryStepMethod)
        assertTrue(fakeRepository.discoverPrimaryStepCalled)
        assertEquals("APP_LOGIN", fakeRepository.lastDiscoverOperationType)
        assertEquals("tenant-a", fakeRepository.lastDiscoverTenantId)
    }

    @Test
    fun `loadActiveFlow with FACE primary sets primaryStepMethod to FACE`() = runTest {
        fakeRepository.mockPrimaryStepMethodType = "FACE"

        viewModel.loadActiveFlow(tenantId = "tenant-face")

        assertEquals("FACE", viewModel.state.value.primaryStepMethod)
    }

    @Test
    fun `discoverActiveFlow_WhenFlowIsFaceFirst_StateShouldRenderFacePrimary`() = runTest {
        fakeRepository.mockPrimaryStepMethodType = "FACE"

        viewModel.loadActiveFlow(tenantId = "tenant-face")

        assertEquals("FACE", viewModel.state.value.primaryStepMethod)
        assertFalse(viewModel.state.value.flowDiscoveryLoading)
    }

    @Test
    fun `loadActiveFlow with EMAIL_OTP primary sets primaryStepMethod to EMAIL_OTP`() = runTest {
        fakeRepository.mockPrimaryStepMethodType = "EMAIL_OTP"

        viewModel.loadActiveFlow(tenantId = "tenant-otp")

        assertEquals("EMAIL_OTP", viewModel.state.value.primaryStepMethod)
    }

    @Test
    fun `loadActiveFlow with TOTP primary sets primaryStepMethod to TOTP`() = runTest {
        fakeRepository.mockPrimaryStepMethodType = "TOTP"

        viewModel.loadActiveFlow(tenantId = "tenant-totp")

        assertEquals("TOTP", viewModel.state.value.primaryStepMethod)
    }

    @Test
    fun `loadActiveFlow is idempotent — second call does not re-fetch`() = runTest {
        fakeRepository.mockPrimaryStepMethodType = "FACE"
        viewModel.loadActiveFlow(tenantId = "tenant-1")
        assertEquals("FACE", viewModel.state.value.primaryStepMethod)

        // Flip the fake's response — a real second call would observe it.
        fakeRepository.mockPrimaryStepMethodType = "EMAIL_OTP"
        fakeRepository.discoverPrimaryStepCalled = false

        viewModel.loadActiveFlow(tenantId = "tenant-1")

        assertEquals("FACE", viewModel.state.value.primaryStepMethod)
        assertFalse(fakeRepository.discoverPrimaryStepCalled)
    }

    @Test
    fun `loadActiveFlow with null tenantId still calls discover with null`() = runTest {
        fakeRepository.mockPrimaryStepMethodType = null

        viewModel.loadActiveFlow(tenantId = null)

        assertTrue(fakeRepository.discoverPrimaryStepCalled)
        assertEquals(null, fakeRepository.lastDiscoverTenantId)
        assertEquals("PASSWORD", viewModel.state.value.primaryStepMethod)
    }
}

private class FakePushService : IPushNotificationService {
    override suspend fun registerToken(userId: String, token: String) {}
    override suspend fun getToken(): String? = null
    override fun isSupported(): Boolean = false
}

private class InMemorySecureStorage : ISecureStorage {
    private val stringStore = mutableMapOf<String, String>()
    private val boolStore = mutableMapOf<String, Boolean>()
    private val intStore = mutableMapOf<String, Int>()
    private val longStore = mutableMapOf<String, Long>()

    override fun saveString(key: String, value: String) { stringStore[key] = value }
    override fun getString(key: String): String? = stringStore[key]
    override fun saveBoolean(key: String, value: Boolean) { boolStore[key] = value }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = boolStore[key] ?: defaultValue
    override fun saveInt(key: String, value: Int) { intStore[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = intStore[key] ?: defaultValue
    override fun saveLong(key: String, value: Long) { longStore[key] = value }
    override fun getLong(key: String, defaultValue: Long): Long = longStore[key] ?: defaultValue
    override fun remove(key: String) { stringStore.remove(key); boolStore.remove(key); intStore.remove(key); longStore.remove(key) }
    override fun contains(key: String): Boolean = stringStore.containsKey(key) || boolStore.containsKey(key) || intStore.containsKey(key) || longStore.containsKey(key)
    override fun clear() { stringStore.clear(); boolStore.clear(); intStore.clear(); longStore.clear() }
}
