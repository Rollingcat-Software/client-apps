package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.StatisticsDto
import com.fivucsas.shared.data.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Identity API implementation
 * Handles user management and statistics
 */
class IdentityApiImpl(
    private val client: HttpClient
) : IdentityApi {

    companion object {
        private const val BASE_PATH = "users"
        private const val STATS_PATH = "statistics"
    }

    override suspend fun getUsers(): List<UserDto> {
        return client.get(BASE_PATH).body()
    }

    override suspend fun getUserById(id: String): UserDto {
        return client.get("$BASE_PATH/$id").body()
    }

    override suspend fun createUser(user: UserDto): UserDto {
        return client.post(BASE_PATH) {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }

    override suspend fun updateUser(id: String, user: UserDto): UserDto {
        return client.put("$BASE_PATH/$id") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }

    override suspend fun deleteUser(id: String) {
        client.delete("$BASE_PATH/$id")
    }

    override suspend fun searchUsers(query: String): List<UserDto> {
        return client.get("$BASE_PATH/search") {
            parameter("q", query)
        }.body()
    }

    override suspend fun getStatistics(): StatisticsDto {
        return client.get(STATS_PATH).body()
    }
}
