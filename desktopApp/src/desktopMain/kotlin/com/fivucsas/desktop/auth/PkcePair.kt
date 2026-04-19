package com.fivucsas.desktop.auth

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * RFC 7636 PKCE verifier + S256 challenge pair.
 *
 * - `verifier` is a high-entropy random string (43-128 chars of the unreserved set)
 * - `challenge` = BASE64URL-NO-PAD(SHA256(ASCII(verifier)))
 *
 * See [PkcePair.generate] for the canonical constructor.
 */
data class PkcePair(
    val verifier: String,
    val challenge: String,
) {
    companion object {
        private const val VERIFIER_BYTES = 32 // → 43 base64url chars, middle of the RFC 7636 range

        /** Generate a new PKCE pair using a cryptographically secure RNG. */
        fun generate(random: SecureRandom = SecureRandom()): PkcePair {
            val verifierBytes = ByteArray(VERIFIER_BYTES).also(random::nextBytes)
            val verifier = base64UrlNoPad(verifierBytes)
            val challenge = challengeFor(verifier)
            return PkcePair(verifier, challenge)
        }

        /** S256 transform: BASE64URL-NO-PAD(SHA256(ASCII(verifier))). */
        fun challengeFor(verifier: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(verifier.toByteArray(StandardCharsets.US_ASCII))
            return base64UrlNoPad(digest)
        }

        private fun base64UrlNoPad(bytes: ByteArray): String =
            Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
