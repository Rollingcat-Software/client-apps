package com.fivucsas.desktop.auth

/**
 * OAuth 2.0 / OIDC constants for the FIVUCSAS desktop client.
 *
 * Per the 2026-04-16 hosted-first pivot, the desktop app authenticates
 * via RFC 8252 loopback redirect to `verify.fivucsas.com/login`.
 * This mirrors the pattern used by `gh`, `gcloud`, `az`, and `aws` CLIs.
 */
object OAuthConfig {
    /** Hosted login URL. User is redirected here in the system browser. */
    const val AUTH_URL = "https://verify.fivucsas.com/login"

    /** Token endpoint at identity-core-api. Exchanges code for tokens. */
    const val TOKEN_URL = "https://api.fivucsas.com/oauth2/token"

    /** Public client identifier (PKCE-only, no client_secret). */
    const val CLIENT_ID = "fivucsas-desktop"

    /** Path used on the ephemeral loopback server. */
    const val REDIRECT_CALLBACK_PATH = "/callback"

    /** OIDC scopes requested at authorize time. */
    val SCOPES = listOf("openid", "profile", "email")

    /** Loopback address per RFC 8252 §7.3. Must be IPv4 literal, never `localhost`. */
    const val LOOPBACK_HOST = "127.0.0.1"
}
