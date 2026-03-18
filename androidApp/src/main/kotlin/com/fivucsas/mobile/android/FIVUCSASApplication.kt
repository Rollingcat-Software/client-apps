package com.fivucsas.mobile.android

import android.app.Application
import com.fivucsas.mobile.android.data.AndroidNetworkMonitor
import com.fivucsas.mobile.android.data.nfc.AndroidNfcService
import com.fivucsas.mobile.android.data.push.AndroidPushNotificationService
import com.fivucsas.shared.di.getAppModules
import com.fivucsas.shared.platform.INetworkMonitor
import com.fivucsas.shared.platform.INfcService
import com.fivucsas.shared.platform.IPushNotificationService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

/**
 * FIVUCSAS Android Application
 * Initializes Koin dependency injection for Android
 */
class FIVUCSASApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Android-app-specific module: overrides shared no-op services with real implementations
        val androidAppModule = module {
            single<INfcService> { AndroidNfcService(androidContext()) }
            single<IPushNotificationService> { AndroidPushNotificationService() }
            single<INetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
        }

        // Initialize Koin
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@FIVUCSASApplication)
            modules(getAppModules() + androidAppModule)
        }
    }
}
