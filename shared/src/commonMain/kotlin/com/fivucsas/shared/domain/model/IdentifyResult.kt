package com.fivucsas.shared.domain.model

data class IdentifyResult(
    val userId: String,
    val name: String,
    val confidence: Float,
    val isMatch: Boolean
)
