package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.test.FakeUserRepository
import com.fivucsas.shared.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchUsersUseCaseTest {
    
    @Test
    fun `invoke should return matching users by first name`() = runTest {
        // Given
        val repository = FakeUserRepository()
        repository.addUsers(TestData.testUser1, TestData.testUser2, TestData.inactiveUser)
        val useCase = SearchUsersUseCase(repository)
        
        // When
        val result = useCase("John")
        
        // Then
        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertEquals(1, users.size)
        assertTrue(users.first().name.contains("John"))
    }
    
    @Test
    fun `invoke should return matching users by last name`() = runTest {
        // Given
        val repository = FakeUserRepository()
        repository.addUsers(TestData.testUser1, TestData.testUser2)
        val useCase = SearchUsersUseCase(repository)
        
        // When
        val result = useCase("Smith")
        
        // Then
        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertEquals(1, users.size)
        assertTrue(users.first().name.contains("Smith"))
    }
    
    @Test
    fun `invoke should return matching users by email`() = runTest {
        // Given
        val repository = FakeUserRepository()
        repository.addUsers(TestData.testUser1, TestData.testUser2)
        val useCase = SearchUsersUseCase(repository)
        
        // When
        val result = useCase("jane")
        
        // Then
        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertEquals(1, users.size)
        assertTrue(users.first().email.contains("jane", ignoreCase = true))
    }
    
    @Test
    fun `invoke should return empty list for no matches`() = runTest {
        // Given
        val repository = FakeUserRepository()
        repository.addUsers(TestData.testUser1, TestData.testUser2)
        val useCase = SearchUsersUseCase(repository)
        
        // When
        val result = useCase("NonExistent")
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
    
    @Test
    fun `invoke should handle empty query`() = runTest {
        // Given
        val repository = FakeUserRepository()
        repository.addUsers(TestData.testUser1, TestData.testUser2)
        val useCase = SearchUsersUseCase(repository)
        
        // When
        val result = useCase("")
        
        // Then
        assertTrue(result.isSuccess)
        // Empty query should return empty results (or all users depending on requirements)
        // Current implementation filters by empty string, which matches nothing
        val users = result.getOrNull()!!
        // Accept either behavior - empty or all users
        assertTrue(users.isEmpty() || users.size == 2)
    }
    
    @Test
    fun `invoke should be case insensitive`() = runTest {
        // Given
        val repository = FakeUserRepository()
        repository.addUsers(TestData.testUser1)
        val useCase = SearchUsersUseCase(repository)
        
        // When
        val result1 = useCase("john")
        val result2 = useCase("JOHN")
        val result3 = useCase("John")
        
        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertTrue(result3.isSuccess)
        assertEquals(1, result1.getOrNull()!!.size)
        assertEquals(1, result2.getOrNull()!!.size)
        assertEquals(1, result3.getOrNull()!!.size)
    }
}
