package com.fivucsas.shared.domain.model

/**
 * Biometric data model
 * 
 * Contains face recognition data for a user
 */
data class BiometricData(
    val id: String,
    val userId: String,
    val faceEmbedding: FloatArray,
    val enrollmentDate: String,
    val lastVerificationDate: String? = null,
    val verificationCount: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BiometricData

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (!faceEmbedding.contentEquals(other.faceEmbedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + faceEmbedding.contentHashCode()
        return result
    }
}

/**
 * Verification result
 */
data class VerificationResult(
    val isVerified: Boolean,
    val userId: String?,
    val confidence: Float,
    val message: String
)

/**
 * Liveness check result
 */
data class LivenessResult(
    val isLive: Boolean,
    val confidence: Float,
    val message: String
)

/**
 * Facial action for liveness detection
 */
enum class FacialAction {
    SMILE,
    BLINK,
    LOOK_LEFT,
    LOOK_RIGHT,
    LOOK_UP,
    LOOK_DOWN,
    OPEN_MOUTH
}
