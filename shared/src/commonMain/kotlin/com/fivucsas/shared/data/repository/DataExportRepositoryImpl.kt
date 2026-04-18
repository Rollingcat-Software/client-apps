package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.DataExportApi
import com.fivucsas.shared.domain.repository.DataExportRateLimitedException
import com.fivucsas.shared.domain.repository.DataExportRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

/**
 * Adapter implementation of [DataExportRepository].
 *
 * Maps Ktor's [ClientRequestException] 429 into a typed
 * [DataExportRateLimitedException] so the ViewModel can distinguish
 * rate-limit hits from generic failures without reaching into HTTP
 * internals.
 */
class DataExportRepositoryImpl(
    private val dataExportApi: DataExportApi
) : DataExportRepository {

    override suspend fun exportUserData(userId: String): Result<String> {
        return try {
            Result.success(dataExportApi.exportUserData(userId))
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.TooManyRequests) {
                val retryAfter = e.response.headers["Retry-After"]
                    ?: e.response.headers["retry-after"]
                Result.failure(
                    DataExportRateLimitedException(
                        retryAfterSeconds = retryAfter?.toLongOrNull()
                    )
                )
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
