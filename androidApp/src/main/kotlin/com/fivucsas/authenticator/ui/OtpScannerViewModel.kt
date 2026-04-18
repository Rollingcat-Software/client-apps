package com.fivucsas.authenticator.ui

import androidx.lifecycle.ViewModel
import com.fivucsas.authenticator.totp.OtpQrScanFilter
import com.fivucsas.authenticator.totp.OtpQrScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * OtpScannerViewModel
 *
 * Minimal state holder for the standalone-TOTP QR-scanner screen.
 * CameraX + ML Kit live inside [com.fivucsas.mobile.android.ui.screen.OtpQrScannerScreen];
 * this VM only owns the "did the last detected payload pass our otpauth://
 * denylist + parser?" decision, which is cheap, synchronous, and pure.
 *
 * The scanner screen can use this VM directly, or — as in the current
 * implementation — keep its transient banner state locally and call
 * [OtpQrScanFilter.accept] inline. Having the VM separate makes the
 * scan-decision unit-testable without ML Kit.
 */
class OtpScannerViewModel : ViewModel() {

    private val _state = MutableStateFlow<OtpScannerState>(OtpScannerState.Scanning)
    val state: StateFlow<OtpScannerState> = _state.asStateFlow()

    /**
     * Feed a raw barcode payload through the scan filter and update [state].
     * Returns the same verdict so callers can react inline (navigate, toast, …).
     */
    fun onBarcode(rawValue: String?): OtpQrScanResult {
        val verdict = OtpQrScanFilter.accept(rawValue)
        _state.value = when (verdict) {
            is OtpQrScanResult.Accepted -> OtpScannerState.Success(verdict.uri)
            is OtpQrScanResult.Invalid -> OtpScannerState.ParseError(verdict.reason.name)
        }
        return verdict
    }

    /** Reset back to the scanning state (e.g. after showing an error toast). */
    fun resetToScanning() {
        _state.value = OtpScannerState.Scanning
    }
}

sealed class OtpScannerState {
    data object Scanning : OtpScannerState()
    data class ParseError(val reason: String) : OtpScannerState()
    data class Success(val uri: String) : OtpScannerState()
}
