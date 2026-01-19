package com.fivucsas.shared.platform

/**
 * Dialog Service Interface (UI Port)
 *
 * Platform abstraction for dialog operations following Hexagonal Architecture.
 * Allows domain layer to show dialogs without depending on UI framework.
 *
 * Design Principles Applied:
 * - Dependency Inversion Principle (DIP): Domain layer depends on abstraction
 * - Interface Segregation Principle (ISP): Focused interface for dialogs
 * - Single Responsibility Principle (SRP): Only handles dialog operations
 *
 * Hexagonal Architecture Role: PORT (Primary/Driving Port)
 */
interface IDialogService {
    /**
     * Show an informational dialog
     * @param title Dialog title
     * @param message Dialog message
     * @param onDismiss Callback when dialog is dismissed
     */
    suspend fun showInfo(
        title: String,
        message: String,
        onDismiss: (() -> Unit)? = null
    )

    /**
     * Show a confirmation dialog
     * @param title Dialog title
     * @param message Dialog message
     * @param confirmText Confirm button text
     * @param cancelText Cancel button text
     * @return true if confirmed, false if cancelled
     */
    suspend fun showConfirmation(
        title: String,
        message: String,
        confirmText: String = "Confirm",
        cancelText: String = "Cancel"
    ): Boolean

    /**
     * Show an error dialog
     * @param title Dialog title
     * @param message Error message
     * @param onDismiss Callback when dialog is dismissed
     */
    suspend fun showError(
        title: String,
        message: String,
        onDismiss: (() -> Unit)? = null
    )

    /**
     * Show a loading dialog
     * @param message Loading message
     * @return Dialog handle to dismiss later
     */
    suspend fun showLoading(message: String = "Loading..."): DialogHandle

    /**
     * Dismiss a dialog by handle
     * @param handle Dialog handle returned from show methods
     */
    fun dismiss(handle: DialogHandle)

    /**
     * Dismiss all dialogs
     */
    fun dismissAll()
}

/**
 * Handle for dialog dismissal
 */
data class DialogHandle(val id: String)
