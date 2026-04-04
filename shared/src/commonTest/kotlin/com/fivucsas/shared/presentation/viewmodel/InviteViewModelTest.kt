package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.InviteStatus
import com.fivucsas.shared.domain.usecase.invite.CreateInviteUseCase
import com.fivucsas.shared.domain.usecase.invite.GetInvitesUseCase
import com.fivucsas.shared.domain.usecase.invite.RevokeInviteUseCase
import com.fivucsas.shared.test.mocks.FakeInviteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class InviteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeInviteRepository
    private lateinit var viewModel: InviteViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeInviteRepository()
        viewModel = InviteViewModel(
            getInvitesUseCase = GetInvitesUseCase(repository),
            createInviteUseCase = CreateInviteUseCase(repository),
            revokeInviteUseCase = RevokeInviteUseCase(repository)
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
        assertTrue(state.invites.isEmpty())
        assertFalse(state.showCreateDialog)
        assertNull(state.errorMessage)
    }

    // ========== Load Invites ==========

    @Test
    fun `loadInvites should populate invites on success`() = runTest {
        viewModel.loadInvites()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.invites.size)
        assertEquals(2, state.filteredInvites.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadInvites should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadInvites()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
    }

    // ========== Search and Filter ==========

    @Test
    fun `updateSearch should filter by email`() = runTest {
        viewModel.loadInvites()
        advanceUntilIdle()

        viewModel.updateSearch("admin@")

        assertTrue(viewModel.state.value.filteredInvites.all {
            it.email.contains("admin@", ignoreCase = true)
        })
    }

    @Test
    fun `setFilter should filter by status`() = runTest {
        viewModel.loadInvites()
        advanceUntilIdle()

        viewModel.setFilter(InviteStatus.PENDING)

        assertTrue(viewModel.state.value.filteredInvites.all { it.status == InviteStatus.PENDING })
    }

    @Test
    fun `setFilter null should show all`() = runTest {
        viewModel.loadInvites()
        advanceUntilIdle()

        viewModel.setFilter(InviteStatus.PENDING)
        viewModel.setFilter(null)

        assertEquals(2, viewModel.state.value.filteredInvites.size)
    }

    @Test
    fun `setTenantFilter should filter by tenant`() = runTest {
        viewModel.loadInvites()
        advanceUntilIdle()

        viewModel.setTenantFilter("t-1")

        assertTrue(viewModel.state.value.filteredInvites.all { it.tenantId == "t-1" })
    }

    // ========== Create Dialog ==========

    @Test
    fun `showCreateDialog should set dialog state`() {
        viewModel.showCreateDialog()
        assertTrue(viewModel.state.value.showCreateDialog)
    }

    @Test
    fun `hideCreateDialog should clear dialog state`() {
        viewModel.showCreateDialog()
        viewModel.hideCreateDialog()
        assertFalse(viewModel.state.value.showCreateDialog)
    }

    // ========== Create Invite ==========

    @Test
    fun `createInvite should close dialog and show success`() = runTest {
        viewModel.createInvite(email = "new@test.com", role = "USER", tenantId = "t-1")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.showCreateDialog)
        assertNotNull(state.successMessage)
        assertNotNull(repository.createdInvite)
    }

    @Test
    fun `createInvite should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.createInvite(email = "new@test.com", role = "USER")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
    }

    // ========== Revoke ==========

    @Test
    fun `revokeInvite should show success`() = runTest {
        viewModel.revokeInvite("inv-1")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.successMessage)
        assertEquals("inv-1", repository.revokedInviteId)
    }

    @Test
    fun `revokeInvite should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.revokeInvite("inv-1")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
    }

    // ========== Utility ==========

    @Test
    fun `clearMessages should reset messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.state.value.errorMessage)
        assertNull(viewModel.state.value.successMessage)
    }
}
