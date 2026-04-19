package com.fivucsas.desktop.auth

import kotlinx.serialization.Serializable

/**
 * OAuth 2.0 token bundle returned by the `/oauth2/token` endpoint.
 *
 * Fields map to RFC 6749 §5.1 (access/refresh token) + OIDC Core §3.1.3.3 (id_token).
 *
 * @property accessToken Bearer token used on API calls
 * @property idToken OIDC id_token (JWT with user claims); may be null when scope=openid is absent
 * @property refreshToken Refresh token; may be null when offline_access is absent
 * @property expiresAt Epoch millis when accessToken expires (NOT seconds, NOT duration)
 */
@Serializable
data class AccessTokens(
    val accessToken: String,
    val idToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: Long,
)
