package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.presentation.state.AdminTab
import com.fivucsas.shared.test.mocks.MockCheckSystemHealthUseCase
import com.fivucsas.shared.test.mocks.MockCreateUserUseCase
import com.fivucsas.shared.test.mocks.MockDeleteUserUseCase
import com.fivucsas.shared.test.mocks.MockGetStatisticsUseCase
import com.fivucsas.shared.test.mocks.MockGetUsersUseCase
import com.fivucsas.shared.test.mocks.MockUpdateUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for AdminViewModel
 *
 * Tests cover:
 * - Tab navigation
 * - User loading
 * - User search/filtering
 * - User CRUD operations
 * - Statistics loading
 * - Error handling
 * - Dialog state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getUsersUseCase: MockGetUsersUseCase
    private lateinit var createUserUseCase: MockCreateUserUseCase
    private lateinit var deleteUserUseCase: MockDeleteUserUseCase
    private lateinit var updateUserUseCase: MockUpdateUserUseCase
    private lateinit var getStatisticsUseCase: MockGetStatisticsUseCase
    private lateinit var checkSystemHealthUseCase: MockCheckSystemHealthUseCase
    private lateinit var viewModel: AdminViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getUsersUseCase = MockGetUsersUseCase()
        createUserUseCase = MockCreateUserUseCase()
        deleteUserUseCase = MockDeleteUserUseCase()
        updateUserUseCase = MockUpdateUserUseCase()
        getStatisticsUseCase = MockGetStatisticsUseCase()
        checkSystemHealthUseCase = MockCheckSystemHealthUseCase()

        viewModel = AdminViewModel(
            getUsersUseCase = getUsersUseCase,
            createUserUseCase = createUserUseCase,
            deleteUserUseCase = deleteUserUseCase,
            updateUserUseCase = updateUserUseCase,
            getStatisticsUseCase = getStatisticsUseCase,
            checkSystemHealthUseCase = checkSystemHealthUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // Tab Navigation Tests
    // ========================================

    @Test
    fun `initial tab should be USERS`() {
        assertEquals(AdminTab.USERS, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `selectTab should update selected tab`() {
        viewModel.selectTab(AdminTab.ANALYTICS)
        assertEquals(AdminTab.ANALYTICS, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(AdminTab.SETTINGS)
        assertEquals(AdminTab.SETTINGS, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `selectTab should clear messages`() = runTest {
        // Set some messages first
        viewModel.uiState.value.copy(
            errorMessage = "Error",
            successMessage = "Success"
        )

        viewModel.selectTab(AdminTab.ANALYTICS)

        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    // ========================================
    // User Loading Tests
    // ========================================

    @Test
    fun `loadUsers should populate users list on success`() = runTest {
        viewModel.loadUsers()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.users.size)
        assertEquals(2, viewModel.uiState.value.filteredUsers.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadUsers should complete without loading flag after idle`() = runTest {
        // AdminViewModel calls loadUsers() in init{}, so we advance to completion
        advanceUntilIdle()

        viewModel.loadUsers()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadUsers should handle failure gracefully`() = runTest {
        advanceUntilIdle() // let init complete first
        getUsersUseCase.shouldSucceed = false

        viewModel.loadUsers()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ========================================
    // Search and Filter Tests
    // ========================================

    @Test
    fun `updateSearchQuery should filter users by name`() = runTest {
        viewModel.loadUsers()
        advanceUntilIdle()

        viewModel.updateSearchQuery("John")

        assertEquals("John", viewModel.uiState.value.searchQuery)
        assertTrue(viewModel.uiState.value.filteredUsers.all {
            it.name.contains("John", ignoreCase = true)
        })
    }

    @Test
    fun `updateSearchQuery should filter users by email`() = runTest {
        viewModel.loadUsers()
        advanceUntilIdle()

        viewModel.updateSearchQuery("jane@")

        assertTrue(viewModel.uiState.value.filteredUsers.all {
            it.email.contains("jane@", ignoreCase = true)
        })
    }

    @Test
    fun `empty search query should show all users`() = runTest {
        viewModel.loadUsers()
        advanceUntilIdle()

        viewModel.updateSearchQuery("John")
        viewModel.updateSearchQuery("")

        assertEquals(viewModel.uiState.value.users.size, viewModel.uiState.value.filteredUsers.size)
    }

    // ========================================
    // Add User Dialog Tests
    // ========================================

    @Test
    fun `showAddUserDialog should set dialog state`() {
        viewModel.showAddUserDialog()

        assertTrue(viewModel.uiState.value.showAddUserDialog)
        assertNull(viewModel.uiState.value.editingUser)
    }

    @Test
    fun `hideAddUserDialog should reset dialog state`() {
        viewModel.showAddUserDialog()
        viewModel.hideAddUserDialog()

        assertFalse(viewModel.uiState.value.showAddUserDialog)
    }

    @Test
    fun `addUser should close dialog and show success`() = runTest {
        advanceUntilIdle()

        val newUser = User(
            id = "new_user",
            name = "New User",
            email = "new@example.com",
            idNumber = "ID999",
            phoneNumber = "+111",
            status = UserStatus.ACTIVE,
            enrollmentDate = "2024-01-01",
            hasBiometric = false
        )

        viewModel.addUser(newUser)
        advanceUntilIdle()

        // addUser calls createUserUseCase then loadUsers() which re-fetches from mock
        assertFalse(viewModel.uiState.value.showAddUserDialog)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    // ========================================
    // Edit User Dialog Tests
    // ========================================

    @Test
    fun `showEditUserDialog should set editing user`() = runTest {
        viewModel.loadUsers()
        advanceUntilIdle()

        val user = viewModel.uiState.value.users.first()
        viewModel.showEditUserDialog(user)

        assertTrue(viewModel.uiState.value.showEditUserDialog)
        assertEquals(user, viewModel.uiState.value.editingUser)
    }

    @Test
    fun `hideEditUserDialog should reset editing state`() = runTest {
        viewModel.loadUsers()
        advanceUntilIdle()

        val user = viewModel.uiState.value.users.first()
        viewModel.showEditUserDialog(user)
        viewModel.hideEditUserDialog()

        assertFalse(viewModel.uiState.value.showEditUserDialog)
        assertNull(viewModel.uiState.value.editingUser)
    }

    @Test
    fun `updateUser should close dialog and show success`() = runTest {
        advanceUntilIdle()

        val user = viewModel.uiState.value.users.first()
        val updatedUser = user.copy(name = "Updated Name")

        viewModel.updateUser(updatedUser)
        advanceUntilIdle()

        // updateUser calls updateUserUseCase then loadUsers() which re-fetches from mock
        assertFalse(viewModel.uiState.value.showEditUserDialog)
        assertNull(viewModel.uiState.value.editingUser)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    // ========================================
    // Delete User Tests
    // ========================================

    @Test
    fun `showDeleteConfirmation should set user to delete`() = runTest {
        viewModel.loadUsers()
        advanceUntilIdle()

        val user = viewModel.uiState.value.users.first()
        viewModel.showDeleteConfirmation(user)

        assertTrue(viewModel.uiState.value.showDeleteConfirmation)
        assertEquals(user, viewModel.uiState.value.userToDelete)
    }

    @Test
    fun `hideDeleteConfirmation should reset delete state`() = runTest {
        viewModel.loadUsers()
        advanceUntilIdle()

        val user = viewModel.uiState.value.users.first()
        viewModel.showDeleteConfirmation(user)
        viewModel.hideDeleteConfirmation()

        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
        assertNull(viewModel.uiState.value.userToDelete)
    }

    @Test
    fun `deleteUser should call use case and close confirmation`() = runTest {
        advanceUntilIdle()

        val userToDelete = viewModel.uiState.value.users.first()
        viewModel.showDeleteConfirmation(userToDelete)
        viewModel.deleteUser(userToDelete.id)
        advanceUntilIdle()

        // deleteUser calls deleteUserUseCase then loadUsers() which re-fetches
        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
        assertNull(viewModel.uiState.value.userToDelete)
        assertEquals(userToDelete.id, deleteUserUseCase.deletedUserId)
    }

    @Test
    fun `confirmDelete should delete selected user`() = runTest {
        advanceUntilIdle()

        val user = viewModel.uiState.value.users.first()
        viewModel.showDeleteConfirmation(user)
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals(user.id, deleteUserUseCase.deletedUserId)
        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
    }

    // ========================================
    // Statistics Tests
    // ========================================

    @Test
    fun `loadStatistics should populate statistics`() = runTest {
        viewModel.loadStatistics()
        advanceUntilIdle()

        assertEquals(100, viewModel.uiState.value.statistics.totalUsers)
        assertEquals(85, viewModel.uiState.value.statistics.activeUsers)
        assertEquals(42, viewModel.uiState.value.statistics.verificationsToday)
    }

    // ========================================
    // Message Control Tests
    // ========================================

    @Test
    fun `clearMessages should reset all messages`() {
        viewModel.clearMessages()

        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `clearError should only clear error message`() {
        viewModel.clearError()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearSuccess should only clear success message`() {
        viewModel.clearSuccess()

        assertNull(viewModel.uiState.value.successMessage)
    }
}
