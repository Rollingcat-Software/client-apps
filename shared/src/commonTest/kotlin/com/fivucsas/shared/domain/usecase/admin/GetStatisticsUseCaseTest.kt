package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.test.FakeUserRepository
import com.fivucsas.shared.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GetStatisticsUseCaseTest {

    @Test
    fun `invoke should return statistics from repository`() = runTest {
        // Given
        val repository = FakeUserRepository()
        val useCase = GetStatisticsUseCase(repository)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertNotNull(stats)
        assertEquals(TestData.testStatistics.totalUsers, stats.totalUsers)
        assertEquals(TestData.testStatistics.activeUsers, stats.activeUsers)
        assertEquals(TestData.testStatistics.successRate, stats.successRate)
    }

    @Test
    fun `invoke should calculate correct values`() = runTest {
        // Given
        val repository = FakeUserRepository()
        val useCase = GetStatisticsUseCase(repository)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(100, stats.totalUsers)
        assertEquals(85, stats.activeUsers)
        assertEquals(95.5, stats.successRate)
    }

    @Test
    fun `invoke should return failure when repository throws error`() = runTest {
        // Given
        val repository = FakeUserRepository()
        repository.shouldThrowError = true
        repository.errorMessage = "Statistics unavailable"
        val useCase = GetStatisticsUseCase(repository)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Statistics unavailable", result.exceptionOrNull()?.message)
    }
}
