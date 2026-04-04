package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.CreateRoleRequestDto
import com.fivucsas.shared.data.remote.dto.PermissionDto
import com.fivucsas.shared.data.remote.dto.RoleDto
import com.fivucsas.shared.data.remote.dto.UpdateRoleRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class RolesApiImpl(
    private val client: HttpClient
) : RolesApi {

    @Serializable
    private data class UpdatePermissionsRequest(
        val permissionNames: List<String>
    )

    override suspend fun getRoles(): List<RoleDto> {
        return client.get("roles").body()
    }

    override suspend fun createRole(request: CreateRoleRequestDto): RoleDto {
        return client.post("roles") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun updateRole(id: String, request: UpdateRoleRequestDto): RoleDto {
        return client.put("roles/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun deleteRole(id: String) {
        client.delete("roles/$id")
    }

    override suspend fun getPermissions(): List<PermissionDto> {
        return client.get("permissions").body()
    }

    override suspend fun updateRolePermissions(roleId: String, permissionNames: List<String>) {
        client.put("roles/$roleId/permissions") {
            contentType(ContentType.Application.Json)
            setBody(UpdatePermissionsRequest(permissionNames = permissionNames))
        }
    }
}
