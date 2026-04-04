package com.fivucsas.shared.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import java.util.UUID
import java.util.prefs.Preferences

/**
 * Desktop actual implementation for fingerprint / biometric authentication.
 *
 * Desktop platforms generally lack a biometric sensor API accessible from
 * the JVM. This implementation provides:
 *
 * 1. **Platform detection** — detects Windows, macOS, Linux
 * 2. **Software ECDSA P-256 key pair** — stored in Java Preferences
 * 3. **Signing** — signs nonces using the stored private key
 *
 * On desktop, `isSupported()` returns true because we provide a
 * software-based authenticator (no biometric prompt, but the key
 * management and signing protocol is identical to mobile).
 *
 * Future enhancements:
 * - Windows Hello via JNA/ProcessBuilder (whoami /user + certutil)
 * - macOS Touch ID via Security framework JNI bridge
 * - Linux: FIDO2 USB tokens via libfido2
 *
 * For the FIVUCSAS platform, this means desktop users can enroll and
 * verify via the fingerprint step without a physical biometric sensor,
 * using the software key as a device credential.
 */

actual fun providePlatformFingerprintAuthenticator(): FingerprintAuthenticator {
    return DesktopFingerprintAuthenticator()
}

actual fun isFingerprintFlowAvailable(): Boolean = true

private class DesktopFingerprintAuthenticator : FingerprintAuthenticator {

    private val prefs = Preferences.userNodeForPackage(DesktopFingerprintAuthenticator::class.java)
    private val keyStoreDir = File(System.getProperty("user.home"), ".fivucsas/keys").also { it.mkdirs() }
    private val keyStoreFile = File(keyStoreDir, "fingerprint.p12")
    private val keyStorePassword = "fivucsas-desktop-fp".toCharArray()

    companion object {
        private const val KEY_ID_PREF = "fingerprint_key_id"
        private const val KEYSTORE_TYPE = "PKCS12"
        private const val KEY_ALIAS = "fp-desktop"
    }

    override suspend fun isSupported(): Boolean = true

    override suspend fun getOrCreateKeyId(): String = withContext(Dispatchers.IO) {
        val existing = prefs.get(KEY_ID_PREF, null)
        if (existing != null && keyPairExists()) {
            return@withContext existing
        }

        val keyId = "fp-desktop-${UUID.randomUUID()}"
        createAndStoreKeyPair()
        prefs.put(KEY_ID_PREF, keyId)
        prefs.flush()
        keyId
    }

    override suspend fun getPublicKeyJwk(): String = withContext(Dispatchers.IO) {
        val keyId = getOrCreateKeyId()
        val keyPair = loadKeyPair()
            ?: throw FingerprintAuthException("Key pair not found on desktop.", false)

        buildEcJwk(keyId, keyPair.public)
    }

    override suspend fun signNonce(nonce: String): String = withContext(Dispatchers.IO) {
        val keyPair = loadKeyPair()
            ?: throw FingerprintAuthException("Key pair not found on desktop.", false)

        val signer = Signature.getInstance("SHA256withECDSA")
        signer.initSign(keyPair.private)
        signer.update(nonce.encodeToByteArray())
        val signature = signer.sign()

        Base64.getEncoder().encodeToString(signature)
    }

    // --- Key Management ---

    private fun keyPairExists(): Boolean {
        if (!keyStoreFile.exists()) return false
        return try {
            val ks = KeyStore.getInstance(KEYSTORE_TYPE)
            ks.load(keyStoreFile.inputStream(), keyStorePassword)
            ks.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            false
        }
    }

    private fun createAndStoreKeyPair() {
        val generator = KeyPairGenerator.getInstance("EC")
        generator.initialize(ECGenParameterSpec("secp256r1"), SecureRandom())
        val keyPair = generator.generateKeyPair()

        // Self-signed certificate (required for PKCS12 private key entry)
        val cert = generateSelfSignedCert(keyPair)

        val ks = KeyStore.getInstance(KEYSTORE_TYPE)
        ks.load(null, keyStorePassword)
        ks.setKeyEntry(KEY_ALIAS, keyPair.private, keyStorePassword, arrayOf(cert))
        ks.store(keyStoreFile.outputStream(), keyStorePassword)
    }

