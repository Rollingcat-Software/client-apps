package com.fivucsas.shared.domain.model

/**
 * Domain model for an OAuth2 client application registered in the Developer Portal.
 */
data class OAuth2Client(
    val id: String,
    val appName: String,
    val clientId: String,
    val clientSecret: String? = null,
    val redirectUris: List<String>,
    val scopes: List<String>,
    val status: String,
    val createdAt: String
)
