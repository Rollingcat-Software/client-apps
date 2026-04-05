package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.AuditLogEntry
import com.fivucsas.shared.domain.model.CapabilityPolicy
import com.fivucsas.shared.domain.model.GlobalUser
import com.fivucsas.shared.domain.model.RolePermissionMatrix
import com.fivucsas.shared.domain.model.RootFilter
import com.fivucsas.shared.domain.model.RootSystemSettings
import com.fivucsas.shared.domain.model.SecurityEvent
import com.fivucsas.shared.domain.model.TenantDetail
import com.fivucsas.shared.domain.model.TenantSummary
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.RootAdminRepository
import com.fivucsas.shared.presentation.state.RootConsoleUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RootConsoleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeRootAdminRepository
    private lateinit var viewModel: RootConsoleViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeRootAdminRepository()
        viewModel = RootConsoleViewModel(UserRole.ROOT, repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Initial State ==========

    @Test
    fun `initial state should have ROOT capabilities`() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(CapabilityPolicy.rootCapabilities, state.capabilities)
        assertTrue(state.tenants.isEmpty())
        assertTrue(state.users.isEmpty())
    }

    @Test
    fun `TENANT_ADMIN role should have tenant admin capabilities`() {
        val vm = RootConsoleViewModel(UserRole.TENANT_ADMIN, repository)
        assertEquals(CapabilityPolicy.tenantAdminCapabilities, vm.state.value.capabilities)
    }

    // ========== Load ==========

    @Test
    fun `Load event should populate tenants, users, audit logs, etc`() = runTest {
        viewModel.onEvent(RootConsoleUiEvent.Load(null))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(2, state.tenants.size)
        assertEquals(1, state.users.size)
        assertNotNull(state.settings)
    }

    @Test
    fun `Load event with tenantId should set selectedTenantId`() = runTest {
        viewModel.onEvent(RootConsoleUiEvent.Load("tenant-1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("tenant-1", state.selectedTenantId)
    }

    @Test
    fun `Load event should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.onEvent(RootConsoleUiEvent.Load(null))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    // ========== Select Tenant ==========

    @Test
    fun `SelectTenant event should reload with tenantId`() = runTest {
        viewModel.onEvent(RootConsoleUiEvent.SelectTenant("tenant-2"))
        advanceUntilIdle()

        assertEquals("tenant-2", viewModel.state.value.selectedTenantId)
    }

    // ========== Toggle User ==========

    @Test
    fun `ToggleUserEnabled event should call updateUser`() = runTest {
        viewModel.onEvent(RootConsoleUiEvent.Load(null))
        advanceUntilIdle()

        viewModel.onEvent(RootConsoleUiEvent.ToggleUserEnabled("user-1", false))
        advanceUntilIdle()

        assertTrue(repository.lastUpdateUserId == "user-1")
    }

    @Test
    fun `ToggleUserEnabled should set error on failure`() = runTest {
        repository.shouldSucceedToggle = false

        viewModel.onEvent(RootConsoleUiEvent.ToggleUserEnabled("user-1", false))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
    }

    // ========== Delete Tenant ==========

    @Test
    fun `DeleteTenant event should call deleteTenant and reload`() = runTest {
        viewModel.onEvent(RootConsoleUiEvent.Load(null))
        advanceUntilIdle()

        viewModel.onEvent(RootConsoleUiEvent.DeleteTenant("tenant-1"))
        advanceUntilIdle()

        assertTrue(repository.deletedTenantIds.contains("tenant-1"))
    }

    // ========== Delete User ==========

    @Test
    fun `DeleteUser event should call deleteUser`() = runTest {
        viewModel.onEvent(RootConsoleUiEvent.DeleteUser("user-1"))
        advanceUntilIdle()

        assertTrue(repository.deletedUserIds.contains("user-1"))
    }

    // ========== Cancel Impersonation ==========

    @Test
    fun `CancelImpersonation should hide confirmation`() {
        viewModel.onEvent(RootConsoleUiEvent.CancelImpersonation)
        assertFalse(viewModel.state.value.showImpersonationConfirm)
    }

    // ========== Error Mapping ==========

    @Test
    fun `401 error should map to session expired`() = runTest {
        repository.shouldSucceed = false
        repository.errorMessage = "401"

        viewModel.onEvent(RootConsoleUiEvent.Load(null))
        advanceUntilIdle()

        assertEquals("Session expired (401).", viewModel.state.value.errorMessage)
    }

    @Test
    fun `403 error should map to operation denied`() = runTest {
        repository.shouldSucceed = false
        repository.errorMessage = "403"

        viewModel.onEvent(RootConsoleUiEvent.Load(null))
        advanceUntilIdle()

        assertEquals("Operation denied by backend (403).", viewModel.state.value.errorMessage)
    }

    // ========== Apply System Settings ==========

    @Test
    fun `applySystemSettings should update settings on success`() = runTest {
        viewModel.onEvent(RootConsoleUiEvent.Load(null))
        advanceUntilIdle()

        viewModel.applySystemSettings("JWT 1h", 120, "8+ chars")
        advanceUntilIdle()

        val settings = viewModel.state.value.settings
        assertNotNull(settings)
        assertEquals("JWT 1h", settings.jwtPolicySummary)
        assertEquals(120, settings.defaultRateLimitPerMinute)
    }

    // ========== rootInitialFilter ==========

    @Test
    fun `rootInitialFilter should return filter with empty query`() {
        val filter = rootInitialFilter()
        assertEquals("", filter.query)
        assertNull(filter.tenantId)
    }

    @Test
    fun `rootInitialFilter with query should return filter with query`() {
        val filter = rootInitialFilter("search term")
        assertEquals("search term", filter.query)
    }
}

// ── Fake ────────────────────────────────────────────────────────────────────

private class FakeRootAdminRepository : RootAdminRepository {
    var shouldSucceed = true
    var shouldSucceedToggle = true
    var errorMessage = "Test error"
    var lastUpdateUserId: String? = null
    val deletedTenantIds = mutableListOf<String>()
    val deletedUserIds = mutableListOf<String>()

    private val mockTenants = listOf(
        TenantSummary("tenant-1", "Acme Corp", "ACTIVE", 1, 5, 100, 10),
        TenantSummary("tenant-2", "Beta Inc", "ACTIVE", 1, 3, 50, 5)
    )

    private val mockUsers = listOf(
        GlobalUser("user-1", "tenant-1", "John Doe", "john@acme.com", "TENANT_ADMIN", true)
    )

    private val mockSettings = RootSystemSettings(
        jwtPolicySummary = "HS256, 1h expiry",
        defaultRateLimitPerMinute = 60,
        passwordPolicySummary = "8+ chars, 1 upper, 1 digit"
    )

    override suspend fun getTenants(filter: RootFilter): Result<List<TenantSummary>> =
        if (shouldSucceed) Result.success(mockTenants) else Result.failure(RuntimeException(errorMessage))

    override suspend fun getTenantDetail(tenantId: String): Result<TenantDetail> =
        Result.success(TenantDetail(mockTenants[0], mockUsers, emptyList(), emptyMap(), emptyMap()))

    override suspend fun createTenant(tenant: TenantSummary): Result<TenantSummary> =
        Result.success(tenant)

    override suspend fun updateTenant(tenantId: String, tenant: TenantSummary): Result<TenantSummary> =
        Result.success(tenant)

    override suspend fun deleteTenant(tenantId: String): Result<Unit> {
        deletedTenantIds.add(tenantId)
        return if (shouldSucceed) Result.success(Unit) else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun getUsers(filter: RootFilter): Result<List<GlobalUser>> =
        if (shouldSucceed) Result.success(mockUsers) else Result.failure(RuntimeException(errorMessage))

    override suspend fun updateUser(userId: String, enabled: Boolean): Result<GlobalUser> {
        lastUpdateUserId = userId
        return if (shouldSucceedToggle) Result.success(mockUsers[0].copy(enabled = enabled))
        else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun updateUserProfile(
        userId: String, fullName: String, email: String, role: String, tenantId: String?
    ): Result<GlobalUser> =
        Result.success(mockUsers[0].copy(fullName = fullName, email = email, role = role))

    override suspend fun deleteUser(userId: String): Result<Unit> {
        deletedUserIds.add(userId)
        return if (shouldSucceed) Result.success(Unit) else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun getTenantAdmins(filter: RootFilter): Result<List<GlobalUser>> =
        if (shouldSucceed) Result.success(mockUsers) else Result.failure(RuntimeException(errorMessage))

    override suspend fun assignTenantAdmin(userId: String, tenantId: String): Result<Unit> = Result.success(Unit)
    override suspend fun unassignTenantAdmin(userId: String, tenantId: String): Result<Unit> = Result.success(Unit)
    override suspend fun resetAdminPassword(userId: String): Result<Unit> = Result.success(Unit)

    override suspend fun getRolesAndPermissions(): Result<List<RolePermissionMatrix>> = Result.success(emptyList())
    override suspend fun updateRolePermissions(role: String, permissions: Set<String>): Result<Unit> = Result.success(Unit)

    override suspend fun getAuditLogs(filter: RootFilter): Result<List<AuditLogEntry>> =
        if (shouldSucceed) Result.success(emptyList()) else Result.failure(RuntimeException(errorMessage))

    override suspend fun exportAuditLogs(filter: RootFilter): Result<String> = Result.success("csv-data")

    override suspend fun getSecurityEvents(filter: RootFilter): Result<List<SecurityEvent>> =
        if (shouldSucceed) Result.success(emptyList()) else Result.failure(RuntimeException(errorMessage))

    override suspend fun getSystemSettings(): Result<RootSystemSettings> =
        if (shouldSucceed) Result.success(mockSettings) else Result.failure(RuntimeException(errorMessage))

    override suspend fun updateSystemSettings(settings: RootSystemSettings): Result<RootSystemSettings> =
        if (shouldSucceed) Result.success(settings) else Result.failure(RuntimeException(errorMessage))
}
