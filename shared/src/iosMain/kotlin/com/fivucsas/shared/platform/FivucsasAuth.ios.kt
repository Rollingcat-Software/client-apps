package com.fivucsas.shared.platform

/**
 * iOS actual implementations for the FIVUCSAS auth widget.
 *
 * Placeholder — full WKWebView integration is planned for a future release.
 * The architecture is identical: load verifyUrl in WKWebView, inject a
 * WKScriptMessageHandler to receive postMessage events, and relay them
 * to the AuthWidgetCallback.
 *
 * For now, isAuthWidgetAvailable() returns false so callers can gracefully
 * fall back to the native auth screens.
 */

actual fun isAuthWidgetAvailable(): Boolean = false

actual fun launchAuthWidget(config: AuthWidgetConfig, callback: AuthWidgetCallback) {
    callback.onError("Auth widget is not yet available on iOS. Use native auth screens.")
}
