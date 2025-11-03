package com.fivucsas.shared.data.repository

import com.fivucsas.shared.domain.model.BiometricData
import com.fivucsas.shared.domain.model.FacialAction
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.VerificationResult
import com.fivucsas.shared.domain.repository.BiometricRepository
import kotlinx.coroutines.delay

/**
 * Mock implementation of BiometricRepository
 * 
 * Simulates biometric operations for development.
 * TODO: Replace with real DeepFace/API integration (Week 2)
 * 
 * This stub allows:
 * - Development without backend
 * - Testing UI flows
 * - Easy to swap mock → real implementation
 */
class BiometricRepositoryImpl : BiometricRepository {
    
    // In-memory storage for enrolled biometric data
    private val biometricData = mutableMapOf<String, BiometricData>()
    
    override suspend fun enrollFace(userId: String, imageData: ByteArray): Result<BiometricData> {
        return try {
            // Simulate processing time
            delay(1000)
            
            // Generate mock face embedding (in real: DeepFace would generate this)
            val embedding = FloatArray(128) { (it * 0.01f) } // Mock 128-dimensional embedding
            
            // Create biometric data
            val data = BiometricData(
                id = "bio_${userId}_${generateId()}",
                userId = userId,
                faceEmbedding = embedding,
                enrollmentDate = getCurrentDate(),
                lastVerificationDate = null,
                verificationCount = 0
            )
            
            // Store in memory
            biometricData[userId] = data
            
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun verifyFace(imageData: ByteArray): Result<VerificationResult> {
        return try {
            // Simulate processing time
            delay(1500)
            
            // Mock verification (always succeeds for demo)
            // TODO: Real verification with DeepFace similarity comparison
            val result = VerificationResult(
                isVerified = true,
                userId = "1", // Mock: would be matched user ID
                confidence = 0.95f,
                message = "Face verified successfully (mock)"
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun checkLiveness(actions: List<FacialAction>): Result<LivenessResult> {
        return try {
            // Simulate liveness detection processing
            delay(800)
            
            // Mock: always passes for demo
            // TODO: Real liveness detection with facial action verification
            val result = LivenessResult(
                isLive = true,
                confidence = 0.92f,
                message = "Liveness check passed (mock): ${actions.size} actions verified"
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getBiometricData(userId: String): Result<BiometricData> {
        return try {
            delay(300)
            
            val data = biometricData[userId]
            if (data != null) {
                Result.success(data)
            } else {
                Result.failure(NoSuchElementException("No biometric data found for user $userId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBiometricData(userId: String): Result<Unit> {
        return try {
            delay(300)
            
            val removed = biometricData.remove(userId)
            if (removed != null) {
                Result.success(Unit)
            } else {
                Result.failure(NoSuchElementException("No biometric data found for user $userId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current date
     * TODO: Use kotlinx-datetime
     */
    private fun getCurrentDate(): String {
        return "2025-11-03"
    }
    
    /**
     * Generate unique ID
     * TODO: Use proper UUID when kotlinx-uuid is added
     */
    private fun generateId(): String {
        return (++idCounter).toString()
    }
    
    companion object {
        private var idCounter = 0
    }
}
