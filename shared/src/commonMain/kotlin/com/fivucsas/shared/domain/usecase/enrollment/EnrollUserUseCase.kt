package com.fivucsas.shared.domain.usecase.enrollment

import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.EnrollmentData
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.domain.repository.UserRepository
import com.fivucsas.shared.domain.validation.ValidationResult
import com.fivucsas.shared.domain.validation.ValidationRules

/**
 * Use case for enrolling a new user
 * 
 * This encapsulates the complete business logic for user enrollment:
 * 1. Validate all user data
 * 2. Create user in system
 * 3. Enroll biometric data (face)
 * 4. Rollback if biometric enrollment fails
 * 5. Update user status to ACTIVE
 * 
 * Benefits of Use Case pattern:
 * - Business logic in one place
 * - Easy to test (just mock repositories)
 * - Transaction-like behavior (rollback on failure)
 * - Reusable across all platforms
 * 
 * Example usage:
 * ```
 * val useCase = EnrollUserUseCase(userRepo, biometricRepo)
 * val result = useCase(enrollmentData, faceImageBytes)
 * when {
 *     result.isSuccess -> showSuccess(result.getOrThrow())
 *     result.isFailure -> showError(result.exceptionOrNull())
 * }
 * ```
 */
class EnrollUserUseCase(
    private val userRepository: UserRepository,
    private val biometricRepository: BiometricRepository
) {
    /**
     * Execute enrollment
     * 
     * @param enrollmentData User's enrollment information
     * @param faceImage Captured face image (as byte array)
     * @return Result with enrolled user or error
     */
    suspend operator fun invoke(
        enrollmentData: EnrollmentData,
        faceImage: ByteArray
    ): Result<User> {
        // STEP 1: Validate all input data
        val validationError = validateEnrollmentData(enrollmentData)
        if (validationError != null) {
            return Result.failure(ValidationException(validationError))
        }
        
        // STEP 2: Validate face image
        if (faceImage.isEmpty()) {
            return Result.failure(ValidationException("Face image is required"))
        }
        
        if (faceImage.size > MAX_IMAGE_SIZE) {
            return Result.failure(
                ValidationException("Face image is too large (max ${MAX_IMAGE_SIZE / 1024 / 1024}MB)")
            )
        }
        
        // STEP 3: Create user (status: PENDING)
        val user = User(
            id = "", // Will be assigned by repository
            name = enrollmentData.fullName.trim(),
            email = enrollmentData.email.trim().lowercase(),
            idNumber = enrollmentData.idNumber.trim(),
            phoneNumber = enrollmentData.phoneNumber.trim(),
            status = UserStatus.PENDING,
            enrollmentDate = "", // Will be assigned by repository
            hasBiometric = false
        )
        
        val userResult = userRepository.createUser(user)
        if (userResult.isFailure) {
            return Result.failure(
                userResult.exceptionOrNull() ?: Exception("Failed to create user")
            )
        }
        
        val createdUser = userResult.getOrThrow()
        
        // STEP 4: Enroll biometric data
        val biometricResult = biometricRepository.enrollFace(createdUser.id, faceImage)
        
        if (biometricResult.isFailure) {
            // ROLLBACK: Delete user since biometric enrollment failed
            userRepository.deleteUser(createdUser.id)
            
            return Result.failure(
                biometricResult.exceptionOrNull() 
                    ?: Exception("Failed to enroll biometric data")
            )
        }
        
        // STEP 5: Update user status to ACTIVE
        val updatedUser = createdUser.copy(
            status = UserStatus.ACTIVE,
            hasBiometric = true
        )
        
        val updateResult = userRepository.updateUser(createdUser.id, updatedUser)
        
        if (updateResult.isFailure) {
            // ROLLBACK: Delete both user and biometric data
            biometricRepository.deleteBiometricData(createdUser.id)
            userRepository.deleteUser(createdUser.id)
            
            return Result.failure(
                updateResult.exceptionOrNull() 
                    ?: Exception("Failed to update user status")
            )
        }
        
        // SUCCESS: Return fully enrolled user
        return Result.success(updateResult.getOrThrow())
    }
    
    /**
     * Validate enrollment data
     * 
     * @return Error message if invalid, null if valid
     */
    private fun validateEnrollmentData(data: EnrollmentData): String? {
        // Validate full name
        val nameValidation = ValidationRules.validateFullName(data.fullName)
        if (nameValidation is ValidationResult.Error) {
            return nameValidation.message
        }
        
        // Validate email
        val emailValidation = ValidationRules.validateEmail(data.email)
        if (emailValidation is ValidationResult.Error) {
            return emailValidation.message
        }
        
        // Validate national ID
        val idValidation = ValidationRules.validateNationalId(data.idNumber)
        if (idValidation is ValidationResult.Error) {
            return idValidation.message
        }
        
        // Validate phone (if provided)
        if (data.phoneNumber.isNotBlank()) {
            val phoneValidation = ValidationRules.validatePhoneNumber(data.phoneNumber)
            if (phoneValidation is ValidationResult.Error) {
                return phoneValidation.message
            }
        }
        
        // Validate address (if provided)
        if (data.address.isNotBlank()) {
            val addressValidation = ValidationRules.validateAddress(data.address)
            if (addressValidation is ValidationResult.Error) {
                return addressValidation.message
            }
        }
        
        return null // All validations passed
    }
    
    companion object {
        private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 // 10 MB
    }
}
