package com.fivucsas.mobile.android.data.nfc.pace

import com.fivucsas.mobile.android.data.nfc.security.SecureLogger
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import java.io.ByteArrayInputStream

/**
 * Parses `EF.CardAccess` (ICAO 9303 part 11, BSI TR-03110) — the publicly
 * readable file that advertises which access-control protocols a chip
 * supports, including **PACE**.
 *
 * `EF.CardAccess ::= SET OF SecurityInfo`, where each `SecurityInfo` is a
 * `SEQUENCE { protocol OBJECT IDENTIFIER, requiredData ANY, optionalData ANY OPTIONAL }`.
 * For a `PACEInfo` the protocol OID encodes the mapping + cipher + key length,
 * `requiredData` is the version (INTEGER, must be 2), and `optionalData` is the
 * standardized-domain-parameter id (INTEGER).
 *
 * This parser only needs BouncyCastle ASN.1 (already a dependency) and runs
 * with NO card — so it is fully unit-testable from a captured EF.CardAccess
 * byte string.
 */
object CardAccessParser {

    private const val TAG = "CardAccessParser"

    /** PACE protocol OID prefix per BSI TR-03110: `0.4.0.127.0.7.2.2.4`. */
    private const val PACE_OID_PREFIX = "0.4.0.127.0.7.2.2.4"

    /**
     * One PACE protocol entry from EF.CardAccess.
     *
     * @param protocolOid full OID, e.g. `0.4.0.127.0.7.2.2.4.2.4`
     *        (id-PACE-ECDH-GM-AES-CBC-CMAC-128).
     * @param version PACEInfo version (expected 2).
     * @param parameterId standardized domain parameter id (e.g. 13 = NIST P-256),
     *        or null when not present.
     */
    data class PaceInfo(
        val protocolOid: String,
        val version: Int,
        val parameterId: Int?
    ) {
        val isGenericMapping: Boolean get() = protocolOid.startsWith("$PACE_OID_PREFIX.2")
        val isAes: Boolean get() = protocolOid.contains(".4") // …-AES-… families
    }

    /**
     * @return the PACE entries advertised by the chip, or an empty list when
     *         EF.CardAccess is absent / unparsable / advertises no PACE.
     */
    fun parse(cardAccess: ByteArray): List<PaceInfo> {
        if (cardAccess.isEmpty()) return emptyList()
        return try {
            ASN1InputStream(ByteArrayInputStream(cardAccess)).use { ais ->
                val top = ais.readObject()
                val securityInfos = when (top) {
                    is ASN1Set -> top.objects.toList()
                    is ASN1Sequence -> top.objects.toList() // some chips wrap loosely
                    else -> emptyList()
                }
                securityInfos.mapNotNull { obj ->
                    (obj as? ASN1Sequence)?.let { parseSecurityInfo(it) }
                }
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Failed to parse EF.CardAccess", e)
            emptyList()
        }
    }

    private fun parseSecurityInfo(seq: ASN1Sequence): PaceInfo? {
        if (seq.size() < 2) return null
        val oid = (seq.getObjectAt(0) as? ASN1ObjectIdentifier)?.id ?: return null
        if (!oid.startsWith(PACE_OID_PREFIX)) return null // only PACEInfo entries
        val version = (seq.getObjectAt(1) as? ASN1Integer)?.value?.toInt() ?: return null
        val parameterId = if (seq.size() >= 3) {
            (seq.getObjectAt(2) as? ASN1Integer)?.value?.toInt()
        } else null
        return PaceInfo(protocolOid = oid, version = version, parameterId = parameterId)
    }
}
