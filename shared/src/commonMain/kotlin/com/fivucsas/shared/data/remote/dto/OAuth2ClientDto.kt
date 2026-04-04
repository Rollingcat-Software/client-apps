package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.OAuth2Client
import kotlinx.serialization.Serializable

/**
 * OAuth2 Client DTO -- server returns camelCase JSON (Spring Boot / Jackson)
 */
@Serializable
data class OAuth2ClientDto(
    val id: String = "",
    val appName: String = "",
    val clientId: String = "",
    val clientSecret: String? = null,
    val redirectUris: List<String> = emptyList(),
    val scopes: List<String> = emptyList(),
    val status: String = "ACTIVE",
    val createdAt: String = ""
)

/**
 * Request body for registering a new OAuth2 client
 */
@Serializable
data class RegisterOAuth2ClientRequest(
    val appName: String,
    val redirectUris: String,
    val scopes: List<String>
)

fun OAuth2ClientDto.toDomain(): OAuth2Client = OAuth2Client(
    id = id,
    appName = appName,
    clientId = clientId,
    clientSecret = clientSecret,
    redirectUris = redirectUris,
    scopes = scopes,
    status = status,
    createdAt = createdAt
)
