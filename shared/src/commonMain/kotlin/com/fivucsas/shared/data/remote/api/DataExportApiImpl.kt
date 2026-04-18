package com.fivucsas.shared.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType

/**
 * Ktor adapter for [DataExportApi].
 *
 * We read the body as text rather than decoding to a Map — the web-app
 * treats the response as an opaque Blob and writes it to disk verbatim so
 * the user can inspect / machine-parse the JSON later. Keeping it as a
 * String avoids having to mirror the server's evolving bundle schema
 * (users, audit_logs, enrollments, sessions, …) in the mobile client.
 */
class DataExportApiImpl(
    private val client: HttpClient
) : DataExportApi {

    companion object {
        private const val BASE_PATH = "users"
    }

    override suspend fun exportUserData(userId: String): String {
        val response = client.get("$BASE_PATH/$userId/export") {
            accept(ContentType.Application.Json)
        }
        return response.bodyAsText()
    }
}
