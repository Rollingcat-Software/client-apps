package com.fivucsas.authenticator.totp

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Base32Test {

    @Test
    fun round_trip_preserves_bytes() {
        val original = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val encoded = Base32.encode(original)
        val decoded = Base32.decode(encoded)
        assertContentEquals(original, decoded)
    }

    @Test
    fun decodes_known_ascii() {
        // "Hello!" in base32 (RFC 4648) is "JBSWY3DPEE======"
        val decoded = Base32.decode("JBSWY3DPEE")
        assertContentEquals("Hello!".encodeToByteArray(), decoded)
    }

    @Test
    fun encode_round_trip_of_ascii() {
        val bytes = "Hello!".encodeToByteArray()
        val encoded = Base32.encode(bytes)
        assertEquals("JBSWY3DPEE", encoded)
    }

    @Test
    fun empty_input_is_empty_output() {
        assertEquals(0, Base32.decode("").size)
        assertEquals("", Base32.encode(ByteArray(0)))
    }

    @Test
    fun invalid_character_throws() {
        assertFailsWith<IllegalArgumentException> {
            Base32.decode("!!!!")
        }
    }
}
