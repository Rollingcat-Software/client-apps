package com.fivucsas.shared.platform

import kotlinx.cinterop.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.*
import platform.Foundation.*
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * iOS actual implementation for WebAuthn / Passkeys.
 *
 * Uses the AuthenticationServices framework (ASAuthorizationController) introduced in iOS 16.
 * This enables platform passkey creation and assertion using Face ID / Touch ID as
 * the user verification mechanism.
 *
 * Requirements:
 * - iOS 16+ for passkey support
 * - Associated domain entitlement configured for the RP ID
 *
 * Graceful fallback: on iOS < 16 or when passkeys are unsupported,
 * isAvailable() returns false and callers should use the native
 * fingerprint flow instead.
 */

actual fun provideWebAuthnAuthenticator(): WebAuthnAuthenticator {
    return IosWebAuthnAuthenticator()
}

actual fun isWebAuthnAvailable(): Boolean {
    // ASAuthorizationPlatformPublicKeyCredentialProvider is available on iOS 16+
    return NSProcessInfo.processInfo.operatingSystemVersion.useContents {
        majorVersion >= 16L
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosWebAuthnAuthenticator : WebAuthnAuthenticator {

    override suspend fun isAvailable(): Boolean = isWebAuthnAvailable()

    override suspend fun createCredential(
        rpId: String,
        rpName: String,
        userId: String,
        userName: String,
        challenge: String,
        excludeCredentialIds: List<String>,
        authenticatorAttachment: String,
        userVerification: String
    ): WebAuthnCreateResult {
        if (!isAvailable()) {
            throw FingerprintAuthException(
                "Passkeys require iOS 16 or later.",
                false
            )
        }

        val challengeData = base64UrlDecode(challenge)
        val userIdData = userId.encodeToByteArray().toNSData()

        val provider = ASAuthorizationPlatformPublicKeyCredentialProvider(relyingPartyIdentifier = rpId)
        val request = provider.createCredentialRegistrationRequestWithChallenge(
            challenge = challengeData,
            name = userName,
            userID = userIdData
        )

        // Set user verification preference
        when (userVerification) {
            "required" -> request.setUserVerificationPreference(
                ASAuthorizationPublicKeyCredentialUserVerificationPreferenceRequired
            )
            "discouraged" -> request.setUserVerificationPreference(
                ASAuthorizationPublicKeyCredentialUserVerificationPreferenceDiscouraged
            )
            else -> request.setUserVerificationPreference(
                ASAuthorizationPublicKeyCredentialUserVerificationPreferencePreferred
            )
        }

        val authorization = performAuthorization(request)
        val credential = authorization.credential as? ASAuthorizationPlatformPublicKeyCredentialRegistration
            ?: throw FingerprintAuthException("Unexpected credential type from iOS", false)

        val credentialId = credential.credentialID.base64UrlEncode()
        val rawAttestationObject = credential.rawAttestationObject
        val rawClientDataJson = credential.rawClientDataJSON

        return WebAuthnCreateResult(
            credentialId = credentialId,
            publicKey = rawAttestationObject?.base64UrlEncode() ?: "",
            publicKeyAlgorithm = "ES256",
            attestationFormat = "apple",
            transports = "internal",
            clientDataJson = rawClientDataJson?.base64UrlEncode() ?: ""
        )
    }

    override suspend fun getAssertion(
        rpId: String,
        challenge: String,
        allowCredentialIds: List<String>,
        userVerification: String
    ): WebAuthnAssertionResult {
        if (!isAvailable()) {
            throw FingerprintAuthException(
                "Passkeys require iOS 16 or later.",
                false
            )
        }

        val challengeData = base64UrlDecode(challenge)

        val provider = ASAuthorizationPlatformPublicKeyCredentialProvider(relyingPartyIdentifier = rpId)
        val request = provider.createCredentialAssertionRequestWithChallenge(challengeData)

        if (allowCredentialIds.isNotEmpty()) {
            val descriptors = allowCredentialIds.map { credId ->
                ASAuthorizationPlatformPublicKeyCredentialDescriptor(
                    credentialID = base64UrlDecode(credId)
                )
            }
            request.setAllowedCredentials(descriptors)
        }

        when (userVerification) {
            "required" -> request.setUserVerificationPreference(
                ASAuthorizationPublicKeyCredentialUserVerificationPreferenceRequired
            )
            "discouraged" -> request.setUserVerificationPreference(
                ASAuthorizationPublicKeyCredentialUserVerificationPreferenceDiscouraged
            )
            else -> request.setUserVerificationPreference(
                ASAuthorizationPublicKeyCredentialUserVerificationPreferencePreferred
            )
        }

        val authorization = performAuthorization(request)
        val credential = authorization.credential as? ASAuthorizationPlatformPublicKeyCredentialAssertion
            ?: throw FingerprintAuthException("Unexpected assertion type from iOS", false)

        return WebAuthnAssertionResult(
            credentialId = credential.credentialID.base64UrlEncode(),
            authenticatorData = credential.rawAuthenticatorData?.base64UrlEncode() ?: "",
            clientDataJson = credential.rawClientDataJSON?.base64UrlEncode() ?: "",
            signature = credential.signature?.base64UrlEncode() ?: ""
        )
    }

    /**
     * Execute an ASAuthorization request and suspend until it completes.
     */
    private suspend fun performAuthorization(
        request: ASAuthorizationRequest
    ): ASAuthorization = suspendCancellableCoroutine { cont ->
        val delegate = AuthorizationDelegate(
            onSuccess = { authorization ->
                cont.resume(authorization)
            },
            onFailure = { error ->
                val msg = error.localizedDescription
                val recoverable = error.code != 1001L // 1001 = ASAuthorizationErrorCanceled
                cont.resumeWithException(FingerprintAuthException(msg, recoverable))
            }
        )

        val controller = ASAuthorizationController(authorizationRequests = listOf(request))
        controller.delegate = delegate
        controller.performRequests()
    }

    // --- Helpers ---

    private fun base64UrlDecode(input: String): NSData {
        var base64 = input.replace('-', '+').replace('_', '/')
        while (base64.length % 4 != 0) base64 += "="
        return NSData.create(base64Encoding = base64)
            ?: NSData()
    }

    private fun NSData.base64UrlEncode(): String {
        return base64EncodedStringWithOptions(0u)
            .replace('+', '-')
            .replace('/', '_')
            .trimEnd('=')
    }
}

/**
 * ASAuthorizationControllerDelegate implementation.
 */
private class AuthorizationDelegate(
    private val onSuccess: (ASAuthorization) -> Unit,
    private val onFailure: (NSError) -> Unit
) : NSObject(), ASAuthorizationControllerDelegateProtocol {

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization
    ) {
        onSuccess(didCompleteWithAuthorization)
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError
    ) {
        onFailure(didCompleteWithError)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return memScoped {
        NSData.create(bytes = this@toNSData.refTo(0), length = this@toNSData.size.toULong())
    }
}
