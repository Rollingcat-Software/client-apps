package com.fivucsas.shared.platform

import android.app.Activity
import android.app.Application
import android.os.Build
import android.util.Base64
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.context.GlobalContext
import java.lang.ref.WeakReference

actual fun provideWebAuthnAuthenticator(): WebAuthnAuthenticator {
    val app = GlobalContext.get().get<android.content.Context>().applicationContext as Application
    return AndroidWebAuthnAuthenticator(app)
}

actual fun isWebAuthnAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

/**
 * Android implementation of WebAuthnAuthenticator using AndroidX Credential Manager.
 *
 * Credential Manager delegates to Google Play Services FIDO2 provider for both
 * platform authenticators (fingerprint/face unlock) and cross-platform authenticators
 * (USB/NFC/BLE security keys like YubiKey).
 */
private class AndroidWebAuthnAuthenticator(
    private val application: Application
) : WebAuthnAuthenticator {

    private val credentialManager by lazy {
        CredentialManager.create(application)
    }

    init {
        WebAuthnActivityTracker.registerIfNeeded(application)
    }

    override suspend fun isAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

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
        val activity = WebAuthnActivityTracker.currentActivity()
            ?: throw FingerprintAuthException(
                "WebAuthn requires an active Activity context.",
                recoverable = true
            )

        // Build the PublicKeyCredentialCreationOptions JSON as per WebAuthn spec
        val requestJson = buildCreationOptionsJson(
            rpId = rpId,
            rpName = rpName,
            userId = userId,
            userName = userName,
            challenge = challenge,
            excludeCredentialIds = excludeCredentialIds,
            authenticatorAttachment = authenticatorAttachment,
            userVerification = userVerification
        )

        val createRequest = CreatePublicKeyCredentialRequest(
            requestJson = requestJson
        )

        val result = credentialManager.createCredential(activity, createRequest)
        val credential = result.data.getString("androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON")
            ?: throw FingerprintAuthException(
                "No registration response received from Credential Manager.",
                recoverable = true
            )

        return parseCreateResponse(credential)
    }

    override suspend fun getAssertion(
        rpId: String,
        challenge: String,
        allowCredentialIds: List<String>,
        userVerification: String
    ): WebAuthnAssertionResult {
        val activity = WebAuthnActivityTracker.currentActivity()
            ?: throw FingerprintAuthException(
                "WebAuthn requires an active Activity context.",
                recoverable = true
            )

        val requestJson = buildAssertionOptionsJson(
            rpId = rpId,
            challenge = challenge,
            allowCredentialIds = allowCredentialIds,
            userVerification = userVerification
        )

        val getRequest = GetCredentialRequest(
            listOf(GetPublicKeyCredentialOption(requestJson = requestJson))
        )

        val result = credentialManager.getCredential(activity, getRequest)
        val publicKeyCredential = result.credential as? PublicKeyCredential
            ?: throw FingerprintAuthException(
                "Expected PublicKeyCredential but got ${result.credential.type}.",
                recoverable = true
            )

        return parseAssertionResponse(publicKeyCredential.authenticationResponseJson)
    }

    /**
     * Build the JSON for PublicKeyCredentialCreationOptions.
     * See: https://www.w3.org/TR/webauthn-3/#dictdef-publickeycredentialcreationoptions
     */
    private fun buildCreationOptionsJson(
        rpId: String,
        rpName: String,
        userId: String,
        userName: String,
        challenge: String,
        excludeCredentialIds: List<String>,
        authenticatorAttachment: String,
        userVerification: String
    ): String {
        // userId needs to be base64url-encoded bytes for WebAuthn
        val userIdB64 = Base64.encodeToString(
            userId.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

        val json = buildJsonObject {
            put("rp", buildJsonObject {
                put("id", JsonPrimitive(rpId))
                put("name", JsonPrimitive(rpName))
            })
            put("user", buildJsonObject {
                put("id", JsonPrimitive(userIdB64))
                put("name", JsonPrimitive(userName))
                put("displayName", JsonPrimitive(userName))
            })
            put("challenge", JsonPrimitive(challenge))
            put("pubKeyCredParams", buildJsonArray {
                // ES256 (ECDSA with SHA-256) — most common for FIDO2
                add(buildJsonObject {
                    put("type", JsonPrimitive("public-key"))
                    put("alg", JsonPrimitive(-7))
                })
                // RS256 (RSASSA-PKCS1-v1_5 with SHA-256) — fallback
                add(buildJsonObject {
                    put("type", JsonPrimitive("public-key"))
                    put("alg", JsonPrimitive(-257))
                })
            })
            put("timeout", JsonPrimitive(60000))
            put("attestation", JsonPrimitive("direct"))
            put("authenticatorSelection", buildJsonObject {
                put("authenticatorAttachment", JsonPrimitive(authenticatorAttachment))
                put("requireResidentKey", JsonPrimitive(false))
                put("residentKey", JsonPrimitive("discouraged"))
                put("userVerification", JsonPrimitive(userVerification))
            })
            if (excludeCredentialIds.isNotEmpty()) {
                put("excludeCredentials", buildJsonArray {
                    for (credId in excludeCredentialIds) {
                        add(buildJsonObject {
                            put("type", JsonPrimitive("public-key"))
                            put("id", JsonPrimitive(credId))
                        })
                    }
                })
            }
        }

        return json.toString()
    }

    /**
     * Build the JSON for PublicKeyCredentialRequestOptions (assertion).
     */
    private fun buildAssertionOptionsJson(
        rpId: String,
        challenge: String,
        allowCredentialIds: List<String>,
        userVerification: String
    ): String {
        val json = buildJsonObject {
            put("rpId", JsonPrimitive(rpId))
            put("challenge", JsonPrimitive(challenge))
            put("timeout", JsonPrimitive(60000))
            put("userVerification", JsonPrimitive(userVerification))
            if (allowCredentialIds.isNotEmpty()) {
                put("allowCredentials", buildJsonArray {
                    for (credId in allowCredentialIds) {
                        add(buildJsonObject {
                            put("type", JsonPrimitive("public-key"))
                            put("id", JsonPrimitive(credId))
                        })
                    }
                })
            }
        }
        return json.toString()
    }

    /**
     * Parse the Credential Manager registration response JSON.
     */
    private fun parseCreateResponse(responseJson: String): WebAuthnCreateResult {
        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(responseJson).jsonObject

        val id = root["id"]?.jsonPrimitive?.content ?: ""
        val response = root["response"]?.jsonObject

        val clientDataJson = response?.get("clientDataJSON")?.jsonPrimitive?.content ?: ""
        val attestationObject = response?.get("attestationObject")?.jsonPrimitive?.content ?: ""

        // Extract transports if available
        val transports = response?.get("transports")?.jsonArray
            ?.joinToString(",") { it.jsonPrimitive.content } ?: ""

        // The public key is embedded in the attestation object; for server verification
        // we send the full attestation. The server's WebAuthnService handles extraction.
        // We also extract publicKey from response if available.
        val publicKey = response?.get("publicKey")?.jsonPrimitive?.content ?: attestationObject

        val publicKeyAlgorithm = response?.get("publicKeyAlgorithm")
            ?.jsonPrimitive?.content?.let { algCodeToName(it) } ?: "ES256"

        return WebAuthnCreateResult(
            credentialId = id,
            publicKey = publicKey,
            publicKeyAlgorithm = publicKeyAlgorithm,
            attestationFormat = "packed",
            transports = transports,
            clientDataJson = clientDataJson
        )
    }

    /**
     * Parse the Credential Manager assertion response JSON.
     */
    private fun parseAssertionResponse(responseJson: String): WebAuthnAssertionResult {
        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(responseJson).jsonObject

        val id = root["id"]?.jsonPrimitive?.content ?: ""
        val response = root["response"]?.jsonObject

        val authenticatorData = response?.get("authenticatorData")?.jsonPrimitive?.content ?: ""
        val clientDataJson = response?.get("clientDataJSON")?.jsonPrimitive?.content ?: ""
        val signature = response?.get("signature")?.jsonPrimitive?.content ?: ""

        return WebAuthnAssertionResult(
            credentialId = id,
            authenticatorData = authenticatorData,
            clientDataJson = clientDataJson,
            signature = signature
        )
    }

    private fun algCodeToName(code: String): String {
        return when (code) {
            "-7" -> "ES256"
            "-257" -> "RS256"
            "-35" -> "ES384"
            "-36" -> "ES512"
            else -> "ES256"
        }
    }
}

/**
 * Tracks the current resumed Activity for Credential Manager operations.
 */
private object WebAuthnActivityTracker : Application.ActivityLifecycleCallbacks {
    private var activityRef: WeakReference<Activity>? = null
    private var isRegistered = false

    fun registerIfNeeded(application: Application) {
        if (!isRegistered) {
            application.registerActivityLifecycleCallbacks(this)
            isRegistered = true
        }
    }

    fun currentActivity(): Activity? = activityRef?.get()

    override fun onActivityResumed(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
