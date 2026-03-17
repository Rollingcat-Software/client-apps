package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.DeviceApi
import com.fivucsas.shared.data.remote.dto.toDomain
import com.fivucsas.shared.domain.model.Device
import com.fivucsas.shared.domain.model.WebAuthnCredential
import com.fivucsas.shared.domain.repository.DeviceRepository

class DeviceRepositoryImpl(
    private val deviceApi: DeviceApi
) : DeviceRepository {

    override suspend fun getDevices(userId: String): Result<List<Device>> {
        return try {
            val devices = deviceApi.getDevices(userId).map { it.toDomain() }
            Result.success(devices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeDevice(deviceId: String): Result<Unit> {
        return try {
            deviceApi.removeDevice(deviceId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWebAuthnCredentials(userId: String): Result<List<WebAuthnCredential>> {
        return try {
            val credentials = deviceApi.getWebAuthnCredentials(userId).map { it.toDomain() }
            Result.success(credentials)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
