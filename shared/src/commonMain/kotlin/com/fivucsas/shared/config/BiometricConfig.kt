package com.fivucsas.shared.config

/**
 * Biometric Configuration Constants
 *
 * Centralizes all biometric verification thresholds, timeouts,
 * and quality requirements for face recognition system.
 *
 * These values are critical for security and should be tuned
 * based on testing and security requirements.
 */
object BiometricConfig {

    // ============================================
    // Verification Thresholds
    // ============================================

    /**
     * Minimum confidence score for successful face match
     * Range: 0.0 to 1.0 (0% to 100%)
     * Higher value = more strict matching
     */
    const val CONFIDENCE_THRESHOLD = 0.85

    /**
     * Minimum liveness detection score
     * Range: 0.0 to 1.0 (0% to 100%)
     * Detects if the face is from a live person vs photo/video
     */
    const val LIVENESS_THRESHOLD = 0.80

    /**
     * Minimum image quality score
     * Range: 0.0 to 1.0 (0% to 100%)
     * Ensures captured image is suitable for verification
     */
    const val QUALITY_THRESHOLD = 0.75

    // ============================================
    // Retry Limits
    // ============================================

    const val MAX_ENROLLMENT_RETRIES = 3
    const val MAX_VERIFICATION_RETRIES = 3
    const val MAX_CAMERA_INIT_RETRIES = 3
    const val MAX_LIVENESS_CHECK_RETRIES = 2

    // ============================================
    // Timeouts (seconds)
    // ============================================

    const val CAMERA_INIT_TIMEOUT = 10L
    const val CAPTURE_TIMEOUT = 5L
    const val LIVENESS_CHECK_TIMEOUT = 10L
    const val PROCESSING_TIMEOUT = 30L
    const val VERIFICATION_TIMEOUT = 15L

    // ============================================
    // Image Requirements
    // ============================================

    /**
     * Minimum face size in pixels for reliable detection
     */
    const val MIN_FACE_SIZE_PIXELS = 100

    /**
     * Maximum face size in pixels (prevents too-close faces)
     */
    const val MAX_FACE_SIZE_PIXELS = 500

    /**
     * Preferred image dimensions for processing
     */
    const val PREFERRED_IMAGE_WIDTH = 640
    const val PREFERRED_IMAGE_HEIGHT = 480

    /**
     * Maximum image file size in KB
     */
    const val MAX_IMAGE_SIZE_KB = 500

    // ============================================
    // Quality Checks
    // ============================================

    /**
     * Minimum brightness level (0.0 = black, 1.0 = white)
     * Too dark images won't process well
     */
    const val MIN_BRIGHTNESS = 0.3

    /**
     * Maximum brightness level
     * Too bright images cause glare/washout
     */
    const val MAX_BRIGHTNESS = 0.9

    /**
     * Minimum sharpness score (0.0 to 1.0)
     * Detects blurry images
     */
    const val MIN_SHARPNESS = 0.5

    /**
     * Maximum blur score (0.0 to 1.0)
     * Lower is better (less blur)
     */
    const val MAX_BLUR_SCORE = 0.3

    // ============================================
    // Enrollment Configuration
    // ============================================

    /**
     * Number of face samples required during enrollment
     * More samples = better accuracy but slower enrollment
     */
    const val ENROLLMENT_SAMPLES_REQUIRED = 3

    /**
     * Minimum diversity between enrollment samples
     * Ensures samples capture different angles/expressions
     */
    const val ENROLLMENT_DIVERSITY_THRESHOLD = 0.2

    // ============================================
    // Camera Configuration
    // ============================================

    const val CAMERA_FRAME_RATE_FPS = 30
    const val CAMERA_AUTO_FOCUS_ENABLED = true
    const val CAMERA_FLASH_ENABLED = false

    // ============================================
    // Display Configuration
    // ============================================

    /**
     * Show confidence score to user during verification
     */
    const val SHOW_CONFIDENCE_SCORE = true

    /**
     * Show liveness detection progress
     */
    const val SHOW_LIVENESS_PROGRESS = true

    /**
     * Enable debugging overlays (face bounding boxes, etc.)
     * Should be false in production
     */
    const val ENABLE_DEBUG_OVERLAY = false
}
