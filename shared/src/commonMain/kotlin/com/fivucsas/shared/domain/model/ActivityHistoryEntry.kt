package com.fivucsas.shared.domain.model

data class ActivityHistoryEntry(
    val category: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val score: String,
    val isSuccess: Boolean
)
