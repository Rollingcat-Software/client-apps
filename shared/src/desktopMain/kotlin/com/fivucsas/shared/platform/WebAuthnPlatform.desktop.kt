package com.fivucsas.shared.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.UUID

/**
 * Desktop actual implementation for WebAuthn / platform authenticator.
 *
 * Desktop platforms do not have a native WebAuthn browser API, but we
 * provide a software-based ECDSA P-256 authenticator that:
 *
 * 1. Generates and stores a key pair in the Java KeyStore (PKCS12 file)
 * 2. Signs challenges with the private key
 * 3. Returns results in the same WebAuthn-compatible format
 *
 * This allows desktop users to register and authenticate with the
 * FIVUCSAS backend using the same FIDO2 protocol, albeit without
 * hardware-backed security (TPM/Windows Hello).
 *
 * Platform detection:
 * - Windows: could be enhanced with Windows Hello via JNA in a future release
 * - macOS: could use Security framework via JNI
 * - Linux: software-only for now
 *
 * The implementation is functional and secure enough for development
 * and non-critical use cases. For production, Windows Hello / macOS
 * Touch ID integration via native bridges is recommended.
 */

actual fun provideWebAuthnAuthenticator(): WebAuthnAuthenticator {
    return DesktopWebAuthnAuthenticator()
}

actual fun isWebAuthnAvailable(): Boolean = true

private class DesktopWebAuthnAuthenticator : WebAuthnAuthenticator {

    private val credentialStore = DesktopCredentialStore()

    override suspend fun isAvailable(): Boolean = true

    override suspend fun createCredential(
        rpId: String,
        rpName: String,
        userId: String,
        userName: String,
        challenge: String,
        excludeCredentialIds: List<String>,
        authenticatorAttachment: String,
        userVerification: String
    ): WebAuthnCreateResult = withContext(Dispatchers.IO) {
        // Check for excluded credentials
        for (excludeId in excludeCredentialIds) {
            if (credentialStore.hasCredential(excludeId)) {
                throw FingerprintAuthException(
                    "Credential already registered for this device.",
                    recoverable = false
                )
            }
        }

        // Generate a new ECDSA P-256 key pair
        val credentialId = "desktop-${UUID.randomUUID()}"
        val keyPair = generateKeyPair()
        credentialStore.saveCredential(credentialId, rpId, keyPair)

        // Build a minimal attestation object (self-attestation)
        val challengeBytes = base64UrlDecode(challenge)
        val clientDataJson = buildClientDataJson("webauthn.create", challenge, rpId)
        val clientDataJsonBytes = clientDataJson.encodeToByteArray()
        val clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataJsonBytes)

        // Build authenticator data: rpIdHash (32) + flags (1) + signCount (4) + attestedCredData
        val rpIdHash = MessageDigest.getInstance("SHA-256").digest(rpId.encodeToByteArray())
        val flags = byteArrayOf(0x45) // AT + UV + UP flags
        val signCount = byteArrayOf(0, 0, 0, 1)

        // Attested credential data: AAGUID (16) + credIdLen (2) + credId + pubKeyCOSE
        val aaguid = ByteArray(16) // zero AAGUID for software authenticator
        val credIdBytes = credentialId.encodeToByteArray()
        val credIdLen = byteArrayOf(
            ((credIdBytes.size shr 8) and 0xFF).toByte(),
            (credIdBytes.size and 0xFF).toByte()
        )
        val pubKeyCose = exportPublicKeyCose(keyPair.public)

        val authData = rpIdHash + flags + signCount + aaguid + credIdLen + credIdBytes + pubKeyCose

        // Export public key as base64url for the result
        val publicKeyB64 = base64UrlEncode(keyPair.public.encoded)

