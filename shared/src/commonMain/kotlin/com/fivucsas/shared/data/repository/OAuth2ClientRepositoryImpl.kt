package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.OAuth2ClientApi
import com.fivucsas.shared.data.remote.dto.RegisterOAuth2ClientRequest
import com.fivucsas.shared.data.remote.dto.toDomain
import com.fivucsas.shared.domain.model.OAuth2Client
import com.fivucsas.shared.domain.repository.OAuth2ClientRepository

class OAuth2ClientRepositoryImpl(
    private val oAuth2ClientApi: OAuth2ClientApi
) : OAuth2ClientRepository {

    override suspend fun listClients(): Result<List<OAuth2Client>> {
        return try {
            val clients = oAuth2ClientApi.listClients().map { it.toDomain() }
            Result.success(clients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerClient(
        appName: String,
        redirectUris: String,
        scopes: List<String>
    ): Result<OAuth2Client> {
        return try {
            val request = RegisterOAuth2ClientRequest(
                appName = appName,
                redirectUris = redirectUris,
                scopes = scopes
            )
            val client = oAuth2ClientApi.registerClient(request).toDomain()
            Result.success(client)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteClient(id: String): Result<Unit> {
        return try {
            oAuth2ClientApi.deleteClient(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
