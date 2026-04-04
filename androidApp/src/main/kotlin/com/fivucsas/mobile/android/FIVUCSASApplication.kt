package com.fivucsas.mobile.android

import android.app.Application
import android.util.Log
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
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FIVUCSAS Android Application
 * Initializes Koin dependency injection for Android
 */
class FIVUCSASApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Install global crash handler to log uncaught exceptions to a file
        setupCrashHandler()

        // Android-app-specific module: overrides shared no-op services with real implementations
        val androidAppModule = module {
            single<INfcService> { AndroidNfcService(androidContext()) }
            single<IPushNotificationService> { AndroidPushNotificationService(get()) }
            single<INetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
        }

        // Initialize Koin
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@FIVUCSASApplication)
            modules(getAppModules() + androidAppModule)
        }
    }

    /**
     * Install a global uncaught exception handler that writes crash logs
     * to the app's internal files directory before delegating to the default handler.
     */
    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
                val crashDir = File(filesDir, "crash_logs")
                crashDir.mkdirs()

                val logFile = File(crashDir, "crash_$timestamp.log")
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                pw.println("FIVUCSAS Crash Report")
                pw.println("Time: $timestamp")
                pw.println("Thread: ${thread.name}")
                pw.println("Exception: ${throwable.javaClass.name}")
                pw.println("Message: ${throwable.message}")
                pw.println()
                throwable.printStackTrace(pw)
                pw.flush()

                logFile.writeText(sw.toString())
                Log.e("FIVUCSAS", "Crash logged to ${logFile.absolutePath}", throwable)

                // Prune old crash logs (keep most recent 10)
                crashDir.listFiles()
                    ?.sortedByDescending { it.lastModified() }
                    ?.drop(10)
                    ?.forEach { it.delete() }
            } catch (e: Exception) {
                Log.e("FIVUCSAS", "Failed to write crash log", e)
            }

            // Delegate to default handler (shows ANR dialog or kills process)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
