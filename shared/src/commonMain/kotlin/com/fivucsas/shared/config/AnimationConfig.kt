package com.fivucsas.shared.config

/**
 * Animation Configuration Constants
 *
 * Centralizes all animation durations and delays to ensure
 * consistent animation behavior across the application.
 *
 * Based on Material Design motion guidelines.
 */
object AnimationConfig {

    // ============================================
    // Animation Durations (milliseconds)
    // ============================================

    const val DURATION_INSTANT = 0
    const val DURATION_FAST = 150
    const val DURATION_NORMAL = 300
    const val DURATION_SLOW = 500
    const val DURATION_VERY_SLOW = 1000

    // ============================================
    // Delays (milliseconds)
    // ============================================

    const val DELAY_SHORT = 100L
    const val DELAY_MEDIUM = 500L
    const val DELAY_LONG = 1000L
    const val DELAY_VERIFICATION = 2000L
    const val DELAY_SUCCESS_MESSAGE = 3000L
    const val DELAY_SCREEN_TRANSITION = 3000L
    const val DELAY_AUTO_DISMISS_LONG = 5000L

    // API Simulation Delays
    const val DELAY_API_SIMULATION_SHORT = 500L
    const val DELAY_API_SIMULATION = 800L

    // ============================================
    // Fade Animations
    // ============================================

    const val FADE_IN_ALPHA_START = 0f
    const val FADE_IN_ALPHA_END = 1f
    const val FADE_OUT_ALPHA_START = 1f
    const val FADE_OUT_ALPHA_END = 0f

    // ============================================
    // Specific Use Cases
    // ============================================

    const val LOADING_INDICATOR_DURATION = 300
    const val DIALOG_ANIMATION_DURATION = 200
    const val SCREEN_TRANSITION_DURATION = 300
    const val SNACKBAR_DISPLAY_DURATION = 3000L
    const val TOAST_DISPLAY_DURATION = 2000L
}
