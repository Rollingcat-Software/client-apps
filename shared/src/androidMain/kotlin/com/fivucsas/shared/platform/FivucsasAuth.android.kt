package com.fivucsas.shared.platform

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import org.koin.core.context.GlobalContext

/**
 * Android actual implementations for the FIVUCSAS auth widget.
 *
 * Opens the auth widget in a dedicated Activity that hosts an Android WebView.
 * The WebView:
 * - Loads the verify URL with config params
 * - Grants camera and microphone permissions for biometric capture
 * - Listens for postMessage events via a JavaScript interface bridge
 * - Returns results to the caller via a static callback holder
 */

actual fun isAuthWidgetAvailable(): Boolean = true

actual fun launchAuthWidget(config: AuthWidgetConfig, callback: AuthWidgetCallback) {
    val context = GlobalContext.get().get<Context>()
    FivucsasAuthActivity.pendingCallback = callback
    FivucsasAuthActivity.pendingConfig = config
    val intent = Intent(context, FivucsasAuthActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

/**
 * Dedicated Activity hosting the auth widget WebView.
 *
 * Lifecycle:
 *   1. onCreate  → configures WebView, loads widget URL
 *   2. Widget posts "fivucsas:complete" / "fivucsas:error" / "fivucsas:cancel"
 *   3. JS bridge receives the message, invokes callback, finishes Activity
 *
 * Camera/mic permissions are auto-granted via WebChromeClient.onPermissionRequest()
 * because the widget needs them for face/voice biometrics.
 */
class FivucsasAuthActivity : Activity() {

    companion object {
        /** Static holder — safe because only one auth flow runs at a time. */
        internal var pendingCallback: AuthWidgetCallback? = null
        internal var pendingConfig: AuthWidgetConfig? = null
    }

    private var callbackFired = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = pendingConfig
        val callback = pendingCallback

        if (config == null || callback == null) {
            finish()
            return
        }

        val webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        setContentView(webView)

        // Configure WebView settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
            allowContentAccess = false
            cacheMode = WebSettings.LOAD_NO_CACHE
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }

        // Grant camera + microphone permissions for biometric capture
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                val grantable = request.resources.filter { resource ->
                    resource == PermissionRequest.RESOURCE_VIDEO_CAPTURE ||
                        resource == PermissionRequest.RESOURCE_AUDIO_CAPTURE
                }.toTypedArray()
                if (grantable.isNotEmpty()) {
                    request.grant(grantable)
                } else {
                    request.deny()
                }
            }
        }

        // Prevent navigation away from the widget domain
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val host = request.url.host ?: return true
                // Allow navigation within the same domain
                val widgetHost = Uri.parse(config.verifyUrl).host
                return host != widgetHost
            }
        }

        // JavaScript bridge for postMessage communication
        webView.addJavascriptInterface(
            AuthBridge(callback) { fireCallbackOnce() },
            "FivucsasNative"
        )

        // Inject a script that forwards postMessage events to the native bridge
        val bridgeScript = """
            <script>
            window.addEventListener('message', function(event) {
                try {
                    var data = (typeof event.data === 'string') ? JSON.parse(event.data) : event.data;
                    if (data && data.type) {
                        FivucsasNative.onMessage(JSON.stringify(data));
                    }
                } catch(e) { /* ignore non-JSON messages */ }
            });
            </script>
        """.trimIndent()

        // Load the widget URL
        val url = config.buildUrl()
        webView.loadUrl(url)

        // After page loads, inject the bridge script
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val host = request.url.host ?: return true
                val widgetHost = Uri.parse(config.verifyUrl).host
                return host != widgetHost
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.evaluateJavascript("""
                    (function() {
                        window.addEventListener('message', function(event) {
                            try {
                                var data = (typeof event.data === 'string') ? JSON.parse(event.data) : event.data;
                                if (data && data.type && data.type.startsWith('fivucsas:')) {
                                    FivucsasNative.onMessage(JSON.stringify(data));
                                }
                            } catch(e) {}
                        });
                    })();
                """.trimIndent(), null)
            }
        }
    }

    private fun fireCallbackOnce() {
        if (!callbackFired) {
            callbackFired = true
            pendingCallback = null
            pendingConfig = null
            finish()
        }
    }

    override fun onBackPressed() {
        if (!callbackFired) {
            callbackFired = true
            pendingCallback?.onCancel()
            pendingCallback = null
            pendingConfig = null
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        if (!callbackFired) {
            pendingCallback?.onCancel()
            pendingCallback = null
            pendingConfig = null
        }
        super.onDestroy()
    }
}

/**
 * JavaScript interface bridge that receives postMessage data from the WebView.
 */
private class AuthBridge(
    private val callback: AuthWidgetCallback,
    private val finishActivity: () -> Unit
) {
    @JavascriptInterface
    fun onMessage(jsonString: String) {
        try {
            // Parse the JSON manually to avoid pulling in extra dependencies
            val type = extractJsonString(jsonString, "type")
            when (type) {
                "fivucsas:complete" -> {
                    val result = AuthWidgetResult(
                        success = extractJsonBoolean(jsonString, "success"),
                        sessionId = extractJsonString(jsonString, "sessionId") ?: "",
                        userId = extractJsonString(jsonString, "userId"),
                        completedMethods = extractJsonArray(jsonString, "completedMethods"),
                        token = extractJsonString(jsonString, "token")
                    )
                    callback.onComplete(result)
                    finishActivity()
                }
                "fivucsas:error" -> {
                    val message = extractJsonString(jsonString, "message") ?: "Unknown error"
                    callback.onError(message)
                    finishActivity()
                }
                "fivucsas:cancel" -> {
                    callback.onCancel()
                    finishActivity()
                }
            }
        } catch (e: Exception) {
            callback.onError("Failed to parse widget message: ${e.message}")
            finishActivity()
        }
    }

    // Minimal JSON extraction helpers — avoids kotlinx.serialization dependency
    // on the Android-specific bridge class.

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = """"$key"\s*:\s*"([^"]*?)"""".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1)
    }

    private fun extractJsonBoolean(json: String, key: String): Boolean {
        val pattern = """"$key"\s*:\s*(true|false)""".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1) == "true"
    }

    private fun extractJsonArray(json: String, key: String): List<String> {
        val pattern = """"$key"\s*:\s*\[([^\]]*)\]""".toRegex()
        val match = pattern.find(json)?.groupValues?.getOrNull(1) ?: return emptyList()
        return match.split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    }
}
