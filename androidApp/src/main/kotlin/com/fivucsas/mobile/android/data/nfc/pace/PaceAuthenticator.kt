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
 *     a. Get the encrypted nonce, then AES-decrypt it under the derived nonce
 *        key (the third KDF output of the PACE secret, which is SHA-1 over the
 *        MRZ-derived seed; a CAN may be used instead of the MRZ).
 *     b. Generic Mapping: exchange the ephemeral public keys and map the base
 *        point using the decrypted nonce and the ECDH secret of the mapping keys.
 *     c. Exchange the mapped ephemeral public keys; compute the shared secret.
 *     d. Derive the encryption and MAC session keys (first and second KDF
 *        outputs) and exchange + verify the PCD/PICC authentication tokens
 *        (AES-CMAC).
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
     * Derive the nonce-decryption key `K_π` from the MRZ-derived seed.
     *
     * `π = SHA-1(mrzSeed)`, then `K_π = KDF(π, 3)`. This step is implemented
     * and vector-tested via [PaceKeyDerivation] (ICAO 9303 worked-example
     * vectors). It is the first cryptographic step of the PACE-GM handshake.
     */
    fun derivePasswordKey(mrzSeed: ByteArray): ByteArray {
        val pi = java.security.MessageDigest.getInstance("SHA-1").digest(mrzSeed)
        return PaceKeyDerivation.derivePasswordKey(pi)
    }

    /**
     * Attempt PACE.
     *
     * Implemented + vector-tested today: EF.CardAccess parsing
     * ([CardAccessParser]), protocol selection ([selectProtocol]), and the
     * TR-03110 key derivation ([PaceKeyDerivation] / [derivePasswordKey]).
     *
     * NOT yet implemented (returns [PaceResult.NotImplemented] → BAC fallback):
     * the on-card APDU exchange — MSE:Set AT + the chained GENERAL AUTHENTICATE
     * (encrypted-nonce fetch, GM point mapping, token verify) and the AES
     * secure-messaging channel. That last leg needs a physical PACE document to
     * validate the full handshake (operator-blocked). The integration point is
     * here: derive `K_π`, then drive the GA chain over [isoDep].
     *
     * @param cardAccess raw EF.CardAccess bytes (publicly readable).
     * @param mrzSeed the MRZ-derived seed (or CAN) used to derive `K_π`.
     */
    @Suppress("UNUSED_PARAMETER")
    fun run(isoDep: IsoDep, cardAccess: ByteArray, mrzSeed: ByteArray): PaceResult {
        val infos = CardAccessParser.parse(cardAccess)
        val selected = selectProtocol(infos) ?: return PaceResult.NoPaceAdvertised
        // Key derivation is ready (vector-tested); the on-card GA exchange is the
        // remaining TODO — fall back to BAC until a PACE test card validates it.
        // val kPassword = derivePasswordKey(mrzSeed)  // <- integration point
        return PaceResult.NotImplemented(selected)
    }
}
