package com.fivucsas.shared.data.repository

import com.fivucsas.shared.test.FakeIdentityApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for UserRepositoryImpl using FakeIdentityApi
 * Verifies the repository correctly delegates to and maps from the API layer
 */
class UserRepositoryImplTest {

    private fun createRepository() = UserRepositoryImpl(FakeIdentityApi())

    @Test
    fun `getUsers should return success with data`() = runTest {
        val repository = createRepository()

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertNotNull(users)
        assertTrue(users.isNotEmpty(), "Should return users")
    }

    @Test
    fun `getUserById should return success when ID exists`() = runTest {
        val repository = createRepository()
        val allUsers = repository.getUsers().getOrNull()!!
        val firstUser = allUsers.first()

        val result = repository.getUserById(firstUser.id)

        assertTrue(result.isSuccess)
        val user = result.getOrNull()!!
        assertNotNull(user)
        assertEquals(firstUser.id, user.id)
        assertEquals(firstUser.email, user.email)
    }

    @Test
    fun `getUserById should return failure when ID does not exist`() = runTest {
        val repository = createRepository()

        val result = repository.getUserById("non-existent-id")

        assertTrue(result.isFailure)
    }

    @Test
    fun `searchUsers should filter by name`() = runTest {
        val repository = createRepository()

        val result = repository.searchUsers("Ahmet")

        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertTrue(users.isNotEmpty())
        assertTrue(users.all {
            it.name.contains("Ahmet", ignoreCase = true)
        })
    }

    @Test
    fun `searchUsers should filter by email`() = runTest {
        val repository = createRepository()

        val result = repository.searchUsers("example.com")

        assertTrue(result.isSuccess)
        val users = result.getOrNull()!!
        assertTrue(users.all {
            it.email.contains("example.com", ignoreCase = true)
        })
    }

    @Test
    fun `searchUsers should return empty list for no matches`() = runTest {
        val repository = createRepository()

        val result = repository.searchUsers("NonExistentName12345XYZ")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `searchUsers should be case insensitive`() = runTest {
        val repository = createRepository()

        val resultLower = repository.searchUsers("ahmet")
        val resultUpper = repository.searchUsers("AHMET")
        val resultMixed = repository.searchUsers("Ahmet")

        assertTrue(resultLower.isSuccess)
        assertTrue(resultUpper.isSuccess)
        assertTrue(resultMixed.isSuccess)
        assertEquals(resultLower.getOrNull()!!.size, resultUpper.getOrNull()!!.size)
        assertEquals(resultLower.getOrNull()!!.size, resultMixed.getOrNull()!!.size)
    }

    @Test
    fun `getStatistics should return valid statistics`() = runTest {
        val repository = createRepository()

        val result = repository.getStatistics()

        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertNotNull(stats)
        assertTrue(stats.totalUsers >= 0)
        assertTrue(stats.activeUsers >= 0)
        assertTrue(stats.successRate >= 0.0 && stats.successRate <= 100.0)
    }
}
