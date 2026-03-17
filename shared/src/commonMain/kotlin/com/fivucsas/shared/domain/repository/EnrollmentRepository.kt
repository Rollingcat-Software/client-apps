package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.Enrollment

interface EnrollmentRepository {
    suspend fun getEnrollments(userId: String): Result<List<Enrollment>>
}
