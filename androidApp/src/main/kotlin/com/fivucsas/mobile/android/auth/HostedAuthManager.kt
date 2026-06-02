package com.fivucsas.mobile.android.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Hosted-first OAuth 2.0 / OIDC login (RFC 8252) for the FIVUCSAS Android app.
 *
 * Opens `https://verify.fivucsas.com/login` in a Chrome Custom Tab, receives the
 * authorization code on the `fivucsas://callback` private-use URI scheme, and
 * exchanges it (PKCE S256, generated automatically by AppAuth) at
 * `https://api.fivucsas.com/api/v1/oauth2/token`.
 *
 * Locked architecture (2026-06-02): the auth ceremony (password + every MFA
 * method) lives on the hosted page; the native app is a thin OAuth client and
 * carries zero credential/MFA code. See `docs/plans/CLIENT_APPS_PARITY.md`.
 */
class HostedAuthManager(context: Context) {

    private val authService = AuthorizationService(context)

    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse(AUTH_ENDPOINT),
        Uri.parse(TOKEN_ENDPOINT),
    )

    /** Intent that launches the hosted login page in a Custom Tab. */
    fun authorizeIntent(): Intent {
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI),
        )
            .setScopes("openid", "profile", "email")
            .build()
        return authService.getAuthorizationRequestIntent(request)
    }

    /**
     * Exchange the authorization-code callback for tokens. Throws on OAuth error
     * or user cancellation (no [AuthorizationResponse] in the intent).
     */
    suspend fun exchange(callbackIntent: Intent): HostedTokens {
        val response = AuthorizationResponse.fromIntent(callbackIntent)
            ?: throw (AuthorizationException.fromIntent(callbackIntent)
                ?: IllegalStateException("No authorization response in callback"))

        val tokenResponse = performTokenRequest(response)
        val access = tokenResponse.accessToken
            ?: throw IllegalStateException("Token response missing access_token")
        val expiresIn = tokenResponse.accessTokenExpirationTime
            ?.let { (it - System.currentTimeMillis()) / 1000 }
            ?.coerceAtLeast(0)
            ?: 0L
        return HostedTokens(
            accessToken = access,
            idToken = tokenResponse.idToken,
            expiresIn = expiresIn,
        )
    }

    private suspend fun performTokenRequest(response: AuthorizationResponse): TokenResponse =
        suspendCancellableCoroutine { cont ->
            // Two-arg overload defaults to NoClientAuthentication — correct for a
            // public PKCE client (no client_secret).
            authService.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, ex ->
                if (tokenResponse != null) {
                    cont.resume(tokenResponse)
                } else {
                    cont.resumeWithException(ex ?: IllegalStateException("Token exchange failed"))
                }
            }
        }

    fun dispose() = authService.dispose()

    companion object {
        const val AUTH_ENDPOINT = "https://verify.fivucsas.com/login"
        const val TOKEN_ENDPOINT = "https://api.fivucsas.com/api/v1/oauth2/token"
        const val CLIENT_ID = "fivucsas-mobile"
        const val REDIRECT_URI = "fivucsas://callback"
    }
}

/** Minimal token bundle returned by the hosted login exchange. */
data class HostedTokens(
    val accessToken: String,
    val idToken: String?,
    val expiresIn: Long,
)
