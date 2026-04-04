package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.OAuth2ClientDto
import com.fivucsas.shared.data.remote.dto.RegisterOAuth2ClientRequest

/**
 * OAuth2 Client API interface
 *
 * Endpoints:
 * - GET    /oauth2/clients           -> listClients()
 * - POST   /oauth2/clients           -> registerClient()
 * - DELETE /oauth2/clients/{id}      -> deleteClient()
 */
interface OAuth2ClientApi {
    suspend fun listClients(): List<OAuth2ClientDto>
    suspend fun registerClient(request: RegisterOAuth2ClientRequest): OAuth2ClientDto
    suspend fun deleteClient(id: String)
}
