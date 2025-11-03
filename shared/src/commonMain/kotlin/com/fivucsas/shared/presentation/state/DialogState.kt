package com.fivucsas.shared.presentation.state

/**
 * Dialog state for confirmations and alerts
 */
sealed class DialogState {
    data object None : DialogState()
    
    data class Confirmation(
        val title: String,
        val message: String,
        val confirmText: String = "Confirm",
        val cancelText: String = "Cancel",
        val onConfirm: () -> Unit,
        val onCancel: () -> Unit = {}
    ) : DialogState()
    
    data class Alert(
        val title: String,
        val message: String,
        val buttonText: String = "OK",
        val onDismiss: () -> Unit = {}
    ) : DialogState()
    
    data class Error(
        val title: String = "Error",
        val message: String,
        val canRetry: Boolean = false,
        val onRetry: (() -> Unit)? = null,
        val onDismiss: () -> Unit = {}
    ) : DialogState()
}
