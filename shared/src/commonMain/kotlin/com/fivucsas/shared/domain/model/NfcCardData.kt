package com.fivucsas.shared.domain.model

/**
 * Platform-independent NFC card data models for the shared module.
 *
 * These models represent the results of NFC card reading operations
 * in a multiplatform-compatible way (no Android-specific types like Bitmap).
 */

/**
 * Sealed class representing all possible NFC card read results.
 */
sealed class NfcReadResult {
    /** Card was read successfully. */
    data class Success(val cardData: NfcCardData) : NfcReadResult()

    /** Card requires MRZ authentication data. */
    data class AuthenticationRequired(
        val cardTypeName: String,
        val message: String = "This card requires MRZ authentication data."
    ) : NfcReadResult()

    /** Card read failed with an error. */
    data class Failure(
        val errorMessage: String,
        val isRecoverable: Boolean = true
    ) : NfcReadResult()

    /** NFC is not available on this device. */
    data object NfcNotAvailable : NfcReadResult()

    /** NFC is disabled in settings. */
    data object NfcDisabled : NfcReadResult()
}

/**
 * Platform-independent NFC card data.
 * Photo is represented as ByteArray (JPEG) instead of Android Bitmap.
 */
sealed class NfcCardData {
    abstract val uid: String
    abstract val cardTypeName: String
    abstract val technologies: List<String>
    abstract val readTimestamp: Long
}

/**
 * Identity document data (passport or Turkish eID).
 */
data class NfcIdentityDocumentData(
    override val uid: String,
    override val cardTypeName: String,
    override val technologies: List<String>,
    override val readTimestamp: Long,

    // Personal data from MRZ
    val documentType: String = "",
    val issuingCountry: String = "",
    val documentNumber: String = "",
    val surname: String = "",
    val givenNames: String = "",
    val nationality: String = "",
    val dateOfBirth: String = "",
    val sex: String = "",
    val dateOfExpiry: String = "",
    val personalNumber: String = "",

    // Photo as JPEG bytes (platform-independent)
    val photoBytes: ByteArray? = null,

    // Security validation
    val bacSuccessful: Boolean = false,
    val sodValid: Boolean? = null,
    val dg1HashValid: Boolean? = null,
    val dg2HashValid: Boolean? = null
) : NfcCardData() {
    val fullName: String
        get() = if (givenNames.isNotEmpty()) "$givenNames $surname" else surname

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NfcIdentityDocumentData) return false
        return uid == other.uid && documentNumber == other.documentNumber
    }

    override fun hashCode(): Int = uid.hashCode()
}

/**
 * Generic NFC card data (NDEF, DESFire, Mifare, etc.).
 */
data class NfcGenericCardData(
    override val uid: String,
    override val cardTypeName: String,
    override val technologies: List<String>,
    override val readTimestamp: Long,
    val details: Map<String, String> = emptyMap()
) : NfcCardData()

/**
 * MRZ input data for BAC authentication.
 */
data class MrzInputData(
    val documentNumber: String,
    val dateOfBirth: String,   // YYMMDD
    val dateOfExpiry: String   // YYMMDD
) {
    fun isValid(): Boolean {
        return documentNumber.isNotBlank() &&
                dateOfBirth.length == 6 && dateOfBirth.all { it.isDigit() } &&
                dateOfExpiry.length == 6 && dateOfExpiry.all { it.isDigit() }
    }
}
