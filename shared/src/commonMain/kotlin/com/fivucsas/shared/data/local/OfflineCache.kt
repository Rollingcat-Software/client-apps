package com.fivucsas.shared.data.local

import com.fivucsas.shared.platform.ISecureStorage
import com.fivucsas.shared.platform.StorageKeys

/**
 * Offline cache for storing last-known user data locally.
 *
 * Caches login user data and enrollment status so the app can
 * display a read-only profile when the device is offline.
 */
class OfflineCache(
    private val storage: ISecureStorage
) {
    companion object {
        private const val KEY_CACHED_USER_NAME = "offline_user_name"
        private const val KEY_CACHED_USER_EMAIL = "offline_user_email"
        private const val KEY_CACHED_USER_ID = "offline_user_id"
        private const val KEY_CACHED_USER_ROLE = "offline_user_role"
        private const val KEY_CACHED_USER_STATUS = "offline_user_status"
        private const val KEY_CACHED_USER_ID_NUMBER = "offline_user_id_number"
        private const val KEY_CACHED_USER_PHONE = "offline_user_phone"
        private const val KEY_CACHED_HAS_BIOMETRIC = "offline_has_biometric"
        private const val KEY_CACHED_ENROLLMENT_DATE = "offline_enrollment_date"
        private const val KEY_CACHED_FACE_ENROLLED = "offline_face_enrolled"
        private const val KEY_CACHED_VOICE_ENROLLED = "offline_voice_enrolled"
        private const val KEY_CACHED_FINGERPRINT_ENROLLED = "offline_fingerprint_enrolled"
        private const val KEY_CACHED_LAST_SYNC = "offline_last_sync"
    }

    /**
     * Cache user profile data after a successful API call.
     */
    fun cacheUserProfile(
        id: String,
        name: String,
        email: String,
        role: String,
        status: String,
        idNumber: String = "",
        phoneNumber: String = "",
        hasBiometric: Boolean = false,
        enrollmentDate: String = ""
    ) {
        storage.saveString(KEY_CACHED_USER_ID, id)
        storage.saveString(KEY_CACHED_USER_NAME, name)
        storage.saveString(KEY_CACHED_USER_EMAIL, email)
        storage.saveString(KEY_CACHED_USER_ROLE, role)
        storage.saveString(KEY_CACHED_USER_STATUS, status)
        storage.saveString(KEY_CACHED_USER_ID_NUMBER, idNumber)
        storage.saveString(KEY_CACHED_USER_PHONE, phoneNumber)
        storage.saveBoolean(KEY_CACHED_HAS_BIOMETRIC, hasBiometric)
        storage.saveString(KEY_CACHED_ENROLLMENT_DATE, enrollmentDate)
        storage.saveString(KEY_CACHED_LAST_SYNC, currentTimestamp())
    }

    /**
     * Cache biometric enrollment status.
     */
    fun cacheEnrollmentStatus(
        faceEnrolled: Boolean = false,
        voiceEnrolled: Boolean = false,
        fingerprintEnrolled: Boolean = false
    ) {
        storage.saveBoolean(KEY_CACHED_FACE_ENROLLED, faceEnrolled)
        storage.saveBoolean(KEY_CACHED_VOICE_ENROLLED, voiceEnrolled)
        storage.saveBoolean(KEY_CACHED_FINGERPRINT_ENROLLED, fingerprintEnrolled)
    }

    /**
     * Retrieve cached user profile, or null if nothing is cached.
     */
    fun getCachedProfile(): CachedUserProfile? {
        val id = storage.getString(KEY_CACHED_USER_ID) ?: return null
        return CachedUserProfile(
            id = id,
            name = storage.getString(KEY_CACHED_USER_NAME) ?: "",
            email = storage.getString(KEY_CACHED_USER_EMAIL) ?: "",
            role = storage.getString(KEY_CACHED_USER_ROLE) ?: "USER",
            status = storage.getString(KEY_CACHED_USER_STATUS) ?: "ACTIVE",
            idNumber = storage.getString(KEY_CACHED_USER_ID_NUMBER) ?: "",
            phoneNumber = storage.getString(KEY_CACHED_USER_PHONE) ?: "",
            hasBiometric = storage.getBoolean(KEY_CACHED_HAS_BIOMETRIC),
            enrollmentDate = storage.getString(KEY_CACHED_ENROLLMENT_DATE) ?: "",
            faceEnrolled = storage.getBoolean(KEY_CACHED_FACE_ENROLLED),
            voiceEnrolled = storage.getBoolean(KEY_CACHED_VOICE_ENROLLED),
            fingerprintEnrolled = storage.getBoolean(KEY_CACHED_FINGERPRINT_ENROLLED),
            lastSyncTimestamp = storage.getString(KEY_CACHED_LAST_SYNC) ?: ""
        )
    }

    /**
     * Cache login data from a successful login response.
     */
    fun cacheLoginData(
        userId: String,
        userName: String,
        userEmail: String,
        role: String
    ) {
        storage.saveString(KEY_CACHED_USER_ID, userId)
        storage.saveString(KEY_CACHED_USER_NAME, userName)
        storage.saveString(KEY_CACHED_USER_EMAIL, userEmail)
        storage.saveString(KEY_CACHED_USER_ROLE, role)
        storage.saveString(KEY_CACHED_LAST_SYNC, currentTimestamp())
    }

    /**
     * Clear all cached offline data (e.g. on logout).
     */
    fun clearCache() {
        listOf(
            KEY_CACHED_USER_ID, KEY_CACHED_USER_NAME, KEY_CACHED_USER_EMAIL,
            KEY_CACHED_USER_ROLE, KEY_CACHED_USER_STATUS, KEY_CACHED_USER_ID_NUMBER,
            KEY_CACHED_USER_PHONE, KEY_CACHED_HAS_BIOMETRIC, KEY_CACHED_ENROLLMENT_DATE,
            KEY_CACHED_FACE_ENROLLED, KEY_CACHED_VOICE_ENROLLED, KEY_CACHED_FINGERPRINT_ENROLLED,
            KEY_CACHED_LAST_SYNC
        ).forEach { storage.remove(it) }
    }

    fun hasCachedData(): Boolean {
        return storage.contains(KEY_CACHED_USER_ID)
    }

    private fun currentTimestamp(): String {
        // Simple epoch-based timestamp as a string
        return kotlinx.datetime.Clock.System.now().toString()
    }
}

/**
 * Cached user profile data for offline display.
 */
data class CachedUserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val status: String,
    val idNumber: String = "",
    val phoneNumber: String = "",
    val hasBiometric: Boolean = false,
    val enrollmentDate: String = "",
    val faceEnrolled: Boolean = false,
    val voiceEnrolled: Boolean = false,
    val fingerprintEnrolled: Boolean = false,
    val lastSyncTimestamp: String = ""
)
