package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.domain.usecase.admin.GetMyProfileUseCase
import com.fivucsas.shared.domain.usecase.admin.UpdateUserUseCase
import com.fivucsas.shared.platform.INetworkMonitor
import com.fivucsas.shared.platform.ISecureStorage
import com.fivucsas.shared.test.FakeUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userRepository: FakeUserRepository
    private lateinit var getMyProfileUseCase: GetMyProfileUseCase
    private lateinit var updateUserUseCase: UpdateUserUseCase
    private lateinit var offlineCache: OfflineCache
    private lateinit var networkMonitor: ProfileTestNetworkMonitor
    private lateinit var viewModel: UserProfileViewModel

    private val testUser = User(
        id = "user-1",
        name = "John Doe",
        email = "john@fivucsas.com",
        idNumber = "12345678901",
        phoneNumber = "+905551234567",
        status = UserStatus.ACTIVE,
        enrollmentDate = "2025-01-01",
        hasBiometric = true,
        role = UserRole.USER
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = FakeUserRepository()
        userRepository.addUser(testUser)
        getMyProfileUseCase = GetMyProfileUseCase(userRepository)
        updateUserUseCase = UpdateUserUseCase(userRepository)
        offlineCache = OfflineCache(ProfileTestSecureStorage())
        networkMonitor = ProfileTestNetworkMonitor()
        viewModel = UserProfileViewModel(getMyProfileUseCase, updateUserUseCase, offlineCache, networkMonitor)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Initial State ==========

    @Test
    fun `initial state should be default`() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.user)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
        assertFalse(state.isOfflineData)
    }

    // ========== Load Profile (Online) ==========

    @Test
    fun `loadProfile when online should fetch from API and populate state`() = runTest {
        networkMonitor.online = true

        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.user)
        assertEquals("John Doe", state.user?.name)
        assertEquals("john@fivucsas.com", state.user?.email)
        assertFalse(state.isOfflineData)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadProfile when online should cache profile for offline use`() = runTest {
        networkMonitor.online = true

        viewModel.loadProfile()
        advanceUntilIdle()

        // Verify data was cached
        val cached = offlineCache.getCachedProfile()
        assertNotNull(cached)
        assertEquals("user-1", cached.id)
        assertEquals("John Doe", cached.name)
    }

    @Test
    fun `loadProfile when online with API failure should show cached data if available`() = runTest {
        networkMonitor.online = true

        // First load to populate cache
        viewModel.loadProfile()
        advanceUntilIdle()

        // Now make API fail
        userRepository.shouldThrowError = true
        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.user) // Fallback to cached data
        assertTrue(state.isOfflineData)
        assertNotNull(state.errorMessage) // Shows error + cached data note
    }

    @Test
    fun `loadProfile when online with API failure and no cache should show error`() = runTest {
        networkMonitor.online = true
        userRepository.shouldThrowError = true

        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.user)
        assertNotNull(state.errorMessage)
    }

    // ========== Load Profile (Offline) ==========

    @Test
    fun `loadProfile when offline with cached data should show cached profile`() = runTest {
        networkMonitor.online = false

        // Pre-populate cache
        offlineCache.cacheUserProfile(
            id = "user-1",
            name = "John Doe",
            email = "john@fivucsas.com",
            role = "USER",
            status = "ACTIVE"
        )

        viewModel.loadProfile()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.user)
        assertEquals("John Doe", state.user?.name)
        assertTrue(state.isOfflineData)
    }

    @Test
    fun `loadProfile when offline without cached data should show error`() = runTest {
        networkMonitor.online = false

        viewModel.loadProfile()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.user)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("offline", ignoreCase = true) || state.errorMessage!!.contains("internet", ignoreCase = true))
    }

    // ========== Update Profile ==========

    @Test
    fun `updateProfile should update user and show success`() = runTest {
        networkMonitor.online = true
        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.updateProfile("Jane", "Doe", "+905559876543")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.successMessage)
    }

    @Test
    fun `updateProfile with API failure should show error`() = runTest {
        networkMonitor.online = true
        viewModel.loadProfile()
        advanceUntilIdle()

        userRepository.shouldThrowError = true

        viewModel.updateProfile("Jane", "Doe", "+905559876543")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `updateProfile without loaded user should do nothing`() {
        viewModel.updateProfile("Jane", "Doe", "+905559876543")

        // No crash, state unchanged
        assertNull(viewModel.state.value.user)
    }

    // ========== Clear Messages ==========

    @Test
    fun `clearMessages should reset error and success messages`() = runTest {
        networkMonitor.online = true
        viewModel.loadProfile()
        advanceUntilIdle()

        viewModel.updateProfile("Jane", "Doe", "+905559876543")
        advanceUntilIdle()

        viewModel.clearMessages()

        assertNull(viewModel.state.value.errorMessage)
        assertNull(viewModel.state.value.successMessage)
    }
}

// ── Fakes ───────────────────────────────────────────────────────────────────

private class ProfileTestNetworkMonitor : INetworkMonitor {
    var online = true

    override val isOnline: StateFlow<Boolean>
        get() = MutableStateFlow(online)

    override fun checkConnectivity(): Boolean = online
}

private class ProfileTestSecureStorage : ISecureStorage {
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
