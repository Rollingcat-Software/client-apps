package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.OAuth2Client

interface OAuth2ClientRepository {
    suspend fun listClients(): Result<List<OAuth2Client>>
    suspend fun registerClient(appName: String, redirectUris: String, scopes: List<String>): Result<OAuth2Client>
    suspend fun deleteClient(id: String): Result<Unit>
}
