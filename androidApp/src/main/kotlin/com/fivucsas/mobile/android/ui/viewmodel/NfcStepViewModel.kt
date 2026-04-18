package com.fivucsas.mobile.android.ui.viewmodel

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.fivucsas.shared.domain.model.MrzInputData
import com.fivucsas.shared.domain.model.NfcIdentityDocumentData
import com.fivucsas.shared.domain.model.NfcReadResult
import com.fivucsas.shared.platform.INfcService
import com.fivucsas.shared.platform.NfcScanState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the NFC MFA step.
 *
 * Wraps [INfcService] and exposes a simplified state machine for the MFA
 * flow. The screen drives the VM through MRZ capture (camera or manual) and
 * hands off to the AndroidNfcService once MRZ data is available; the VM
 * observes [INfcService.scanState] and translates it into UI-friendly states.
 *
 * Analytics / logging uses [Log] (same as other androidApp ViewModels such as
 * MrzAnalyzer, AndroidNfcService).
 */
class NfcStepViewModel(
    private val nfcService: INfcService,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : ViewModel() {

    companion object {
        private const val TAG = "NfcStepViewModel"
    }

    /**
     * UI state machine. The dispatcher drives [Idle] -> [MrzCapture] ->
     * [Scanning] -> [Success] (or [Error]).
     */
    sealed class UiState {
        /** Initial state, nothing scheduled yet. */
        data object Idle : UiState()

        /** Showing MRZ capture UI (camera scanner + manual dialog). */
        data object MrzCapture : UiState()

        /** NFC enabled + waiting for tag tap (or currently reading). */
        data class Scanning(val cardTypeName: String = "Unknown") : UiState()

        /**
         * Card read successfully. [payload] is the data map the MFA
         * dispatcher should send to [MfaFlowViewModel.verifyStep].
         */
        data class Success(
            val document: NfcIdentityDocumentData,
            val payload: Map<String, String>
        ) : UiState()

        /** Unrecoverable (or user-dismissable) error. */
        data class Error(val reason: String, val isRecoverable: Boolean = true) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    /** Reset to idle and tear down any outstanding scan. */
    fun reset() {
        observeJob?.cancel()
        observeJob = null
        nfcService.stopNfcScan()
        nfcService.clearMrzData()
        _uiState.value = UiState.Idle
    }

    /** Called by the screen when the user should start entering / scanning MRZ. */
    fun beginMrzCapture() {
        Log.d(TAG, "beginMrzCapture")
        _uiState.value = UiState.MrzCapture
    }

    /**
     * Kick off the NFC scan with the MRZ data the user just captured.
     * The VM subscribes to [INfcService.scanState] and updates UI state
     * accordingly.
     */
    fun startScanWithMrz(mrz: MrzInputData) {
        Log.i(TAG, "startScanWithMrz docNum=${mrz.documentNumber.take(2)}***")
        if (!mrz.isValid()) {
            _uiState.value = UiState.Error("Invalid MRZ data.", isRecoverable = true)
            return
        }
        if (!nfcService.isNfcAvailable) {
            _uiState.value = UiState.Error(
                "NFC is not available on this device.",
                isRecoverable = false
            )
            return
        }
        if (!nfcService.isNfcEnabled) {
            _uiState.value = UiState.Error(
                "NFC is disabled. Please enable it in device settings.",
                isRecoverable = true
            )
            return
        }

        nfcService.setMrzData(mrz)
        observeScanState()
        nfcService.startNfcScan()
        _uiState.value = UiState.Scanning()
    }

    /** Explicit cancel from UI. */
    fun cancel() {
        Log.d(TAG, "cancel")
        reset()
    }

    private fun observeScanState() {
        observeJob?.cancel()
        observeJob = scope.launch {
            nfcService.scanState.collect { state ->
                handleScanState(state)
            }
        }
    }

    private fun handleScanState(state: NfcScanState) {
        when (state) {
            is NfcScanState.Idle -> {
                // Passive — transitions handled via explicit VM methods.
            }

            is NfcScanState.WaitingForCard -> {
                _uiState.update { UiState.Scanning() }
            }

            is NfcScanState.Reading -> {
                _uiState.update { UiState.Scanning(cardTypeName = state.cardTypeName) }
            }

            is NfcScanState.Completed -> {
                when (val result = state.result) {
                    is NfcReadResult.Success -> {
                        val data = result.cardData
                        if (data is NfcIdentityDocumentData) {
                            Log.i(TAG, "NFC read success uid=${data.uid.take(4)}***")
                            _uiState.value = UiState.Success(
                                document = data,
                                payload = buildPayload(data)
                            )
                        } else {
                            _uiState.value = UiState.Error(
                                "Scanned card is not an identity document.",
                                isRecoverable = true
                            )
                        }
                    }
                    is NfcReadResult.AuthenticationRequired -> {
                        _uiState.value = UiState.Error(result.message, isRecoverable = true)
                    }
                    is NfcReadResult.Failure -> {
                        Log.w(TAG, "NFC read failure: ${result.errorMessage}")
                        _uiState.value = UiState.Error(
                            result.errorMessage,
                            isRecoverable = result.isRecoverable
                        )
                    }
                    NfcReadResult.NfcDisabled -> {
                        _uiState.value = UiState.Error(
                            "NFC is disabled. Please enable it in device settings.",
                            isRecoverable = true
                        )
                    }
                    NfcReadResult.NfcNotAvailable -> {
                        _uiState.value = UiState.Error(
                            "NFC is not available on this device.",
                            isRecoverable = false
                        )
                    }
                }
            }

            is NfcScanState.Error -> {
                _uiState.value = UiState.Error(state.message, state.isRecoverable)
            }
        }
    }

    /**
     * Build the dataMap sent to [MfaFlowViewModel.verifyStep].
     *
     * The backend [NfcDocumentAuthHandler] currently only requires a single
     * `nfcData` field holding the enrolled card serial — see
     * identity-core-api/.../NfcDocumentAuthHandler.java. Until the handler is
     * extended to verify full passport BAC evidence, we pass the card UID as
     * `nfcData` (which enrolment also stores) and include the full BAC
     * payload (doc_number, dob, expiry, dg1_bytes_b64, dg2_bytes_b64) on a
     * best-effort basis so the integration test reveals the real contract.
     *
     * DG1/DG2 raw bytes are not currently exposed by
     * [NfcIdentityDocumentData]; photo bytes are surfaced and attached
     * instead as dg2_photo_b64 so downstream verification can at least
     * compare the chip photo against the enrolled face embedding.
     */
    private fun buildPayload(doc: NfcIdentityDocumentData): Map<String, String> {
        val payload = mutableMapOf<String, String>()
        payload["nfcData"] = doc.uid
        payload["doc_number"] = doc.documentNumber
        payload["dob"] = doc.dateOfBirth
        payload["expiry"] = doc.dateOfExpiry
        payload["surname"] = doc.surname
        payload["given_names"] = doc.givenNames
        payload["nationality"] = doc.nationality
        payload["bac_successful"] = doc.bacSuccessful.toString()
        doc.sodValid?.let { payload["sod_valid"] = it.toString() }
        doc.dg1HashValid?.let { payload["dg1_hash_valid"] = it.toString() }
        doc.dg2HashValid?.let { payload["dg2_hash_valid"] = it.toString() }
        doc.photoBytes?.let { bytes ->
            payload["dg2_photo_b64"] = Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
        return payload
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}
