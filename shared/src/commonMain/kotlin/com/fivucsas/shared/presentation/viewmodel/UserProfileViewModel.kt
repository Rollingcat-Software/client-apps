package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.domain.usecase.admin.GetMyProfileUseCase
import com.fivucsas.shared.platform.INetworkMonitor
import com.fivucsas.shared.presentation.state.UserProfileUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val offlineCache: OfflineCache,
    private val networkMonitor: INetworkMonitor
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(UserProfileUiState())
    val state: StateFlow<UserProfileUiState> = _state.asStateFlow()

    fun loadProfile() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        if (!networkMonitor.checkConnectivity()) {
            // Offline: show cached profile if available
            val cached = offlineCache.getCachedProfile()
            if (cached != null) {
                val user = User(
                    id = cached.id,
                    name = cached.name,
                    email = cached.email,
                    idNumber = cached.idNumber,
                    phoneNumber = cached.phoneNumber,
                    status = try { UserStatus.valueOf(cached.status) } catch (_: Exception) { UserStatus.ACTIVE },
                    enrollmentDate = cached.enrollmentDate,
                    hasBiometric = cached.hasBiometric,
                    role = UserRole.fromString(cached.role)
                )
                _state.update {
                    UserProfileUiState(
                        user = user,
                        isLoading = false,
                        isOfflineData = true,
                        lastSyncTimestamp = cached.lastSyncTimestamp
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No internet connection and no cached data available."
                    )
                }
            }
            return
        }

        scope.launch {
            getMyProfileUseCase().fold(
                onSuccess = { user ->
                    // Cache profile for offline use
                    offlineCache.cacheUserProfile(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        role = user.role.name,
                        status = user.status.name,
                        idNumber = user.idNumber,
                        phoneNumber = user.phoneNumber,
                        hasBiometric = user.hasBiometric,
                        enrollmentDate = user.enrollmentDate
                    )
                    _state.update {
                        UserProfileUiState(
                            user = user,
                            isLoading = false,
                            isOfflineData = false
                        )
                    }
                },
                onFailure = { error ->
                    // On failure, try to show cached data with error
                    val cached = offlineCache.getCachedProfile()
                    if (cached != null) {
                        val fallbackUser = User(
                            id = cached.id,
                            name = cached.name,
                            email = cached.email,
                            idNumber = cached.idNumber,
                            phoneNumber = cached.phoneNumber,
                            status = try { UserStatus.valueOf(cached.status) } catch (_: Exception) { UserStatus.ACTIVE },
                            enrollmentDate = cached.enrollmentDate,
                            hasBiometric = cached.hasBiometric,
                            role = UserRole.fromString(cached.role)
                        )
                        _state.update {
                            UserProfileUiState(
                                user = fallbackUser,
                                isLoading = false,
                                isOfflineData = true,
                                lastSyncTimestamp = cached.lastSyncTimestamp,
                                errorMessage = ErrorMapper.mapToUserMessage(error, "load profile") +
                                    " Showing cached data."
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = ErrorMapper.mapToUserMessage(error, "load profile")
                            )
                        }
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun dispose() {
        scope.coroutineContext[Job]?.cancel()
    }
}
