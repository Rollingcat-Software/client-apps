package com.fivucsas.authenticator.totp

expect fun hmacSha1(key: ByteArray, message: ByteArray): ByteArray

expect fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray

expect fun hmacSha512(key: ByteArray, message: ByteArray): ByteArray
