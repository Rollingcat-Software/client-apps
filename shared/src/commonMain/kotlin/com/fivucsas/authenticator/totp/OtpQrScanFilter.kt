package com.fivucsas.authenticator.totp

/**
 * Pure, testable gate between a raw barcode payload and the TOTP vault.
 *
 * The camera scanner (Android `OtpQrScannerScreen`) feeds every detected QR
 * raw-value through [accept] so that:
 *
 * 1. **Scheme denylist** — anything that is not an `otpauth://` URI is rejected
 *    immediately. This prevents users from accidentally adding a website URL,
 *    Wi-Fi payload, vCard, etc. as a fake TOTP account.
 * 2. **Structural validation** — the payload is fed through [OtpauthUri.parse],
 *    which throws [IllegalArgumentException] for any malformed, non-TOTP, or
 *    unsupported otpauth URI. These throws are converted into
 *    [OtpQrScanResult.Invalid] so the UI can show a single error state.
 *
 * Kept in `commonMain` (not in androidApp) so the JVM test runner can exercise
 * the parser-integration path without pulling in CameraX / ML Kit.
 */
object OtpQrScanFilter {

    /** Prefix every accepted payload MUST start with (case-sensitive per RFC). */
    const val OTPAUTH_SCHEME: String = "otpauth://"

    /**
     * Validates [rawValue] and returns either an [OtpQrScanResult.Accepted]
     * carrying the trimmed URI and parsed [OtpauthConfig], or
     * [OtpQrScanResult.Invalid] with a short reason code.
     */
    fun accept(rawValue: String?): OtpQrScanResult {
        if (rawValue.isNullOrBlank()) {
            return OtpQrScanResult.Invalid(OtpQrScanReason.EMPTY)
        }
        val trimmed = rawValue.trim()
        if (!trimmed.startsWith(OTPAUTH_SCHEME)) {
            return OtpQrScanResult.Invalid(OtpQrScanReason.WRONG_SCHEME)
        }
        val cfg = runCatching { OtpauthUri.parse(trimmed) }.getOrElse {
            return OtpQrScanResult.Invalid(OtpQrScanReason.UNPARSEABLE)
        }
        return OtpQrScanResult.Accepted(uri = trimmed, config = cfg)
    }
}

sealed class OtpQrScanResult {
    data class Accepted(val uri: String, val config: OtpauthConfig) : OtpQrScanResult()
    data class Invalid(val reason: OtpQrScanReason) : OtpQrScanResult()
}

enum class OtpQrScanReason {
    EMPTY,
    WRONG_SCHEME,
    UNPARSEABLE,
}
