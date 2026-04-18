package com.fivucsas.shared.data.remote.api

/**
 * Port for the GDPR / KVKK data-portability endpoint.
 *
 * Endpoint:
 *   GET /api/v1/users/{id}/export
 *   Accept: application/json
 *   Response: application/json body + Content-Disposition: attachment; filename="…"
 *   429: rate-limit (1 export / hour / caller), Retry-After header in seconds.
 */
interface DataExportApi {
    /**
     * Returns the export bundle as the raw JSON body string.
     * Throws [io.ktor.client.plugins.ClientRequestException] on 4xx (including 429),
     * [io.ktor.client.plugins.ServerResponseException] on 5xx.
     */
    suspend fun exportUserData(userId: String): String
}
