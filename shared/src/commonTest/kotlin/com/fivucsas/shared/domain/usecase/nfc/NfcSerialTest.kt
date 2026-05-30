package com.fivucsas.shared.domain.usecase.nfc

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies [normalizeCardSerial] yields the API-canonical form agreed with
 * identity-core-api (UPPERHEX, no separators) so a mobile-enrolled card
 * matches a web verify and vice-versa.
 */
class NfcSerialTest {

    @Test
    fun `colon-separated lower hex normalizes to upper no-separator hex`() {
        assertEquals("04A2245B6F7180", normalizeCardSerial("04:a2:24:5b:6f:71:80"))
    }

    @Test
    fun `dash-separated hex normalizes`() {
        assertEquals("04A2245B", normalizeCardSerial("04-a2-24-5b"))
    }

    @Test
    fun `already-canonical UPPERHEX is unchanged`() {
        assertEquals("04A2245B6F7180", normalizeCardSerial("04A2245B6F7180"))
    }

    @Test
    fun `Android toHexString output passes through unchanged`() {
        // ByteArray.toHexString() = joinToString("") { "%02X".format(it) }
        assertEquals("DEADBEEF", normalizeCardSerial("DEADBEEF"))
    }

    @Test
    fun `surrounding whitespace is trimmed`() {
        assertEquals("04A2245B", normalizeCardSerial("  04 a2 24 5b  "))
    }

    @Test
    fun `non-hex opaque serial is upper-cased and trimmed but separators kept`() {
        assertEquals("CARD-XYZ:01", normalizeCardSerial(" card-xyz:01 "))
    }

    @Test
    fun `dotted hex normalizes`() {
        assertEquals("0A1B2C", normalizeCardSerial("0a.1b.2c"))
    }
}
