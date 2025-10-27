package com.fivucsas.mobile.android

import android.content.Context
import com.fivucsas.mobile.data.local.TokenStorage
import com.fivucsas.mobile.data.remote.ApiClient
import com.fivucsas.mobile.data.repository.AuthRepositoryImpl
import com.fivucsas.mobile.data.repository.BiometricRepositoryImpl
import com.fivucsas.mobile.domain.repository.AuthRepository
import com.fivucsas.mobile.domain.repository.BiometricRepository
import com.fivucsas.mobile.domain.usecase.EnrollFaceUseCase
import com.fivucsas.mobile.domain.usecase.LoginUseCase
import com.fivucsas.mobile.domain.usecase.RegisterUseCase
import com.fivucsas.mobile.domain.usecase.VerifyFaceUseCase
import com.fivucsas.mobile.platform.AndroidTokenStorage
import com.fivucsas.mobile.presentation.biometric.BiometricViewModel
import com.fivucsas.mobile.presentation.login.LoginViewModel
import com.fivucsas.mobile.presentation.register.RegisterViewModel

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
