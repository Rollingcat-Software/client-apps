package com.fivucsas.shared.platform.factory

import com.fivucsas.shared.platform.*
import com.fivucsas.shared.platform.config.DefaultConfigurationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * iOS Platform Service Factory
 *
 * Creates iOS-specific implementations of platform services.
 * The main DI is handled by Koin in PlatformModule.ios.kt;
 * this factory provides standalone creation for non-DI contexts.
 */
actual fun getCurrentPlatform(): PlatformType = PlatformType.IOS

actual fun createPlatformServiceFactory(): PlatformServiceFactory {
    return IosPlatformServiceFactory()
}

class IosPlatformServiceFactory : PlatformServiceFactory {
    override fun createCameraService(): ICameraService = StubCameraService()
    override fun createLogger(): ILogger = IosStubLogger()
    override fun createSecureStorage(): ISecureStorage = StubSecureStorage()
    override fun createNavigationService(): INavigationService = StubNavigationService()
    override fun createDialogService(): IDialogService = StubDialogService()
    override fun createNotificationService(): INotificationService = StubNotificationService()
    override fun createConfigurationProvider(): IConfigurationProvider = DefaultConfigurationProvider()
}

// --- Stub implementations (real ones injected via Koin) ---

private class StubCameraService : ICameraService {
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    override val cameraState: StateFlow<CameraState> = _cameraState
    override suspend fun initialize(lensFacing: LensFacing): Result<Unit> = Result.failure(UnsupportedOperationException("Use Koin DI"))
    override suspend fun startPreview(): Result<Unit> = Result.failure(UnsupportedOperationException("Use Koin DI"))
    override suspend fun stopPreview(): Result<Unit> = Result.success(Unit)
    override suspend fun captureImage(): Result<ByteArray> = Result.failure(UnsupportedOperationException("Use Koin DI"))
    override suspend fun captureFrame(): Result<ByteArray> = Result.failure(UnsupportedOperationException("Use Koin DI"))
    override fun isAvailable(): Boolean = false
    override fun hasCamera(lensFacing: LensFacing): Boolean = false
    override suspend fun release() {}
    override fun getPreviewDimensions(): Pair<Int, Int> = Pair(0, 0)
    override fun getSupportedResolutions(): List<Pair<Int, Int>> = emptyList()
}

private class IosStubLogger : ILogger {
    override fun debug(tag: String, message: String) {}
    override fun info(tag: String, message: String) {}
    override fun warn(tag: String, message: String) {}
    override fun error(tag: String, message: String, throwable: Throwable?) {}
    override fun verbose(tag: String, message: String) {}
}

private class StubSecureStorage : ISecureStorage {
    private val map = mutableMapOf<String, Any>()
    override fun saveString(key: String, value: String) { map[key] = value }
    override fun getString(key: String): String? = map[key] as? String
    override fun saveBoolean(key: String, value: Boolean) { map[key] = value }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = map[key] as? Boolean ?: defaultValue
    override fun saveInt(key: String, value: Int) { map[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = map[key] as? Int ?: defaultValue
    override fun saveLong(key: String, value: Long) { map[key] = value }
    override fun getLong(key: String, defaultValue: Long): Long = map[key] as? Long ?: defaultValue
    override fun remove(key: String) { map.remove(key) }
    override fun clear() { map.clear() }
    override fun contains(key: String): Boolean = map.containsKey(key)
}

private class StubNavigationService : INavigationService {
    override fun navigateTo(route: String, params: Map<String, Any>) {}
    override fun navigateBack(): Boolean = false
    override fun navigateAndClearStack(route: String) {}
    override fun popUpTo(route: String, inclusive: Boolean) {}
}

private class StubDialogService : IDialogService {
    override suspend fun showInfo(title: String, message: String, onDismiss: (() -> Unit)?) {}
    override suspend fun showConfirmation(title: String, message: String, confirmText: String, cancelText: String): Boolean = false
    override suspend fun showError(title: String, message: String, onDismiss: (() -> Unit)?) {}
    override suspend fun showLoading(message: String): DialogHandle = DialogHandle("stub")
    override fun dismiss(handle: DialogHandle) {}
    override fun dismissAll() {}
}

private class StubNotificationService : INotificationService {
    override fun showSuccess(message: String, duration: Long?) {}
    override fun showError(message: String, duration: Long?) {}
    override fun showWarning(message: String, duration: Long?) {}
    override fun showInfo(message: String, duration: Long?) {}
    override fun clearAll() {}
}
