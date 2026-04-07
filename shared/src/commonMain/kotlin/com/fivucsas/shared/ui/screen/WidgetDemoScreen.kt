package com.fivucsas.shared.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s

// ── Code Examples ──────────────────────────────────────────────────

private val SCRIPT_TAG_EXAMPLE = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>FIVUCSAS Auth Demo</title>
</head>
<body>
  <!-- 1. Include the SDK (9.5KB gzipped, zero dependencies) -->
  <script src="https://cdn.fivucsas.com/auth.min.js"></script>

  <!-- 2. Add the Web Component -->
  <fivucsas-verify
    client-id="your-client-id"
    flow="login"
    locale="en"
    base-url="https://verify.fivucsas.com"
    api-base-url="https://api.fivucsas.com/api/v1"
  ></fivucsas-verify>

  <!-- 3. Listen for events -->
  <script>
    const el = document.querySelector('fivucsas-verify');
    el.addEventListener('fivucsas-complete', (e) => {
      console.log('Verified!', e.detail);
    });
    el.addEventListener('fivucsas-error', (e) => {
      console.error('Error:', e.detail.message);
    });
  </script>
</body>
</html>
""".trimIndent()

private val PROGRAMMATIC_EXAMPLE = """
import { FivucsasAuth } from '@fivucsas/auth-js';

const auth = new FivucsasAuth({
  clientId: 'your-client-id',
  baseUrl: 'https://verify.fivucsas.com',
  apiBaseUrl: 'https://api.fivucsas.com/api/v1',
  locale: 'en',
  theme: { primaryColor: '#6366f1', mode: 'light' },
});

// Option A: Modal overlay
const result = await auth.verify({
  flow: 'login',
  userId: 'user-123',
});

// Option B: Inline embed
const result = await auth.verify({
  flow: 'login',
  userId: 'user-123',
  container: '#verify-container',
  onStepChange: (step) => console.log(step),
  onError: (err) => console.error(err),
  onCancel: () => console.log('cancelled'),
});

console.log(result.success, result.completedMethods);
""".trimIndent()

private val REACT_EXAMPLE = """
import { useRef, useState, useCallback } from 'react';
import { FivucsasAuth } from '@fivucsas/auth-js';

