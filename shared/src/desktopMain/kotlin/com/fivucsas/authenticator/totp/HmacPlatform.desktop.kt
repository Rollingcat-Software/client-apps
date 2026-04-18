package com.fivucsas.authenticator.totp

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

actual fun hmacSha1(key: ByteArray, message: ByteArray): ByteArray =
    runMac("HmacSHA1", key, message)

actual fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray =
    runMac("HmacSHA256", key, message)

actual fun hmacSha512(key: ByteArray, message: ByteArray): ByteArray =
    runMac("HmacSHA512", key, message)

private fun runMac(algorithm: String, key: ByteArray, message: ByteArray): ByteArray {
    val mac = Mac.getInstance(algorithm)
    mac.init(SecretKeySpec(key, algorithm))
    return mac.doFinal(message)
}
