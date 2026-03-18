package com.fivucsas.shared.presentation.util

/**
 * Shared error mapping utility for all ViewModels.
 *
 * Translates technical exceptions (serialization errors, network timeouts,
 * HTTP status codes) into user-friendly messages. Prevents raw stack traces
 * and serialization errors from leaking to the UI.
 */
object ErrorMapper {

    /**
     * Map a throwable to a user-friendly message.
     * @param error The caught exception
     * @param context A short label for the operation (e.g. "load users", "change password")
     */
    fun mapToUserMessage(error: Throwable, context: String = ""): String {
        val message = error.message ?: return fallback(context)
        return when {
            // HTTP status codes
            "401" in message || "Unauthorized" in message ->
                "Session expired. Please log in again."
            "403" in message || "Forbidden" in message ->
                "You do not have permission for this action."
            "404" in message || "Not Found" in message ->
                "The requested resource was not found."
            "409" in message || "Conflict" in message || "already exists" in message.lowercase() ->
                "A conflict occurred. The item may already exist."
            "429" in message || "Rate Limit" in message || "Too many" in message ->
                "Too many requests. Please wait and try again."
            "400" in message || "Bad Request" in message ->
                "Invalid request. Please check your input."
            "500" in message || "Internal Server Error" in message ->
                "Server error. Please try again later."

            // Network errors
            "UnresolvedAddressException" in message || "ConnectException" in message
                || "Unable to resolve host" in message || "No address" in message ->
                "Cannot reach the server. Check your internet connection."
            "timeout" in message.lowercase() || "Timeout" in message ->
                "Connection timed out. Please try again."

            // Serialization errors (should not happen after DTO fixes)
            "Illegal input" in message || "serializ" in message.lowercase()
                || "JsonDecodingException" in message || "MissingFieldException" in message ->
                "Unexpected server response. Please update the app or try again later."

            // Generic fallback
            else -> fallback(context)
        }
    }

    private fun fallback(context: String): String {
        return if (context.isNotBlank()) {
            "Failed to $context. Please try again."
        } else {
            "Something went wrong. Please try again."
        }
    }
}
