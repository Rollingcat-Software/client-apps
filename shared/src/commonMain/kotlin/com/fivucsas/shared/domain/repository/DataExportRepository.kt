package com.fivucsas.shared.domain.repository

/**
 * GDPR Art. 20 / KVKK data-portability repository port.
 *
 * Thin wrapper around the Identity Core API endpoint
 * `GET /api/v1/users/{id}/export` that returns a JSON bundle of
 * personal data the controller holds about the user.
 *
 * Hexagonal role: PORT (primary/driving) — domain depends on this
 * abstraction; [com.fivucsas.shared.data.repository.DataExportRepositoryImpl]
 * is the Ktor-based adapter.
 *
 * Follows the same Result<T> convention used by all other repositories
 * in this module.
 */
interface DataExportRepository {
    /**
     * Fetch the user's personal-data export bundle as a JSON string
     * (the server serializes a [kotlin.collections.Map] to JSON with
     * `Content-Type: application/json` and `Content-Disposition: attachment`).
     *
     * On HTTP 429 the returned [Result.failure] carries a
     * [DataExportRateLimitedException] with the `Retry-After` seconds
     * parsed from the response header (null if the header was missing).
     *
     * @param userId the UUID of the user to export; self-export for regular
     *               users, tenant-scoped for admins, any user for ROOT.
     * @return success with the raw JSON body, or failure on network / auth /
     *         rate-limit error.
     */
    suspend fun exportUserData(userId: String): Result<String>
}

/**
 * Thrown when the server returns HTTP 429 for a data-export request.
 * [retryAfterSeconds] is parsed from the `Retry-After` header; it may be
 * null if the header was absent or unparsable.
 */
class DataExportRateLimitedException(
    val retryAfterSeconds: Long?,
    message: String = "Data export rate limit exceeded",
) : RuntimeException(message)
