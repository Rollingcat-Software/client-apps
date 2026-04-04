package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.usecase.tenant.GetTenantSettingsUseCase
import com.fivucsas.shared.domain.usecase.tenant.UpdateTenantSettingsUseCase
import com.fivucsas.shared.test.mocks.FakeTenantSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class TenantSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTenantSettingsRepository
    private lateinit var viewModel: TenantSettingsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTenantSettingsRepository()
        viewModel = TenantSettingsViewModel(
            getTenantSettingsUseCase = GetTenantSettingsUseCase(repository),
            updateTenantSettingsUseCase = UpdateTenantSettingsUseCase(repository)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.hasUnsavedChanges)
        assertNull(state.errorMessage)
    }

    // ========== Load Settings ==========

    @Test
    fun `loadSettings should populate state on success`() = runTest {
        viewModel.loadSettings()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Test Tenant", state.tenantName)
        assertTrue(state.livenessCheckEnabled)
        assertEquals(0.85f, state.confidenceThreshold)
        assertEquals(3, state.maxEnrollmentAttempts)
        assertEquals(30, state.sessionTimeoutMinutes)
    }

    @Test
    fun `loadSettings should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadSettings()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
    }

    // ========== Setting Updates ==========

    @Test
    fun `setLivenessCheck should update and mark unsaved`() {
        viewModel.setLivenessCheck(false)

        assertFalse(viewModel.state.value.livenessCheckEnabled)
        assertTrue(viewModel.state.value.hasUnsavedChanges)
    }

    @Test
    fun `setConfidenceThreshold should update and mark unsaved`() {
        viewModel.setConfidenceThreshold(0.9f)

        assertEquals(0.9f, viewModel.state.value.confidenceThreshold)
        assertTrue(viewModel.state.value.hasUnsavedChanges)
    }

    @Test
    fun `setMaxEnrollmentAttempts should update and mark unsaved`() {
        viewModel.setMaxEnrollmentAttempts(5)

        assertEquals(5, viewModel.state.value.maxEnrollmentAttempts)
        assertTrue(viewModel.state.value.hasUnsavedChanges)
    }

    @Test
    fun `setSessionTimeout should update and mark unsaved`() {
        viewModel.setSessionTimeout(60)

        assertEquals(60, viewModel.state.value.sessionTimeoutMinutes)
        assertTrue(viewModel.state.value.hasUnsavedChanges)
    }

    @Test
    fun `setAutoLock should update and mark unsaved`() {
        viewModel.setAutoLock(true)

        assertTrue(viewModel.state.value.autoLockEnabled)
        assertTrue(viewModel.state.value.hasUnsavedChanges)
    }

    @Test
    fun `setNfcExamEntry should update and mark unsaved`() {
        viewModel.setNfcExamEntry(true)

        assertTrue(viewModel.state.value.nfcExamEntryEnabled)
        assertTrue(viewModel.state.value.hasUnsavedChanges)
    }

    @Test
    fun `setInviteExpiryDays should update and mark unsaved`() {
        viewModel.setInviteExpiryDays(14)

        assertEquals(14, viewModel.state.value.inviteExpiryDays)
        assertTrue(viewModel.state.value.hasUnsavedChanges)
    }

    // ========== Save Settings ==========

    @Test
    fun `saveSettings should save and clear unsaved flag`() = runTest {
        viewModel.loadSettings()
        advanceUntilIdle()

        viewModel.setLivenessCheck(false)
        viewModel.saveSettings()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.hasUnsavedChanges)
        assertNotNull(state.successMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `saveSettings should set error on failure`() = runTest {
        viewModel.loadSettings()
        advanceUntilIdle()

        repository.shouldSucceed = false
        viewModel.saveSettings()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
    }

    // ========== Utility ==========

    @Test
    fun `clearMessages should reset messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.state.value.successMessage)
        assertNull(viewModel.state.value.errorMessage)
    }
}
