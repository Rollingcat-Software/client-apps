package com.fivucsas.shared.platform

import java.awt.Desktop

/**
 * Desktop actual implementations for the FIVUCSAS auth widget.
 *
 * On Desktop, JavaFX WebView is not included in the dependency tree.
 * Instead, we open the system browser pointed at the verify URL.
 * The widget in the browser completes auth and the result is communicated
 * back to the app via a local HTTP callback server or deep link.
 *
 * For the initial integration, the desktop implementation opens the
 * browser and immediately reports success via the callback. A future
 * enhancement will add a localhost callback server (e.g. on port 18923)
 * that the widget redirects to with the auth result, similar to the
 * OAuth2 desktop flow pattern.
 *
 * If JavaFX becomes available (e.g. via org.openjfx:javafx-web dependency),
 * this can be upgraded to an in-process WebView with full postMessage bridge.
 */

actual fun isAuthWidgetAvailable(): Boolean = Desktop.isDesktopSupported()

actual fun launchAuthWidget(config: AuthWidgetConfig, callback: AuthWidgetCallback) {
    try {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            callback.onError("System browser is not available on this desktop environment.")
            return
        }
        val uri = java.net.URI(config.buildUrl())
        Desktop.getDesktop().browse(uri)
        // Browser-based flow: the widget will redirect to a callback URL when done.
        // For now, we inform the caller that the browser was opened successfully.
        // The app should poll the auth session status or use a local callback server.
        callback.onComplete(
            AuthWidgetResult(
                success = true,
                sessionId = "",
                userId = config.userId,
                completedMethods = emptyList(),
                token = null
            )
        )
    } catch (e: Exception) {
        callback.onError("Failed to open auth widget in browser: ${e.message}")
    }
}
