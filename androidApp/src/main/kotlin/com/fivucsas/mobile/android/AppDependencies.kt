package com.fivucsas.mobile.android

import android.content.Context
import com.fivucsas.shared.data.local.TokenStorage
import com.fivucsas.shared.data.remote.ApiClient
import com.fivucsas.shared.data.repository.AuthRepositoryImpl
import com.fivucsas.shared.data.repository.BiometricRepositoryImpl
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.domain.usecase.enrollment.EnrollUserUseCase
import com.fivucsas.shared.domain.usecase.auth.LoginUseCase
import com.fivucsas.shared.domain.usecase.auth.RegisterUseCase
import com.fivucsas.shared.domain.usecase.verification.VerifyUserUseCase
import com.fivucsas.shared.platform.AndroidTokenStorage
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel

// Simple dependency injection for MVP
class AppDependencies(context: Context) {

    // Storage
    private val tokenStorage: TokenStorage = AndroidTokenStorage(context)

    // API Client
    private val apiClient = ApiClient(
        tokenProvider = { tokenStorage.getToken() }
    )

    // Repositories
    val authRepository: AuthRepository = AuthRepositoryImpl(apiClient, tokenStorage)
    val biometricRepository: BiometricRepository = BiometricRepositoryImpl(apiClient)

    // Use Cases
    private val loginUseCase = LoginUseCase(authRepository)
    private val registerUseCase = RegisterUseCase(authRepository)
    private val enrollFaceUseCase = EnrollFaceUseCase(biometricRepository)
    private val verifyFaceUseCase = VerifyFaceUseCase(biometricRepository)

    // ViewModels
    val loginViewModel = LoginViewModel(loginUseCase)
    val registerViewModel = RegisterViewModel(registerUseCase)
    val biometricViewModel = BiometricViewModel(enrollFaceUseCase, verifyFaceUseCase)
}
