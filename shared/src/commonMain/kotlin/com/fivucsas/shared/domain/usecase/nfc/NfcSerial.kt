package com.fivucsas.shared.domain.usecase.nfc

/**
 * Normalize an NFC card serial to the API-canonical form agreed with
 * identity-core-api: upper-case hex with NO separators
 * (e.g. `04:a2:24:5b` and `04-A2-24-5B` both → `04A2245B`).
 *
 * Rationale: the server normalizes every inbound serial the same way at
 * ingest, so a card enrolled from mobile matches a web verify and
 * vice-versa. Android's `Tag.getId()` (via `ByteArray.toHexString()`)
 * already produces canonical UPPERHEX — this guards the opaque/generic
 * UID paths and any future caller, and guarantees we never emit
 * separators.
 *
 * Non-hex / opaque serials are upper-cased + trimmed only (separators
 * preserved), matching the server's fallback for non-hex values.
 */
fun normalizeCardSerial(raw: String): String {
    val trimmed = raw.trim()
    val stripped = trimmed.replace(":", "").replace("-", "").replace(".", "").replace(" ", "")
    val isPureHex = stripped.isNotEmpty() && stripped.all { it in "0123456789abcdefABCDEF" }
    return if (isPureHex) stripped.uppercase() else trimmed.uppercase()
}
