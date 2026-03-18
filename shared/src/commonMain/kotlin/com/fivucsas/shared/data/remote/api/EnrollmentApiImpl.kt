package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.EnrollmentDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class EnrollmentApiImpl(
    private val client: HttpClient
) : EnrollmentApi {

    override suspend fun getEnrollments(userId: String): List<EnrollmentDto> {
        return client.get("users/$userId/enrollments").body()
    }
}
