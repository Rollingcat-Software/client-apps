package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.PagedUserResponse
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
 *
 * GET /users returns a paginated response: { content: [...], page, size, totalPages }
 * GET /users/search returns a plain list: [...]
 * GET /users/{id} returns a single UserDto
 *
 * Note: There is no /users/me endpoint on the server.
 * User profile info is available from the auth response's `user` field.
 */
class IdentityApiImpl(
    private val client: HttpClient
) : IdentityApi {

    companion object {
        private const val BASE_PATH = "users"
        private const val STATS_PATH = "statistics"
    }

    override suspend fun getUsers(): List<UserDto> {
        // Server returns paginated Spring Boot Page<UserResponse>
        val pagedResponse: PagedUserResponse = client.get(BASE_PATH) {
            parameter("size", 100)
        }.body()
        return pagedResponse.content
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
        // Server returns a plain list from /users/search
        return client.get("$BASE_PATH/search") {
            parameter("query", query)
        }.body()
    }

    override suspend fun getStatistics(): StatisticsDto {
        return client.get(STATS_PATH).body()
    }

    override suspend fun getMyProfile(): UserDto {
        // Server provides user profile at GET /auth/me (not /users/me)
        return client.get("auth/me").body()
    }

    override suspend fun healthCheck(): Boolean {
        return try {
            // The server's health check is at /actuator/health (outside /api/v1/ prefix)
            // Use a relative path that Ktor will resolve against the base URL
            client.get("../../../actuator/health")
            true
        } catch (_: Exception) {
            // Fallback: if the relative path resolution fails, just check if the base URL is reachable
            try {
                client.get("auth/me")
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}
