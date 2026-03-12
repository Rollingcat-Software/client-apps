package com.fivucsas.shared.platform.factory

import com.fivucsas.shared.platform.*
import com.fivucsas.shared.platform.config.DefaultConfigurationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.prefs.Preferences

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
        return DesktopCameraService()
    }

    override fun createLogger(): ILogger {
        return DesktopLogger()
    }

    override fun createSecureStorage(): ISecureStorage {
        return DesktopSecureStorage()
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

/**
 * Desktop Camera Service Implementation
 *
 * Desktop has no built-in camera API, so this returns appropriate errors.
 * Applications should check isAvailable() before attempting camera operations.
 */
class DesktopCameraService : ICameraService {
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Uninitialized)
    override val cameraState: StateFlow<CameraState> = _cameraState

    override suspend fun initialize(lensFacing: LensFacing): Result<Unit> {
        _cameraState.value = CameraState.Error("Camera not available on desktop")
        return Result.failure(UnsupportedOperationException("Camera not available on desktop platform"))
    }

    override suspend fun startPreview(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Camera not available on desktop platform"))

    override suspend fun stopPreview(): Result<Unit> = Result.success(Unit)

    override suspend fun captureImage(): Result<ByteArray> =
        Result.failure(UnsupportedOperationException("Camera not available on desktop platform"))

    override suspend fun captureFrame(): Result<ByteArray> =
        Result.failure(UnsupportedOperationException("Camera not available on desktop platform"))

    override fun isAvailable(): Boolean = false
    override fun hasCamera(lensFacing: LensFacing): Boolean = false
    override suspend fun release() { _cameraState.value = CameraState.Uninitialized }
    override fun getPreviewDimensions(): Pair<Int, Int> = Pair(0, 0)
    override fun getSupportedResolutions(): List<Pair<Int, Int>> = emptyList()
}

/**
 * Desktop Logger Implementation
 *
 * Uses standard output for logging on desktop platform.
 */
class DesktopLogger : ILogger {
    override fun debug(tag: String, message: String) {
        println("D/$tag: $message")
    }

    override fun info(tag: String, message: String) {
        println("I/$tag: $message")
    }

    override fun warn(tag: String, message: String) {
        System.err.println("W/$tag: $message")
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        System.err.println("E/$tag: $message")
        throwable?.printStackTrace(System.err)
    }

    override fun verbose(tag: String, message: String) {
        println("V/$tag: $message")
    }
}

/**
 * Desktop Secure Storage Implementation
 *
 * Uses Java Preferences API for persistent storage on desktop.
 */
class DesktopSecureStorage : ISecureStorage {
    private val prefs = Preferences.userNodeForPackage(DesktopSecureStorage::class.java)

    override fun saveString(key: String, value: String) { prefs.put(key, value) }
    override fun getString(key: String): String? = prefs.get(key, null)
    override fun saveBoolean(key: String, value: Boolean) { prefs.putBoolean(key, value) }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = prefs.getBoolean(key, defaultValue)
    override fun saveInt(key: String, value: Int) { prefs.putInt(key, value) }
    override fun getInt(key: String, defaultValue: Int): Int = prefs.getInt(key, defaultValue)
    override fun saveLong(key: String, value: Long) { prefs.putLong(key, value) }
    override fun getLong(key: String, defaultValue: Long): Long = prefs.getLong(key, defaultValue)
    override fun remove(key: String) { prefs.remove(key) }
    override fun clear() { prefs.clear() }
    override fun contains(key: String): Boolean = prefs.get(key, null) != null
}
