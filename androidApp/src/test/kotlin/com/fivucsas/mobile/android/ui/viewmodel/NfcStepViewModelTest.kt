package com.fivucsas.mobile.android.ui.viewmodel

import com.fivucsas.shared.domain.model.MrzInputData
import com.fivucsas.shared.domain.model.NfcIdentityDocumentData
import com.fivucsas.shared.domain.model.NfcReadResult
import com.fivucsas.shared.platform.INfcService
import com.fivucsas.shared.platform.NfcScanState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for [NfcStepViewModel].
 *
 * Style follows the shared-module fake-based pattern (e.g. HardwareTokenViewModelTest)
 * rather than MockK, since MockK is not currently configured for this module and the
 * INfcService interface is small enough that a hand-rolled fake is cheaper.
 *
 * Android.util.Log / Base64 are stubbed via `testOptions.unitTests.isReturnDefaultValues = true`
 * (see build.gradle.kts) so the VM's logging + photo-byte encoding paths don't
 * throw in pure-JVM tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NfcStepViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeService: FakeNfcService
    private lateinit var vm: NfcStepViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeService = FakeNfcService()
        vm = NfcStepViewModel(
            nfcService = fakeService,
            scope = CoroutineScope(testDispatcher)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertIs<NfcStepViewModel.UiState.Idle>(vm.uiState.value)
    }

    @Test
    fun `beginMrzCapture transitions Idle to MrzCapture`() {
        vm.beginMrzCapture()
        assertIs<NfcStepViewModel.UiState.MrzCapture>(vm.uiState.value)
    }

    @Test
    fun `startScanWithMrz rejects invalid MRZ and emits recoverable Error`() = runTest {
        vm.beginMrzCapture()
        val bad = MrzInputData(documentNumber = "", dateOfBirth = "x", dateOfExpiry = "x")
        vm.startScanWithMrz(bad)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertIs<NfcStepViewModel.UiState.Error>(state)
        assertTrue(state.isRecoverable)
    }

    @Test
    fun `full happy path transitions Idle to MrzCapture to Scanning to Success`() = runTest {
        // Idle → MrzCapture
        vm.beginMrzCapture()
        assertIs<NfcStepViewModel.UiState.MrzCapture>(vm.uiState.value)

        // MrzCapture → Scanning (VM subscribes to scanState and emits Scanning)
        val mrz = MrzInputData(
            documentNumber = "A12345678",
            dateOfBirth = "900101",
            dateOfExpiry = "301231"
        )
        vm.startScanWithMrz(mrz)
        advanceUntilIdle()

        assertEquals(mrz, fakeService.lastMrz)
        assertTrue(fakeService.scanStarted, "INfcService.startNfcScan should be called")
        assertIs<NfcStepViewModel.UiState.Scanning>(vm.uiState.value)

        // Simulate Reading (service pushes new state)
        fakeService.emit(NfcScanState.Reading(cardTypeName = "Passport"))
        advanceUntilIdle()
        val scanning = vm.uiState.value
        assertIs<NfcStepViewModel.UiState.Scanning>(scanning)
        assertEquals("Passport", scanning.cardTypeName)

        // Scanning → Success
        val doc = NfcIdentityDocumentData(
            uid = "04A2B1C3",
            cardTypeName = "Passport",
            technologies = listOf("IsoDep"),
            readTimestamp = 0L,
            documentNumber = "A12345678",
            surname = "DOE",
            givenNames = "JOHN",
            dateOfBirth = "900101",
            dateOfExpiry = "301231",
            bacSuccessful = true
        )
        fakeService.emit(NfcScanState.Completed(NfcReadResult.Success(doc)))
        advanceUntilIdle()

        val success = vm.uiState.value
        assertIs<NfcStepViewModel.UiState.Success>(success)
        assertEquals(doc, success.document)
        // Payload keys that MfaFlowViewModel.verifyStep consumes
        assertEquals("04A2B1C3", success.payload["nfcData"])
        assertEquals("A12345678", success.payload["doc_number"])
        assertEquals("900101", success.payload["dob"])
        assertEquals("301231", success.payload["expiry"])
        assertEquals("true", success.payload["bac_successful"])
    }

    @Test
    fun `scan failure maps to recoverable Error`() = runTest {
        vm.startScanWithMrz(
            MrzInputData(
                documentNumber = "A12345678",
                dateOfBirth = "900101",
                dateOfExpiry = "301231"
            )
        )
        advanceUntilIdle()
        fakeService.emit(
            NfcScanState.Completed(
                NfcReadResult.Failure("Tag lost", isRecoverable = true)
            )
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertIs<NfcStepViewModel.UiState.Error>(state)
        assertEquals("Tag lost", state.reason)
        assertTrue(state.isRecoverable)
    }

    @Test
    fun `reset returns VM to Idle and stops scan`() = runTest {
        vm.startScanWithMrz(
            MrzInputData(
                documentNumber = "A12345678",
                dateOfBirth = "900101",
                dateOfExpiry = "301231"
            )
        )
        advanceUntilIdle()
        assertIs<NfcStepViewModel.UiState.Scanning>(vm.uiState.value)

        vm.cancel()
        advanceUntilIdle()
        assertIs<NfcStepViewModel.UiState.Idle>(vm.uiState.value)
        assertTrue(fakeService.scanStopped)
        assertTrue(fakeService.mrzCleared)
    }

    // ── Fake ──

    private class FakeNfcService : INfcService {
        private val _scanState = MutableStateFlow<NfcScanState>(NfcScanState.Idle)
        override val scanState: StateFlow<NfcScanState> = _scanState.asStateFlow()

        override val isNfcAvailable: Boolean = true
        override val isNfcEnabled: Boolean = true

        var lastMrz: MrzInputData? = null
        var scanStarted: Boolean = false
        var scanStopped: Boolean = false
        var mrzCleared: Boolean = false

        fun emit(state: NfcScanState) {
            _scanState.value = state
        }

        override fun startNfcScan() {
            scanStarted = true
            _scanState.value = NfcScanState.WaitingForCard
        }

        override fun stopNfcScan() {
            scanStopped = true
            _scanState.value = NfcScanState.Idle
        }

        override fun setMrzData(mrzData: MrzInputData) {
            lastMrz = mrzData
        }

        override fun clearMrzData() {
            mrzCleared = true
            lastMrz = null
        }
    }
}
