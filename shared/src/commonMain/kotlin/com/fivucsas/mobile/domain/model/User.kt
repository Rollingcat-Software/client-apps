package com.fivucsas.mobile.domain.model

import kotlinx.datetime.Instant

data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isBiometricEnrolled: Boolean,
    val createdAt: Instant
) {
    val fullName: String
        get() = "$firstName $lastName"
}
