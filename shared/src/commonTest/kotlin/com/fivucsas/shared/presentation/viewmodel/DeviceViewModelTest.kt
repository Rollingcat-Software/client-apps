package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.Device
import com.fivucsas.shared.test.mocks.FakeDeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeDeviceRepository
    private lateinit var viewModel: DeviceViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeDeviceRepository()
        viewModel = DeviceViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be default`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(state.devices.isEmpty())
        assertFalse(state.showRemoveDialog)
    }

    @Test
    fun `loadDevices should populate devices on success`() = runTest {
        viewModel.loadDevices("user-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.devices.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadDevices should set error on failure`() = runTest {
        repository.shouldSucceed = false

        viewModel.loadDevices("user-1")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `loadWebAuthnCredentials should populate credentials`() = runTest {
        viewModel.loadWebAuthnCredentials("user-1")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.webAuthnCredentials.size)
    }

    @Test
    fun `showRemoveDialog should set dialog state`() {
        val device = Device(id = "dev-1", userId = "user-1", deviceName = "Test")

        viewModel.showRemoveDialog(device)

        assertTrue(viewModel.uiState.value.showRemoveDialog)
        assertEquals(device, viewModel.uiState.value.deviceToRemove)
    }

    @Test
    fun `hideRemoveDialog should clear dialog state`() {
        val device = Device(id = "dev-1", userId = "user-1")

        viewModel.showRemoveDialog(device)
        viewModel.hideRemoveDialog()

        assertFalse(viewModel.uiState.value.showRemoveDialog)
        assertNull(viewModel.uiState.value.deviceToRemove)
    }

    @Test
    fun `confirmRemove should remove device and refresh`() = runTest {
        val device = Device(id = "dev-1", userId = "user-1")
        viewModel.showRemoveDialog(device)

        viewModel.confirmRemove("user-1")
        advanceUntilIdle()

        assertEquals("dev-1", repository.removedDeviceId)
        assertNotNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun `confirmRemove should set error on failure`() = runTest {
        val device = Device(id = "dev-1", userId = "user-1")
        viewModel.showRemoveDialog(device)
        repository.shouldSucceed = false

        viewModel.confirmRemove("user-1")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `confirmRemove without device does nothing`() = runTest {
        viewModel.confirmRemove("user-1")
        advanceUntilIdle()

        assertNull(repository.removedDeviceId)
    }

    @Test
    fun `clearMessages should reset all messages`() {
        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
