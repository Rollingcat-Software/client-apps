package com.fivucsas.shared.platform

import com.fivucsas.shared.domain.model.MrzInputData
import com.fivucsas.shared.domain.model.NfcReadResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform abstraction for NFC card reading operations.
 *
 * Android: Implemented using UniversalNfcReader (IsoDep, MifareClassic, etc.)
 * iOS: Would use Core NFC framework (future)
 * Desktop: Would use javax.smartcardio (future)
 */
interface INfcService {
    /** Whether NFC hardware is available on this device. */
    val isNfcAvailable: Boolean

    /** Whether NFC is currently enabled in device settings. */
    val isNfcEnabled: Boolean

    /** Current NFC scan state. */
    val scanState: StateFlow<NfcScanState>

    /**
     * Start listening for NFC tags.
     * Call this when the NFC scan screen is visible.
     */
    fun startNfcScan()

    /**
     * Stop listening for NFC tags.
     * Call this when leaving the NFC scan screen.
     */
    fun stopNfcScan()

    /**
     * Provide MRZ data for BAC authentication.
     * Call this before or during a scan when reading identity documents.
     */
    fun setMrzData(mrzData: MrzInputData)

    /**
     * Clear any pending MRZ data.
     */
    fun clearMrzData()
}

/**
 * Represents the current state of an NFC scan operation.
 */
sealed class NfcScanState {
    /** Idle - not scanning. */
    data object Idle : NfcScanState()

    /** Waiting for user to present a card. */
    data object WaitingForCard : NfcScanState()

    /** Card detected, reading in progress. */
    data class Reading(val cardTypeName: String = "Unknown") : NfcScanState()

    /** Read completed with result. */
    data class Completed(val result: NfcReadResult) : NfcScanState()

    /** Error occurred during scan. */
    data class Error(val message: String, val isRecoverable: Boolean = true) : NfcScanState()
}
