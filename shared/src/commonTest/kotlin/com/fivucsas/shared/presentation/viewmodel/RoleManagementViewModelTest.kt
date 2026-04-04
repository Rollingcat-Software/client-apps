package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.PermissionItem
import com.fivucsas.shared.domain.model.Role
import com.fivucsas.shared.test.mocks.FakeRolesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RoleManagementViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeRolesRepository
    private lateinit var viewModel: RoleManagementViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeRolesRepository()
        viewModel = RoleManagementViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.roles.isEmpty())
        assertFalse(state.showEditDialog)
        assertFalse(state.showDeleteDialog)
    }

    // ========== Load Roles ==========

    @Test
    fun `loadRoles should populate roles on success`() = runTest {
        viewModel.loadRoles()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.roles.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadRoles should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadRoles()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Load Permissions ==========

    @Test
    fun `loadPermissions should populate allPermissions`() = runTest {
        viewModel.loadPermissions()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.allPermissions.size)
    }

    // ========== Create Dialog ==========

    @Test
    fun `showCreateDialog should open dialog with empty fields`() {
        viewModel.showCreateDialog()

        val state = viewModel.uiState.value
        assertTrue(state.showEditDialog)
        assertNull(state.editingRole)
        assertEquals("", state.editName)
        assertEquals("", state.editDescription)
        assertTrue(state.selectedPermissions.isEmpty())
    }

    @Test
    fun `hideEditDialog should close dialog`() {
        viewModel.showCreateDialog()
        viewModel.hideEditDialog()

        assertFalse(viewModel.uiState.value.showEditDialog)
    }

    // ========== Edit Dialog ==========

    @Test
    fun `showEditDialog should populate dialog with role data`() {
        val role = Role(
            id = "role-1", name = "ADMIN", description = "Administrator",
            permissions = listOf(PermissionItem(id = "p-1", name = "users:read"))
        )

        viewModel.showEditDialog(role)

        val state = viewModel.uiState.value
        assertTrue(state.showEditDialog)
        assertEquals(role, state.editingRole)
        assertEquals("ADMIN", state.editName)
        assertEquals("Administrator", state.editDescription)
        assertTrue(state.selectedPermissions.contains("users:read"))
    }

    @Test
    fun `updateEditName should update name`() {
        viewModel.updateEditName("New Role")
        assertEquals("New Role", viewModel.uiState.value.editName)
    }

    @Test
    fun `updateEditDescription should update description`() {
        viewModel.updateEditDescription("A description")
        assertEquals("A description", viewModel.uiState.value.editDescription)
    }

    @Test
    fun `togglePermission should add then remove`() {
        viewModel.togglePermission("users:read")
        assertTrue(viewModel.uiState.value.selectedPermissions.contains("users:read"))

        viewModel.togglePermission("users:read")
        assertFalse(viewModel.uiState.value.selectedPermissions.contains("users:read"))
    }

    // ========== Save Role ==========

    @Test
    fun `saveRole with blank name does nothing`() = runTest {
        viewModel.showCreateDialog()
        viewModel.updateEditName("")

        viewModel.saveRole()
        advanceUntilIdle()

        // Dialog stays open, no role created
        assertNull(repository.createdRole)
    }

    @Test
    fun `saveRole creates new role on success`() = runTest {
        viewModel.showCreateDialog()
        viewModel.updateEditName("Tester")
        viewModel.updateEditDescription("Test role")

        viewModel.saveRole()
        advanceUntilIdle()

        assertNotNull(repository.createdRole)
        assertEquals("Tester", repository.createdRole?.name)
        assertFalse(viewModel.uiState.value.showEditDialog)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `saveRole updates existing role on success`() = runTest {
        val role = repository.mockRoles[0]
        viewModel.showEditDialog(role)
        viewModel.updateEditName("Updated Admin")

        viewModel.saveRole()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showEditDialog)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `saveRole sets error on failure`() = runTest {
        viewModel.showCreateDialog()
        viewModel.updateEditName("Test")
        repository.shouldSucceed = false

        viewModel.saveRole()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Delete ==========

    @Test
    fun `showDeleteDialog should set dialog state`() {
        val role = Role(id = "role-1", name = "TEST")

        viewModel.showDeleteDialog(role)

        assertTrue(viewModel.uiState.value.showDeleteDialog)
        assertEquals(role, viewModel.uiState.value.roleToDelete)
    }

    @Test
    fun `hideDeleteDialog should clear state`() {
        viewModel.showDeleteDialog(Role(id = "role-1", name = "TEST"))
        viewModel.hideDeleteDialog()

        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertNull(viewModel.uiState.value.roleToDelete)
    }

    @Test
    fun `confirmDelete should delete role`() = runTest {
        viewModel.showDeleteDialog(Role(id = "role-1", name = "TEST"))

        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals("role-1", repository.deletedRoleId)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `confirmDelete without role does nothing`() = runTest {
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertNull(repository.deletedRoleId)
    }

    @Test
    fun `clearMessages should reset messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
