package com.fivucsas.mobile.domain.model

data class BiometricResult(
    val verified: Boolean,
    val confidence: Double,
    val message: String
)