    private fun loadKeyPair(): KeyPair? {
        if (!keyStoreFile.exists()) return null
        return try {
            val ks = KeyStore.getInstance(KEYSTORE_TYPE)
            ks.load(keyStoreFile.inputStream(), keyStorePassword)

            val privateKey = ks.getKey(KEY_ALIAS, keyStorePassword) as? PrivateKey ?: return null
            val cert = ks.getCertificate(KEY_ALIAS) ?: return null
            val publicKey = cert.publicKey

            KeyPair(publicKey, privateKey)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generate a minimal self-signed X.509 certificate for the key pair.
     * Uses the JDK internal API (sun.security.x509) which is available in all
     * standard JDK distributions. This is only used to store the key pair in PKCS12.
     */
    private fun generateSelfSignedCert(keyPair: KeyPair): java.security.cert.X509Certificate {
        // Use Bouncy Castle-free approach: create a minimal self-signed cert via CertificateFactory
        // We'll use the JDK's built-in X509 cert generation via keytool-like approach
        val dn = "CN=FIVUCSAS Desktop Authenticator"
        val validDays = 3650L

        // Reflective approach to sun.security.x509 (works on all JDK 11+)
        try {
            val x500NameClass = Class.forName("sun.security.x509.X500Name")
            val certInfoClass = Class.forName("sun.security.x509.X509CertInfo")
            val certImplClass = Class.forName("sun.security.x509.X509CertImpl")
            val certValidityClass = Class.forName("sun.security.x509.CertificateValidity")
            val certSerialClass = Class.forName("sun.security.x509.CertificateSerialNumber")
            val serialClass = Class.forName("sun.security.x509.SerialNumber")
            val algIdClass = Class.forName("sun.security.x509.AlgorithmId")
            val certAlgIdClass = Class.forName("sun.security.x509.CertificateAlgorithmId")
            val certSubjectClass = Class.forName("sun.security.x509.CertificateSubjectName")
            val certIssuerClass = Class.forName("sun.security.x509.CertificateIssuerName")
            val certKeyClass = Class.forName("sun.security.x509.CertificateX509Key")
            val certVersionClass = Class.forName("sun.security.x509.CertificateVersion")

            val x500Name = x500NameClass.getConstructor(String::class.java).newInstance(dn)

            val now = java.util.Date()
            val until = java.util.Date(now.time + validDays * 86400000L)
            val validity = certValidityClass.getConstructor(java.util.Date::class.java, java.util.Date::class.java)
                .newInstance(now, until)

            val serial = serialClass.getConstructor(Int::class.java).newInstance(
                (System.currentTimeMillis() / 1000).toInt()
            )
            val certSerial = certSerialClass.getConstructor(serialClass).newInstance(serial)

            val algId = algIdClass.getMethod("get", String::class.java).invoke(null, "SHA256withECDSA")
            val certAlgId = certAlgIdClass.getConstructor(algIdClass).newInstance(algId)

            val info = certInfoClass.getConstructor().newInstance()
            val setMethod = certInfoClass.getMethod("set", String::class.java, Object::class.java)

            setMethod.invoke(info, "validity", validity)
            setMethod.invoke(info, "serialNumber", certSerial)
            setMethod.invoke(info, "subject", certSubjectClass.getConstructor(x500NameClass).newInstance(x500Name))
            setMethod.invoke(info, "issuer", certIssuerClass.getConstructor(x500NameClass).newInstance(x500Name))
            setMethod.invoke(info, "key", certKeyClass.getConstructor(PublicKey::class.java).newInstance(keyPair.public))
            setMethod.invoke(info, "version", certVersionClass.getConstructor(Int::class.java).newInstance(2))
            setMethod.invoke(info, "algorithmID", certAlgId)

            val cert = certImplClass.getConstructor(certInfoClass).newInstance(info)
            val signMethod = certImplClass.getMethod("sign", PrivateKey::class.java, String::class.java)
            signMethod.invoke(cert, keyPair.private, "SHA256withECDSA")

            return cert as java.security.cert.X509Certificate
        } catch (e: Exception) {
            // Fallback: if sun.security.x509 is not accessible (rare), throw clear error
            throw FingerprintAuthException(
                "Cannot generate self-signed certificate on this JVM: ${e.message}",
                false
            )
        }
    }

    // --- JWK Export ---

    private fun buildEcJwk(keyId: String, publicKey: PublicKey): String {
        val encoded = publicKey.encoded
        // X.509 SubjectPublicKeyInfo: the uncompressed EC point starts near the end
        val pointStart = encoded.size - 65
        if (pointStart < 0 || encoded[pointStart] != 0x04.toByte()) {
            throw FingerprintAuthException("Unexpected EC public key format", false)
        }

        val x = encoded.copyOfRange(pointStart + 1, pointStart + 33)
        val y = encoded.copyOfRange(pointStart + 33, pointStart + 65)

        val encoder = Base64.getUrlEncoder().withoutPadding()
        val xB64 = encoder.encodeToString(x)
        val yB64 = encoder.encodeToString(y)

        return """{"kty":"EC","crv":"P-256","kid":"$keyId","x":"$xB64","y":"$yB64"}"""
    }
}
