package com.fivucsas.shared.test

import com.fivucsas.shared.data.remote.api.IdentityApi
import com.fivucsas.shared.data.remote.dto.StatisticsDto
import com.fivucsas.shared.data.remote.dto.UserDto

class FakeIdentityApi : IdentityApi {

    private val users = mutableListOf(
        UserDto(
            id = "user-1",
            name = "Ahmet Yilmaz",
            email = "ahmet@example.com",
            idNumber = "ID001",
            phoneNumber = "+1234567890",
            status = "ACTIVE",
            enrollmentDate = "2025-01-01",
            hasBiometric = true,
            role = "USER"
        ),
        UserDto(
            id = "user-2",
            name = "Ayse Demir",
            email = "ayse@example.com",
            idNumber = "ID002",
            phoneNumber = "+0987654321",
            status = "ACTIVE",
            enrollmentDate = "2025-01-02",
            hasBiometric = false,
            role = "USER"
        ),
        UserDto(
            id = "user-3",
            name = "Mehmet Kaya",
            email = "mehmet@example.com",
            idNumber = "ID003",
            phoneNumber = "+1111111111",
            status = "INACTIVE",
            enrollmentDate = "2025-01-03",
            hasBiometric = false,
            role = "USER"
        )
    )

    override suspend fun getUsers(): List<UserDto> = users.toList()

    override suspend fun getUserById(id: String): UserDto {
        return users.find { it.id == id }
            ?: throw NoSuchElementException("User not found: $id")
    }

    override suspend fun createUser(user: UserDto): UserDto {
        val newUser = user.copy(id = "new-${users.size + 1}")
        users.add(newUser)
        return newUser
    }

    override suspend fun updateUser(id: String, user: UserDto): UserDto {
        val index = users.indexOfFirst { it.id == id }
        if (index == -1) throw NoSuchElementException("User not found: $id")
        users[index] = user.copy(id = id)
        return users[index]
    }

    override suspend fun deleteUser(id: String) {
        if (!users.removeAll { it.id == id }) {
            throw NoSuchElementException("User not found: $id")
        }
    }

    override suspend fun searchUsers(query: String): List<UserDto> {
        return users.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true) ||
                    it.idNumber.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getStatistics(): StatisticsDto {
        return StatisticsDto(
            totalUsers = users.size,
            activeUsers = users.count { it.status == "ACTIVE" },
            pendingVerifications = 0,
            verificationsToday = 10,
            successRate = 95.0,
            failedAttempts = 1
        )
    }
}