export function VerifyButton({ userId, onComplete }) {
  const containerRef = useRef(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleVerify = useCallback(async () => {
    setLoading(true);
    setError(null);

    const auth = new FivucsasAuth({
      clientId: 'your-client-id',
      baseUrl: window.location.origin + '/verify',
      apiBaseUrl: import.meta.env.VITE_API_BASE_URL,
      locale: 'en',
      theme: { primaryColor: '#6366f1', borderRadius: '12px' },
    });

    try {
      const result = await auth.verify({
        flow: 'login',
        userId,
        container: containerRef.current ?? undefined,
      });
      onComplete(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [userId, onComplete]);

  return (
    <div>
      <button onClick={handleVerify} disabled={loading}>
        {loading ? 'Verifying...' : 'Verify Identity'}
      </button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <div ref={containerRef} />
    </div>
  );
}
""".trimIndent()

// ── Auth method definitions ────────────────────────────────────────

private data class AuthMethod(
    val name: String,
    val icon: ImageVector
)

private val AUTH_METHODS = listOf(
    AuthMethod("Password", Icons.Filled.Lock),
    AuthMethod("Face", Icons.Filled.Face),
    AuthMethod("Fingerprint", Icons.Filled.Fingerprint),
    AuthMethod("Voice", Icons.Filled.RecordVoiceOver),
    AuthMethod("TOTP", Icons.Filled.Timer),
    AuthMethod("Email OTP", Icons.Filled.Sms),
    AuthMethod("SMS OTP", Icons.Filled.Sms),
    AuthMethod("QR Code", Icons.Filled.QrCode2),
    AuthMethod("Hardware Key", Icons.Filled.Key),
    AuthMethod("NFC", Icons.Filled.Nfc),
)

// ── Architecture layer data ────────────────────────────────────────

private data class ArchitectureLayer(
    val title: String,
    val subtitle: String,
    val items: List<String>,
    val color: Color
)

// ── Main Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WidgetDemoScreen(
    onBack: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    val layers = remember {
        listOf(
            ArchitectureLayer(
                title = "SDK Layer",
                subtitle = "Client-side integration",
                items = listOf(
                    "FivucsasAuth SDK",
                    "Web Component",
                    "postMessage Bridge",
                    "Promise-based API",
                    "Event Callbacks"
                ),
                color = Color(0xFF6366F1)
            ),
            ArchitectureLayer(
                title = "Orchestration Layer",
                subtitle = "Server-side flow control",
                items = listOf(
                    "Multi-Step Controller",
                    "Session Manager",
                    "Step Router",
                    "Theme Engine",
                    "i18n (EN/TR)"
                ),
                color = Color(0xFF8B5CF6)
            ),
            ArchitectureLayer(
                title = "Biometric Capture Layer",
                subtitle = "Sensor & hardware access",
                items = listOf(
                    "Camera (Face)",
                    "Microphone (Voice)",
                    "WebAuthn (Fingerprint/Key)",
                    "NFC Reader",
                    "TOTP / OTP Input"
                ),
                color = Color(0xFF10B981)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // ── Page Header ──────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Web,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = s(StringKey.WIDGET_DEMO_TITLE),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = s(StringKey.WIDGET_DEMO_DESCRIPTION),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Platform Stats ───────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                value = "10",
                label = s(StringKey.WIDGET_DEMO_AUTH_METHODS),
                icon = Icons.Filled.Security,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = "9.5KB",
                label = s(StringKey.WIDGET_DEMO_SDK_SIZE),
                icon = Icons.Filled.Layers,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                value = "0",
                label = s(StringKey.WIDGET_DEMO_DEPENDENCIES),
                icon = Icons.Filled.Layers,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = "OAuth 2.0",
                label = s(StringKey.WIDGET_DEMO_OIDC_COMPATIBLE),
                icon = Icons.Filled.VerifiedUser,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Architecture Diagram ─────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = s(StringKey.WIDGET_DEMO_ARCHITECTURE),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = s(StringKey.WIDGET_DEMO_ARCHITECTURE_DESC),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                layers.forEachIndexed { index, layer ->
                    ArchitectureLayerCard(layer)
                    if (index < layers.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (index == 0) "postMessage" else "Internal API calls",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // ── Supported Auth Methods Grid ──────────────
                Text(
                    text = s(StringKey.WIDGET_DEMO_SUPPORTED_METHODS),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AUTH_METHODS.forEach { method ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(method.name, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = method.icon,
                                    contentDescription = method.name,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Integration Code Examples ────────────────────────
        IntegrationExamplesSection()
    }
}

// ── Sub-components ─────────────────────────────────────────────────

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArchitectureLayerCard(layer: ArchitectureLayer) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = layer.color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, layer.color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = layer.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = layer.color
            )
            Text(
                text = layer.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                layer.items.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = layer.color.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            layer.color.copy(alpha = 0.25f)
                        )
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Medium,
                            color = layer.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntegrationExamplesSection() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        s(StringKey.WIDGET_DEMO_TAB_HTML),
        s(StringKey.WIDGET_DEMO_TAB_JS),
        s(StringKey.WIDGET_DEMO_TAB_REACT)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = s(StringKey.WIDGET_DEMO_INTEGRATION_EXAMPLES),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            val description = when (selectedTab) {
                0 -> s(StringKey.WIDGET_DEMO_HTML_DESC)
                1 -> s(StringKey.WIDGET_DEMO_JS_DESC)
                2 -> s(StringKey.WIDGET_DEMO_REACT_DESC)
                else -> ""
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Code block
            val code = when (selectedTab) {
                0 -> SCRIPT_TAG_EXAMPLE
                1 -> PROGRAMMATIC_EXAMPLE
                2 -> REACT_EXAMPLE
                else -> ""
            }
            val language = when (selectedTab) {
                0 -> "HTML"
                1 -> "TypeScript"
                2 -> "React (TSX)"
                else -> ""
            }
            CodeBlockCard(code = code, language = language)
        }
    }
}

@Composable
private fun CodeBlockCard(
    code: String,
    language: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E)
        )
    ) {
        Column {
            // Header bar with language chip and copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = language,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA5B4FC),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                IconButton(
                    onClick = { /* Platform clipboard copy */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = s(StringKey.WIDGET_DEMO_COPY),
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Code content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFFCDD6F4)
                )
            }
        }
    }
}
