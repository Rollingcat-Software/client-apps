package com.fivucsas.shared.platform.factory

import com.fivucsas.shared.platform.*
import com.fivucsas.shared.platform.config.DefaultConfigurationProvider

/**
 * Desktop Platform Service Factory
 *
 * Creates desktop-specific implementations of platform services.
 * Implements Factory pattern for Hexagonal Architecture adapters.
 *
 * Design Principles Applied:
 * - Factory Pattern: Creates platform-specific implementations
 * - Dependency Inversion Principle: Returns interface types
 * - Single Responsibility Principle: Only creates desktop services
 */
actual fun getCurrentPlatform(): PlatformType = PlatformType.DESKTOP

actual fun createPlatformServiceFactory(): PlatformServiceFactory {
    return DesktopPlatformServiceFactory()
}

class DesktopPlatformServiceFactory : PlatformServiceFactory {
    override fun createCameraService(): ICameraService {
        // Will be provided by Koin DI - DesktopCameraServiceImpl
        TODO("Camera service should be injected via DI")
    }

    override fun createLogger(): ILogger {
        // Will be provided by Koin DI - DesktopLoggerImpl
        TODO("Logger should be injected via DI")
    }

    override fun createSecureStorage(): ISecureStorage {
        // Will be provided by Koin DI - DesktopSecureStorageImpl
        TODO("Secure storage should be injected via DI")
    }

    override fun createNavigationService(): INavigationService {
        return DesktopNavigationService()
    }

    override fun createDialogService(): IDialogService {
        return DesktopDialogService()
    }

    override fun createNotificationService(): INotificationService {
        return DesktopNotificationService()
    }

    override fun createConfigurationProvider(): IConfigurationProvider {
        return DefaultConfigurationProvider()
    }
}

/**
 * Desktop Navigation Service Implementation (Adapter)
 */
class DesktopNavigationService : INavigationService {
    private var currentRoute: String = NavigationRoutes.LOGIN
    private val navigationStack = mutableListOf<String>()
    private val listeners = mutableListOf<(String) -> Unit>()

    override fun navigateTo(route: String, params: Map<String, Any>) {
        navigationStack.add(currentRoute)
        currentRoute = route
        notifyListeners(route)
    }

    override fun navigateBack(): Boolean {
        return if (navigationStack.isNotEmpty()) {
            currentRoute = navigationStack.removeLast()
            notifyListeners(currentRoute)
            true
        } else {
            false
        }
    }

    override fun navigateAndClearStack(route: String) {
        navigationStack.clear()
        currentRoute = route
        notifyListeners(route)
    }

    override fun popUpTo(route: String, inclusive: Boolean) {
        while (navigationStack.isNotEmpty() && currentRoute != route) {
            currentRoute = navigationStack.removeLast()
        }
        if (inclusive && navigationStack.isNotEmpty()) {
            currentRoute = navigationStack.removeLast()
        }
        notifyListeners(currentRoute)
    }

    fun addNavigationListener(listener: (String) -> Unit) {
        listeners.add(listener)
    }

    fun removeNavigationListener(listener: (String) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners(route: String) {
        listeners.forEach { it(route) }
    }
}

/**
 * Desktop Dialog Service Implementation (Adapter)
 */
class DesktopDialogService : IDialogService {
    private val dialogs = mutableMapOf<String, DialogState>()
    private var dialogCounter = 0
    private val listeners = mutableListOf<(Map<String, DialogState>) -> Unit>()

    override suspend fun showInfo(title: String, message: String, onDismiss: (() -> Unit)?) {
        val handle = createDialog(DialogState(title, message, DialogType.INFO, onDismiss))
        // In real implementation, would wait for user interaction
    }

    override suspend fun showConfirmation(
        title: String,
        message: String,
        confirmText: String,
        cancelText: String
    ): Boolean {
        val handle = createDialog(
            DialogState(
                title,
                message,
                DialogType.CONFIRMATION,
                confirmText = confirmText,
                cancelText = cancelText
            )
        )
        // In real implementation, would wait for user response
        return true // Mock response
    }

    override suspend fun showError(title: String, message: String, onDismiss: (() -> Unit)?) {
        val handle = createDialog(DialogState(title, message, DialogType.ERROR, onDismiss))
    }

    override suspend fun showLoading(message: String): DialogHandle {
        return createDialog(DialogState("", message, DialogType.LOADING))
    }

    override fun dismiss(handle: DialogHandle) {
        val dialogState = dialogs.remove(handle.id)
        dialogState?.onDismiss?.invoke()
        notifyListeners()
    }

    override fun dismissAll() {
        dialogs.values.forEach { it.onDismiss?.invoke() }
        dialogs.clear()
        notifyListeners()
    }

    fun addDialogListener(listener: (Map<String, DialogState>) -> Unit) {
        listeners.add(listener)
        listener(dialogs)
    }

    fun removeDialogListener(listener: (Map<String, DialogState>) -> Unit) {
        listeners.remove(listener)
    }

    private fun createDialog(state: DialogState): DialogHandle {
        val handle = DialogHandle("dialog_${dialogCounter++}")
        dialogs[handle.id] = state
        notifyListeners()
        return handle
    }

    private fun notifyListeners() {
        listeners.forEach { it(dialogs) }
    }

    data class DialogState(
        val title: String,
        val message: String,
        val type: DialogType,
        val onDismiss: (() -> Unit)? = null,
        val confirmText: String = "OK",
        val cancelText: String = "Cancel"
    )

    enum class DialogType {
        INFO, ERROR, CONFIRMATION, LOADING
    }
}

/**
 * Desktop Notification Service Implementation (Adapter)
 */
class DesktopNotificationService : INotificationService {
    private val notifications = mutableListOf<NotificationState>()
    private val listeners = mutableListOf<(List<NotificationState>) -> Unit>()

    override fun showSuccess(message: String, duration: Long?) {
        addNotification(NotificationState(message, NotificationType.SUCCESS, duration))
    }

    override fun showError(message: String, duration: Long?) {
        addNotification(NotificationState(message, NotificationType.ERROR, duration))
    }

    override fun showWarning(message: String, duration: Long?) {
        addNotification(NotificationState(message, NotificationType.WARNING, duration))
    }

    override fun showInfo(message: String, duration: Long?) {
        addNotification(NotificationState(message, NotificationType.INFO, duration))
    }

    override fun clearAll() {
        notifications.clear()
        notifyListeners()
    }

    fun addNotificationListener(listener: (List<NotificationState>) -> Unit) {
        listeners.add(listener)
        listener(notifications)
    }

    fun removeNotificationListener(listener: (List<NotificationState>) -> Unit) {
        listeners.remove(listener)
    }

    private fun addNotification(notification: NotificationState) {
        notifications.add(notification)
        notifyListeners()

        // Auto-remove after duration
        val duration = notification.duration ?: 3000L
        // In real implementation, would use coroutine delay
    }

    private fun notifyListeners() {
        listeners.forEach { it(notifications) }
    }

    data class NotificationState(
        val message: String,
        val type: NotificationType,
        val duration: Long?
    )
}
