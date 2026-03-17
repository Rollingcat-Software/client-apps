package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.EnrollmentApi
import com.fivucsas.shared.data.remote.dto.toDomain
import com.fivucsas.shared.domain.model.Enrollment
import com.fivucsas.shared.domain.repository.EnrollmentRepository

class EnrollmentRepositoryImpl(
    private val enrollmentApi: EnrollmentApi
) : EnrollmentRepository {

    override suspend fun getEnrollments(userId: String): Result<List<Enrollment>> {
        return try {
            val enrollments = enrollmentApi.getEnrollments(userId).map { it.toDomain() }
            Result.success(enrollments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
