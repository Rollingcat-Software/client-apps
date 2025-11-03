package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.test.FakeUserRepository
import com.fivucsas.shared.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetUsersUseCaseTest {
    
    @Test
    fun `invoke should return list of users from repository`() = runTest {
        // Given
        val repository = FakeUserRepository()
        repository.addUsers(TestData.testUser1, TestData.testUser2)
        val useCase = GetUsersUseCase(repository)
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertEquals(2, users.size)
        assertTrue(users.contains(TestData.testUser1))
        assertTrue(users.contains(TestData.testUser2))
    }
    
    @Test
    fun `invoke should return empty list when no users exist`() = runTest {
        // Given
        val repository = FakeUserRepository()
        val useCase = GetUsersUseCase(repository)
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertTrue(users.isEmpty())
    }
    
    @Test
    fun `invoke should return large list of users`() = runTest {
        // Given
        val repository = FakeUserRepository()
        val testUsers = TestData.createTestUsers(100)
        repository.setUsers(testUsers)
        val useCase = GetUsersUseCase(repository)
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()!!.size)
    }
    
    @Test
    fun `invoke should return failure when repository throws error`() = runTest {
        // Given
        val repository = FakeUserRepository()
        repository.shouldThrowError = true
        repository.errorMessage = "Database error"
        val useCase = GetUsersUseCase(repository)
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }
}
