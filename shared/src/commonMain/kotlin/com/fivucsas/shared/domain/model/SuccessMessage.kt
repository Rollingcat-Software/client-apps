package com.fivucsas.shared.domain.model

/**
 * Success messages for user feedback
 */
sealed class SuccessMessage(val message: String) {
    data object UserEnrolled : SuccessMessage("User enrolled successfully! ✓")
    data object UserVerified : SuccessMessage("Verification successful! ✓")
    data object UserUpdated : SuccessMessage("User updated successfully")
    data object UserDeleted : SuccessMessage("User deleted successfully")
    data object LivenessCheckPassed : SuccessMessage("Liveness check passed ✓")
    
    data class Custom(val customMessage: String) : SuccessMessage(customMessage)
}