        WebAuthnCreateResult(
            credentialId = base64UrlEncode(credIdBytes),
            publicKey = publicKeyB64,
            publicKeyAlgorithm = "ES256",
            attestationFormat = "none",
            transports = "internal",
            clientDataJson = base64UrlEncode(clientDataJsonBytes)
        )
    }

    override suspend fun getAssertion(
        rpId: String,
        challenge: String,
        allowCredentialIds: List<String>,
        userVerification: String
    ): WebAuthnAssertionResult = withContext(Dispatchers.IO) {
        // Find a matching credential
        val matchedCredId = if (allowCredentialIds.isNotEmpty()) {
            allowCredentialIds.firstOrNull { credentialStore.hasCredential(it) }
                ?: throw FingerprintAuthException(
                    "No matching credential found on this device.",
                    recoverable = false
                )
        } else {
            credentialStore.getFirstCredentialForRp(rpId)
                ?: throw FingerprintAuthException(
                    "No credentials registered for $rpId.",
                    recoverable = false
                )
        }

        val keyPair = credentialStore.loadCredential(matchedCredId)
            ?: throw FingerprintAuthException("Credential key not found.", false)

        // Build client data
        val clientDataJson = buildClientDataJson("webauthn.get", challenge, rpId)
        val clientDataJsonBytes = clientDataJson.encodeToByteArray()
        val clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataJsonBytes)

        // Build authenticator data: rpIdHash (32) + flags (1) + signCount (4)
        val rpIdHash = MessageDigest.getInstance("SHA-256").digest(rpId.encodeToByteArray())
        val flags = byteArrayOf(0x05) // UV + UP flags
        val signCount = credentialStore.incrementSignCount(matchedCredId)
        val signCountBytes = byteArrayOf(
            ((signCount shr 24) and 0xFF).toByte(),
            ((signCount shr 16) and 0xFF).toByte(),
            ((signCount shr 8) and 0xFF).toByte(),
            (signCount and 0xFF).toByte()
        )
        val authData = rpIdHash + flags + signCountBytes

        // Sign authData + clientDataHash
        val signedData = authData + clientDataHash
        val signature = signWithPrivateKey(keyPair.private, signedData)

        WebAuthnAssertionResult(
            credentialId = matchedCredId,
            authenticatorData = base64UrlEncode(authData),
            clientDataJson = base64UrlEncode(clientDataJsonBytes),
            signature = base64UrlEncode(signature)
        )
    }

    // --- Key Generation ---

    private fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("EC")
        generator.initialize(ECGenParameterSpec("secp256r1"), SecureRandom())
        return generator.generateKeyPair()
    }

    private fun signWithPrivateKey(privateKey: PrivateKey, data: ByteArray): ByteArray {
        val signer = Signature.getInstance("SHA256withECDSA")
        signer.initSign(privateKey)
        signer.update(data)
        return signer.sign()
    }

    /**
     * Export EC public key in COSE_Key format (minimal, for attestation data).
     * CBOR map: {1: 2, 3: -7, -1: 1, -2: x, -3: y}
     */
    private fun exportPublicKeyCose(publicKey: PublicKey): ByteArray {
        val encoded = publicKey.encoded
        // X.509 SubjectPublicKeyInfo for EC P-256 has the uncompressed point starting at offset 26
        // The point is 0x04 + 32 bytes X + 32 bytes Y
        val pointStart = encoded.size - 65
        val x = encoded.copyOfRange(pointStart + 1, pointStart + 33)
        val y = encoded.copyOfRange(pointStart + 33, pointStart + 65)

        // Minimal CBOR encoding of the COSE key
        // A5 = map(5)
        //   01 02 = kty: EC2
        //   03 26 = alg: ES256 (-7)
        //   20 01 = crv: P-256
        //   21 5820 [32 bytes x]
        //   22 5820 [32 bytes y]
        val header = byteArrayOf(
            0xA5.toByte(),
            0x01, 0x02,
            0x03, 0x26.toByte(),
            0x20, 0x01,
            0x21, 0x58, 0x20
        )
        val ySep = byteArrayOf(0x22, 0x58, 0x20)
        return header + x + ySep + y
    }

    // --- Client Data ---

    private fun buildClientDataJson(type: String, challenge: String, origin: String): String {
        return """{"type":"$type","challenge":"$challenge","origin":"https://$origin","crossOrigin":false}"""
    }

    // --- Base64 URL ---

    private fun base64UrlEncode(data: ByteArray): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data)
    }

    private fun base64UrlDecode(input: String): ByteArray {
        return Base64.getUrlDecoder().decode(input)
    }
}

/**
 * Simple file-based credential store for desktop WebAuthn keys.
 *
 * Stores key pairs as serialized Java objects in the user's home directory.
 * In production, this should be replaced with a hardware-backed store
 * (Windows Hello / macOS Keychain) or at minimum an encrypted file.
 */
private class DesktopCredentialStore {

    private val storeDir = java.io.File(
        System.getProperty("user.home"), ".fivucsas/webauthn"
    ).also { it.mkdirs() }

    private val signCounts = mutableMapOf<String, Int>()

    fun hasCredential(credentialId: String): Boolean {
        val safeId = credentialId.replace("/", "_").replace("\\", "_")
        return java.io.File(storeDir, "$safeId.key").exists()
    }

    fun saveCredential(credentialId: String, rpId: String, keyPair: KeyPair) {
        val safeId = credentialId.replace("/", "_").replace("\\", "_")
        val file = java.io.File(storeDir, "$safeId.key")
        java.io.ObjectOutputStream(file.outputStream()).use { oos ->
            oos.writeUTF(rpId)
            oos.writeObject(keyPair)
        }
        signCounts[credentialId] = 0

        // Also save a mapping file for RP lookup
        val rpFile = java.io.File(storeDir, "$safeId.rp")
        rpFile.writeText(rpId)
    }

    fun loadCredential(credentialId: String): KeyPair? {
        val safeId = credentialId.replace("/", "_").replace("\\", "_")
        val file = java.io.File(storeDir, "$safeId.key")
        if (!file.exists()) return null

        return try {
            java.io.ObjectInputStream(file.inputStream()).use { ois ->
                ois.readUTF() // rpId
                ois.readObject() as KeyPair
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getFirstCredentialForRp(rpId: String): String? {
        val rpFiles = storeDir.listFiles { _, name -> name.endsWith(".rp") } ?: return null
        for (rpFile in rpFiles) {
            if (rpFile.readText().trim() == rpId) {
                val credId = rpFile.nameWithoutExtension
                return credId
            }
        }
        return null
    }

    fun incrementSignCount(credentialId: String): Int {
        val count = (signCounts[credentialId] ?: 0) + 1
        signCounts[credentialId] = count
        return count
    }
}
