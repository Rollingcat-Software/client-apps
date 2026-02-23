package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.repository.UserRepository

/**
 * Use case for getting system statistics
 *
 * Returns:
 * - Total users
 * - Active users
 * - Pending verifications
 * - Today's enrollments
 * - Success rate
 * - Failed attempts
 */
open class GetStatisticsUseCase(
    private val userRepository: UserRepository
) {
    open suspend operator fun invoke(): Result<Statistics> {
        return userRepository.getStatistics()
    }
}
