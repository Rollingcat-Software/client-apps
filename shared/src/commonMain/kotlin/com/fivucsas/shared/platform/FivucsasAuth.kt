package com.fivucsas.shared.platform

import com.fivucsas.shared.config.AppConfig
import kotlinx.serialization.Serializable

/**
 * FIVUCSAS Embeddable Auth Widget — Common API
 *
 * Platform-agnostic interface for launching the FIVUCSAS auth widget
 * inside a WebView. Each platform (Android, Desktop, iOS) provides an
 * `actual` implementation that opens a WebView, loads the verify URL,
 * and listens for postMessage callbacks from the widget.
 *
 * Communication protocol:
 *   Widget → Native:  window.parent.postMessage({ type: "fivucsas:complete", ... }, "*")
 *   Widget → Native:  window.parent.postMessage({ type: "fivucsas:error", ... }, "*")
 *   Widget → Native:  window.parent.postMessage({ type: "fivucsas:cancel" }, "*")
 *
 * Design Principles:
 * - Dependency Inversion: common code depends on abstractions
 * - Open/Closed: new platforms add `actual` without touching common
 * - Single Responsibility: only handles auth widget lifecycle
 */

/**
 * Configuration for the auth widget.
 */
@Serializable
data class AuthWidgetConfig(
    val clientId: String,
    val flow: String = "login",
    val userId: String? = null,
    val sessionId: String? = null,
    val apiBaseUrl: String = AppConfig.Api.BASE_URL,
    val verifyUrl: String = "https://ica-fivucsas.rollingcatsoftware.com/verify/",
    val locale: String = "en"
) {
    /**
     * Build the full URL with query parameters for the widget.
     */
    fun buildUrl(): String {
        val params = buildList {
            add("client_id=$clientId")
            add("flow=$flow")
            add("locale=$locale")
            add("api_base_url=$apiBaseUrl")
            if (userId != null) add("user_id=$userId")
            if (sessionId != null) add("session_id=$sessionId")
        }
        val separator = if (verifyUrl.contains("?")) "&" else "?"
        return verifyUrl + separator + params.joinToString("&")
    }
}

/**
 * Result returned by the auth widget on completion.
 */
@Serializable
data class AuthWidgetResult(
    val success: Boolean,
    val sessionId: String = "",
    val userId: String? = null,
    val completedMethods: List<String> = emptyList(),
    val token: String? = null
)

/**
 * Callback interface for auth widget events.
 */
interface AuthWidgetCallback {
    /** Called when auth completes successfully. */
    fun onComplete(result: AuthWidgetResult)

    /** Called when an error occurs. */
    fun onError(message: String)

    /** Called when the user cancels the auth flow. */
    fun onCancel()
}

/**
 * Checks whether the current platform supports the WebView-based auth widget.
 */
expect fun isAuthWidgetAvailable(): Boolean

/**
 * Launches the FIVUCSAS auth widget in a platform-native WebView.
 *
 * On Android: opens an Activity with an Android WebView.
 * On Desktop: opens a JavaFX WebView in a Swing/Compose dialog.
 * On iOS: placeholder (WKWebView integration future work).
 *
 * @param config  Widget configuration (client ID, flow, URLs, etc.)
 * @param callback Callback for completion, error, and cancel events.
 */
expect fun launchAuthWidget(config: AuthWidgetConfig, callback: AuthWidgetCallback)
