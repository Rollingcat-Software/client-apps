package com.fivucsas.authenticator.totp

import kotlin.math.pow

object TotpGenerator {

    fun generate(
        secret: ByteArray,
        epochSeconds: Long,
        algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
        digits: Int = 6,
        period: Int = 30
    ): String {
        require(secret.isNotEmpty()) { "secret must not be empty" }
        require(digits in 6..10) { "digits must be between 6 and 10" }
        require(period > 0) { "period must be positive" }

        val counter = epochSeconds / period
        val counterBytes = counterToBytes(counter)
        val hash = when (algorithm) {
            TotpAlgorithm.SHA1 -> hmacSha1(secret, counterBytes)
            TotpAlgorithm.SHA256 -> hmacSha256(secret, counterBytes)
            TotpAlgorithm.SHA512 -> hmacSha512(secret, counterBytes)
        }
        val offset = hash[hash.size - 1].toInt() and 0x0f
        val binary =
            ((hash[offset].toInt() and 0x7f) shl 24) or
            ((hash[offset + 1].toInt() and 0xff) shl 16) or
            ((hash[offset + 2].toInt() and 0xff) shl 8) or
            (hash[offset + 3].toInt() and 0xff)
        val modulus = 10.0.pow(digits).toInt()
        val otp = binary % modulus
        return otp.toString().padStart(digits, '0')
    }

    fun remainingSeconds(epochSeconds: Long, period: Int = 30): Int {
        require(period > 0) { "period must be positive" }
        val elapsed = (epochSeconds % period).toInt()
        return period - elapsed
    }

    private fun counterToBytes(counter: Long): ByteArray {
        val out = ByteArray(8)
        var value = counter
        for (i in 7 downTo 0) {
            out[i] = (value and 0xff).toByte()
            value = value ushr 8
        }
        return out
    }
}
