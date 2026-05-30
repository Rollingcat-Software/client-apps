package com.fivucsas.mobile.android.data.nfc.pace

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [CardAccessParser]. The EF.CardAccess fixtures are built with
 * BouncyCastle ASN.1 encoders and parsed back — no card needed, so this runs as
 * a plain JVM unit test.
 */
class CardAccessParserTest {

    /** id-PACE-ECDH-GM-AES-CBC-CMAC-128 */
    private val paceGmAes128 = "0.4.0.127.0.7.2.2.4.2.4"

    private fun securityInfo(oid: String, version: Int, parameterId: Int?): DERSequence {
        val v = ASN1EncodableVector()
        v.add(ASN1ObjectIdentifier(oid))
        v.add(ASN1Integer(version.toLong()))
        if (parameterId != null) v.add(ASN1Integer(parameterId.toLong()))
        return DERSequence(v)
    }

    private fun cardAccess(vararg infos: DERSequence): ByteArray =
        DERSet(ASN1EncodableVector().apply { infos.forEach { add(it) } }).encoded

    @Test
    fun `parses a single PACE-GM-AES PACEInfo`() {
        val bytes = cardAccess(securityInfo(paceGmAes128, version = 2, parameterId = 13))

        val infos = CardAccessParser.parse(bytes)

        assertEquals(1, infos.size)
        val info = infos.first()
        assertEquals(paceGmAes128, info.protocolOid)
        assertEquals(2, info.version)
        assertEquals(13, info.parameterId)
        assertTrue(info.isGenericMapping)
        assertTrue(info.isAes)
    }

    @Test
    fun `PACEInfo without a parameterId parses with null parameterId`() {
        val bytes = cardAccess(securityInfo(paceGmAes128, version = 2, parameterId = null))

        val infos = CardAccessParser.parse(bytes)

        assertEquals(1, infos.size)
        assertNull(infos.first().parameterId)
    }

    @Test
    fun `non-PACE SecurityInfo entries are ignored`() {
        // A ChipAuthenticationInfo-like OID outside the PACE arc.
        val bytes = cardAccess(
            securityInfo("0.4.0.127.0.7.2.2.3.2.1", version = 1, parameterId = null),
            securityInfo(paceGmAes128, version = 2, parameterId = 13)
        )

        val infos = CardAccessParser.parse(bytes)

        assertEquals(1, infos.size)
        assertEquals(paceGmAes128, infos.first().protocolOid)
    }

    @Test
    fun `empty input yields no entries`() {
        assertTrue(CardAccessParser.parse(ByteArray(0)).isEmpty())
    }

    @Test
    fun `garbage input yields no entries instead of throwing`() {
        assertTrue(CardAccessParser.parse(byteArrayOf(0x01, 0x02, 0x03)).isEmpty())
    }
}
