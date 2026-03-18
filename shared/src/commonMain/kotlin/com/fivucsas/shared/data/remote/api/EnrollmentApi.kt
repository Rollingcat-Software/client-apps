package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.EnrollmentDto

/**
 * Enrollment API interface
 *
 * Endpoints:
 * - GET /users/{userId}/enrollments → getEnrollments()
 */
interface EnrollmentApi {
    suspend fun getEnrollments(userId: String): List<EnrollmentDto>
}
