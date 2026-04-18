package com.fivucsas.authenticator.totp

enum class TotpAlgorithm(val canonical: String) {
    SHA1("SHA1"),
    SHA256("SHA256"),
    SHA512("SHA512");

    companion object {
        fun fromString(value: String?): TotpAlgorithm = when (value?.uppercase()) {
            null, "", "SHA1", "HMAC-SHA1", "SHA-1" -> SHA1
            "SHA256", "HMAC-SHA256", "SHA-256" -> SHA256
            "SHA512", "HMAC-SHA512", "SHA-512" -> SHA512
            else -> throw IllegalArgumentException("Unsupported algorithm: $value")
        }
    }
}
