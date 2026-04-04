package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.OAuth2Client
import com.fivucsas.shared.test.mocks.FakeOAuth2ClientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class DeveloperPortalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeOAuth2ClientRepository
    private lateinit var viewModel: DeveloperPortalViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeOAuth2ClientRepository()
        viewModel = DeveloperPortalViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.apps.isEmpty())
        assertFalse(state.showRegisterDialog)
        assertFalse(state.showDeleteDialog)
    }

    // ========== Load Apps ==========

    @Test
    fun `loadApps should populate apps on success`() = runTest {
        viewModel.loadApps()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.apps.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadApps should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadApps()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Register Dialog ==========

    @Test
    fun `showRegisterDialog should open with defaults`() {
        viewModel.showRegisterDialog()

        val state = viewModel.uiState.value
        assertTrue(state.showRegisterDialog)
        assertEquals("", state.registerAppName)
        assertEquals("", state.registerRedirectUris)
        assertTrue(state.registerScopes.contains("openid"))
    }

    @Test
    fun `hideRegisterDialog should close dialog`() {
        viewModel.showRegisterDialog()
        viewModel.hideRegisterDialog()

        assertFalse(viewModel.uiState.value.showRegisterDialog)
    }

    @Test
    fun `updateAppName should update state`() {
        viewModel.updateAppName("My App")
        assertEquals("My App", viewModel.uiState.value.registerAppName)
    }

    @Test
    fun `updateRedirectUris should update state`() {
        viewModel.updateRedirectUris("http://localhost:3000")
        assertEquals("http://localhost:3000", viewModel.uiState.value.registerRedirectUris)
    }

    @Test
    fun `toggleScope should add and remove scopes`() {
        viewModel.showRegisterDialog() // initializes with ["openid"]

        viewModel.toggleScope("profile")
        assertTrue(viewModel.uiState.value.registerScopes.contains("profile"))

        viewModel.toggleScope("profile")
        assertFalse(viewModel.uiState.value.registerScopes.contains("profile"))
    }

    // ========== Register App ==========

    @Test
    fun `registerApp with blank name does nothing`() = runTest {
        viewModel.showRegisterDialog()
        viewModel.updateAppName("")
        viewModel.updateRedirectUris("http://localhost")

        viewModel.registerApp()
        advanceUntilIdle()

        // Dialog stays open
        assertTrue(viewModel.uiState.value.showRegisterDialog)
    }

    @Test
    fun `registerApp should create app on success`() = runTest {
        viewModel.showRegisterDialog()
        viewModel.updateAppName("My App")
        viewModel.updateRedirectUris("http://localhost:3000/callback")

        viewModel.registerApp()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showRegisterDialog)
        assertNotNull(viewModel.uiState.value.createdApp)
        assertTrue(viewModel.uiState.value.apps.isNotEmpty())
    }

    @Test
    fun `registerApp should set error on failure`() = runTest {
        repository.shouldSucceed = false
        viewModel.showRegisterDialog()
        viewModel.updateAppName("My App")
        viewModel.updateRedirectUris("http://localhost")

        viewModel.registerApp()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    // ========== Credentials Dialog ==========

    @Test
    fun `dismissCredentials should clear createdApp`() {
        viewModel.dismissCredentials()
        assertNull(viewModel.uiState.value.createdApp)
    }

    // ========== Delete ==========

    @Test
    fun `showDeleteDialog should set dialog state`() {
        val app = OAuth2Client(id = "app-1", appName = "Test", clientId = "c-1", redirectUris = emptyList(), scopes = emptyList(), status = "active", createdAt = "")

        viewModel.showDeleteDialog(app)

        assertTrue(viewModel.uiState.value.showDeleteDialog)
        assertEquals(app, viewModel.uiState.value.appToDelete)
    }

    @Test
    fun `hideDeleteDialog should clear state`() {
        val app = OAuth2Client(id = "app-1", appName = "Test", clientId = "c-1", redirectUris = emptyList(), scopes = emptyList(), status = "active", createdAt = "")

        viewModel.showDeleteDialog(app)
        viewModel.hideDeleteDialog()

        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertNull(viewModel.uiState.value.appToDelete)
    }

    @Test
    fun `confirmDelete should delete app`() = runTest {
        viewModel.loadApps()
        advanceUntilIdle()

        val app = viewModel.uiState.value.apps.first()
        viewModel.showDeleteDialog(app)

        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals(app.id, repository.deletedClientId)
        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `confirmDelete without app does nothing`() = runTest {
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertNull(repository.deletedClientId)
    }

    // ========== Clipboard ==========

    @Test
    fun `setCopiedLabel should update state`() {
        viewModel.setCopiedLabel("Client ID")
        assertEquals("Client ID", viewModel.uiState.value.copiedLabel)

        viewModel.setCopiedLabel(null)
        assertNull(viewModel.uiState.value.copiedLabel)
    }

    @Test
    fun `clearMessages should reset messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
