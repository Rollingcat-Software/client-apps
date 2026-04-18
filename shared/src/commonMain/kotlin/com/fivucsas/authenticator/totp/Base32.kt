package com.fivucsas.authenticator.totp

object Base32 {

    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun decode(input: String): ByteArray {
        val cleaned = input.trim().replace(" ", "").replace("-", "").trimEnd('=').uppercase()
        if (cleaned.isEmpty()) return ByteArray(0)
        val out = ArrayList<Byte>(cleaned.length * 5 / 8)
        var buffer = 0
        var bits = 0
        for (ch in cleaned) {
            val idx = ALPHABET.indexOf(ch)
            require(idx >= 0) { "Invalid base32 character: $ch" }
            buffer = (buffer shl 5) or idx
            bits += 5
            if (bits >= 8) {
                bits -= 8
                out.add(((buffer shr bits) and 0xff).toByte())
                buffer = buffer and ((1 shl bits) - 1)
            }
        }
        return out.toByteArray()
    }

    fun encode(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        val sb = StringBuilder()
        var buffer = 0
        var bits = 0
        for (b in bytes) {
            buffer = (buffer shl 8) or (b.toInt() and 0xff)
            bits += 8
            while (bits >= 5) {
                bits -= 5
                sb.append(ALPHABET[(buffer shr bits) and 0x1f])
            }
        }
        if (bits > 0) {
            sb.append(ALPHABET[(buffer shl (5 - bits)) and 0x1f])
        }
        return sb.toString()
    }
}
