package com.fivucsas.authenticator.totp

actual fun hmacSha1(key: ByteArray, message: ByteArray): ByteArray =
    TODO(#106)("iOS HMAC via CommonCrypto — tracked in docs/plans/CLIENT_APPS_PARITY.md")

actual fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray =
    TODO("iOS HMAC via CommonCrypto — tracked in docs/plans/CLIENT_APPS_PARITY.md")

actual fun hmacSha512(key: ByteArray, message: ByteArray): ByteArray =
    TODO("iOS HMAC via CommonCrypto — tracked in docs/plans/CLIENT_APPS_PARITY.md")
