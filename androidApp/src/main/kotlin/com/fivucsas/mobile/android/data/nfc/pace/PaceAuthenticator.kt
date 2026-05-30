package com.fivucsas.mobile.android.data.nfc.pace

import android.nfc.tech.IsoDep
import com.fivucsas.mobile.android.data.nfc.eid.SecureMessaging
import com.fivucsas.mobile.android.data.nfc.pace.CardAccessParser.PaceInfo

/**
 * PACE (Password Authenticated Connection Establishment, BSI TR-03110 /
 * ICAO 9303-11) — Generic Mapping with AES secure messaging.
 *
 * STATUS: **scaffold, not yet implemented.** The protocol selection
 * (EF.CardAccess → [PaceInfo]) and the integration seam are in place, but the
 * cryptographic core (GM ECDH key agreement + AES-CMAC secure messaging
 * derivation) is intentionally left unimplemented until we have:
 *   - a PACE-capable physical test document, and
 *   - the BSI TR-03110 worked-example reference vectors
 * to validate against. Shipping un-vetted ECC/CMAC crypto that silently
 * produces a wrong session would be worse than failing loudly, so [run]
 * returns [PaceResult.NotImplemented] for now and the readers fall back to BAC.
 *
 * ── Protocol outline (for the implementor) ─────────────────────────────────
 *  1. Read EF.CardAccess (public), pick a [PaceInfo] (prefer GM + AES-128).
 *  2. MSE:Set AT — select the PACE protocol OID + key reference (MRZ/CAN).
 *  3. GENERAL AUTHENTICATE (chained):
 *     a. Get the encrypted nonce `z`; derive `s = AES-Dec(K_π, z)` where
 *        `K_π = KDF(password, 3)` (password = SHA-1 over the MRZ-derived seed).
 *     b. Generic Mapping: exchange ephemeral PK; map the base point
 *        `G' = s·G + H` where `H` is the shared ECDH secret of the mapping keys.
 *     c. Exchange the mapped ephemeral PKs; compute shared secret `K`.
 *     d. Derive `K_enc = KDF(K,1)`, `K_mac = KDF(K,2)`; exchange + verify the
 *        authentication tokens `T_PCD`/`T_PICC` (AES-CMAC).
 *  4. Open AES secure messaging (SSC starts at 0) and return it as a
 *     [SecureMessaging] so the existing DG-read path is unchanged.
 *
 * Until implemented, callers should treat [PaceResult.NotImplemented] as "fall
 * back to BAC".
 */
class PaceAuthenticator {

    sealed interface PaceResult {
        /** PACE completed; [secureMessaging] is ready for DG reads. */
        data class Success(val secureMessaging: SecureMessaging) : PaceResult
        /** The chip advertised no usable PACE protocol in EF.CardAccess. */
        data object NoPaceAdvertised : PaceResult
        /** PACE is advertised but not yet implemented — caller should use BAC. */
        data class NotImplemented(val selected: PaceInfo) : PaceResult
        /** PACE attempted and failed. */
        data class Failure(val message: String) : PaceResult
    }

    /**
     * Select a PACE protocol from the parsed EF.CardAccess entries.
     * Prefers Generic Mapping with AES, then the first available entry.
     */
    fun selectProtocol(infos: List<PaceInfo>): PaceInfo? =
        infos.firstOrNull { it.isGenericMapping && it.isAes }
            ?: infos.firstOrNull { it.isGenericMapping }
            ?: infos.firstOrNull()

    /**
     * Attempt PACE. Currently a guarded no-op: it parses EF.CardAccess and
     * selects a protocol, but returns [PaceResult.NotImplemented] (or
     * [PaceResult.NoPaceAdvertised]) rather than running un-vetted crypto.
     *
     * @param cardAccess raw EF.CardAccess bytes (publicly readable).
     */
    @Suppress("UNUSED_PARAMETER")
    fun run(isoDep: IsoDep, cardAccess: ByteArray, mrzSeed: ByteArray): PaceResult {
        val infos = CardAccessParser.parse(cardAccess)
        val selected = selectProtocol(infos) ?: return PaceResult.NoPaceAdvertised
        return PaceResult.NotImplemented(selected)
    }
}
