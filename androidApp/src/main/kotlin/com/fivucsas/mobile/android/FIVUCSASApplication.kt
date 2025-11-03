package com.fivucsas.mobile.android

import android.app.Application
import com.fivucsas.shared.di.getAppModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * FIVUCSAS Android Application
 * Initializes Koin dependency injection for Android
 */
class FIVUCSASApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@FIVUCSASApplication)
            modules(getAppModules())
        }
    }
}
