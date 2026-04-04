package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.OAuth2ClientDto
import com.fivucsas.shared.data.remote.dto.RegisterOAuth2ClientRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class OAuth2ClientApiImpl(
    private val client: HttpClient
) : OAuth2ClientApi {

    override suspend fun listClients(): List<OAuth2ClientDto> {
        return client.get("oauth2/clients").body()
    }

    override suspend fun registerClient(request: RegisterOAuth2ClientRequest): OAuth2ClientDto {
        return client.post("oauth2/clients") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun deleteClient(id: String) {
        client.delete("oauth2/clients/$id")
    }
}
