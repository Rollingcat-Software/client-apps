package com.fivucsas.shared.platform

import com.fivucsas.shared.domain.model.MrzInputData
import com.fivucsas.shared.domain.model.NfcReadResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * No-op NFC Service implementation.
 *
 * Used as a default when a real NFC service is not available
 * (e.g., before the androidApp module overrides the binding).
 */
class NoOpNfcService : INfcService {
    override val isNfcAvailable: Boolean = false
    override val isNfcEnabled: Boolean = false
    override val scanState: StateFlow<NfcScanState> = MutableStateFlow(NfcScanState.Idle)

    override fun startNfcScan() {
        // No-op
    }

    override fun stopNfcScan() {
        // No-op
    }

    override fun setMrzData(mrzData: MrzInputData) {
        // No-op
    }

    override fun clearMrzData() {
        // No-op
    }
}
