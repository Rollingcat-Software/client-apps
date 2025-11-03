package com.fivucsas.shared.data.repository

import com.fivucsas.shared.test.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for UserRepositoryImpl using mock data
 * These tests verify the repository works correctly with mock data fallback
 */
class UserRepositoryImplTest {
    
    @Test
    fun `getUsers should return success with mock data`() = runTest {
        // Given
        val repository = UserRepositoryImpl()
        
        // When
        val result = repository.getUsers()
        
        // Then
        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertNotNull(users)
        assertTrue(users.isNotEmpty(), "Should return mock users")
    }
    
    @Test
    fun `getUserById should return success when ID exists`() = runTest {
        // Given
        val repository = UserRepositoryImpl()
        val allUsers = repository.getUsers().getOrNull()!!
        val firstUser = allUsers.first()
        
        // When
        val result = repository.getUserById(firstUser.id)
        
        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()!!
        assertNotNull(user)
        assertEquals(firstUser.id, user.id)
        assertEquals(firstUser.email, user.email)
    }
    
    @Test
    fun `getUserById should return failure when ID does not exist`() = runTest {
        // Given
        val repository = UserRepositoryImpl()
        
        // When
        val result = repository.getUserById("non-existent-id")
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `searchUsers should filter by name`() = runTest {
        // Given
        val repository = UserRepositoryImpl()
        
        // When
        val result = repository.searchUsers("Ahmet")
        
        // Then
        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertTrue(users.isNotEmpty())
        assertTrue(users.all {
            it.name.contains("Ahmet", ignoreCase = true)
        })
    }
    
    @Test
    fun `searchUsers should filter by email`() = runTest {
        // Given
        val repository = UserRepositoryImpl()
        
        // When
        val result = repository.searchUsers("example.com")
        
        // Then
        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertTrue(users.all {
            it.email.contains("example.com", ignoreCase = true)
        })
    }
    
    @Test
    fun `searchUsers should return empty list for no matches`() = runTest {
        // Given
        val repository = UserRepositoryImpl()
        
        // When
        val result = repository.searchUsers("NonExistentName12345XYZ")
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
    
    @Test
    fun `searchUsers should be case insensitive`() = runTest {
        // Given
        val repository = UserRepositoryImpl()
        
        // When
        val resultLower = repository.searchUsers("ahmet")
        val resultUpper = repository.searchUsers("AHMET")
        val resultMixed = repository.searchUsers("Ahmet")
        
        // Then
        assertTrue(resultLower.isSuccess)
        assertTrue(resultUpper.isSuccess)
        assertTrue(resultMixed.isSuccess)
        assertEquals(resultLower.getOrNull()!!.size, resultUpper.getOrNull()!!.size)
        assertEquals(resultLower.getOrNull()!!.size, resultMixed.getOrNull()!!.size)
    }
    
    @Test
    fun `getStatistics should return valid statistics`() = runTest {
        // Given
        val repository = UserRepositoryImpl()
        
        // When
        val result = repository.getStatistics()
        
        // Then
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertNotNull(stats)
        assertTrue(stats.totalUsers >= 0)
        assertTrue(stats.activeUsers >= 0)
        assertTrue(stats.successRate >= 0.0 && stats.successRate <= 100.0)
    }
}
