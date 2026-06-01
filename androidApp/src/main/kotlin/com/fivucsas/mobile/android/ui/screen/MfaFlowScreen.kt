package com.fivucsas.mobile.android.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import com.fivucsas.mobile.android.ui.util.toCompressedJpegBytes
import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import com.fivucsas.shared.data.remote.dto.MfaChallengeData
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.StringResources
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.platform.WebAuthnAuthenticator
import com.fivucsas.shared.platform.provideWebAuthnAuthenticator
import com.fivucsas.shared.presentation.viewmodel.auth.MfaFlowUiState
import com.fivucsas.shared.presentation.viewmodel.auth.MfaFlowViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.io.File

/**
 * MFA Flow Screen
 *
 * Manages the full N-step MFA verification flow:
 * - Method selection for each step
 * - Step-specific input UIs (OTP, TOTP, etc.)
 * - Progress indicator (Step X of Y)
 * - Navigation back to method picker
 */
@Composable
fun MfaFlowScreen(
    viewModel: MfaFlowViewModel,
    onAuthenticated: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Navigate to dashboard when authenticated
    LaunchedEffect(uiState) {
        if (uiState is MfaFlowUiState.Authenticated) {
            onAuthenticated()
        }
        // Cancelled is a terminal state — bubble it back up to the host nav.
        if (uiState is MfaFlowUiState.Cancelled) {
            onCancel()
        }
    }

    // Overlay QR scanner when requested from QR_CODE step
    var showQrScanner by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // PR #25: server-side cancel via DELETE /auth/mfa/session/{token}, then
    // navigate back. Failures are absorbed inside the ViewModel — the user
    // must always be able to leave the screen.
    val cancelClick: () -> Unit = {
        scope.launch { viewModel.cancelSession() }
    }

    if (showQrScanner) {
        QrScannerScreen(
            onQrScanned = { rawValue ->
                showQrScanner = false
                scope.launch {
                    viewModel.verifyStep("QR_CODE", mapOf("token" to rawValue))
                }
            },
            onBack = { showQrScanner = false },
            title = s(StringKey.MFA_METHOD_QR_CODE),
            instruction = s(StringKey.MFA_POINT_CAMERA_AT_QR)
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Keep all content (incl. the bottom Cancel / action buttons) clear
            // of the status bar and the system navigation bar so nothing is
            // occluded on gesture-nav or 3-button-nav devices. This screen has
            // no Scaffold, so the insets must be consumed here explicitly.
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = s(StringKey.MFA_TITLE),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is MfaFlowUiState.Idle -> {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = s(StringKey.MFA_PREPARING),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(onClick = cancelClick) {
                        Text(s(StringKey.CANCEL))
                    }
                }

                is MfaFlowUiState.MethodSelection -> {
                    MfaMethodSelectionContent(
                        availableMethods = state.availableMethods,
                        currentStep = state.currentStep,
                        totalSteps = state.totalSteps,
                        onMethodSelected = { viewModel.selectMethod(it) },
                        onCancel = cancelClick
                    )
                }

                is MfaFlowUiState.StepInput -> {
                    MfaStepInputContent(
                        method = state.method,
                        currentStep = state.currentStep,
                        totalSteps = state.totalSteps,
                        viewModel = viewModel,
                        onBack = { viewModel.backToMethodSelection() },
                        onOpenQrScanner = { showQrScanner = true },
                        alternativeMethods = viewModel.currentAlternativeMethods(),
                        onSwitchMethod = { newMethod ->
                            scope.launch { viewModel.switchMethod(newMethod) }
                        }
                    )
                }

                is MfaFlowUiState.Verifying -> {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = s(StringKey.MFA_VERIFYING),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is MfaFlowUiState.Authenticated -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = s(StringKey.MFA_AUTHENTICATED),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                is MfaFlowUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (state.canRetry) {
                        Button(onClick = { viewModel.retry() }) {
                            Text(s(StringKey.MFA_ERROR_RETRY))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = cancelClick) {
                        Text(s(StringKey.CANCEL))
                    }
                }

                is MfaFlowUiState.Cancelled -> {
                    // Terminal — LaunchedEffect above will dispatch onCancel().
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }

                is MfaFlowUiState.NeedsEnrollment -> {
                    NeedsEnrollmentContent(
                        method = state.method,
                        description = state.description,
                        onEnroll = { openInBrowser(context, state.enrollmentUrl) },
                        onCancel = cancelClick
                    )
                }
            }
        }
    }
}

/**
 * Open [url] in an external browser. Tries Chrome Custom Tabs first via the
 * `androidx.browser` library if it's on the classpath, otherwise falls back
 * to a plain `ACTION_VIEW` intent. The fallback path keeps this screen
 * compilable without requiring a new dependency.
 */
private fun openInBrowser(context: Context, url: String) {
    if (url.isBlank()) return
    // Server may return a relative path like "/enroll/totp". Resolve it
    // against the auth host so the device can actually open it.
    val absolute = if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        val base = "https://auth.rollingcatsoftware.com"
        if (url.startsWith("/")) "$base$url" else "$base/$url"
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(absolute))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        // No browser available — silently swallow; the screen stays on
        // NeedsEnrollment so the user can cancel.
    }
}

@Composable
private fun NeedsEnrollmentContent(
    method: String,
    description: String?,
    onEnroll: () -> Unit,
    onCancel: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.Security,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(64.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = s(StringKey.MFA_NEEDS_ENROLLMENT_TITLE),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = description?.takeIf { it.isNotBlank() }
            ?: StringResources.get(StringKey.MFA_NEEDS_ENROLLMENT_DESC, methodDisplayName(method)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onEnroll,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(s(StringKey.MFA_ENROLL_NOW))
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = onCancel) {
        Text(s(StringKey.CANCEL))
    }
}

/**
 * Step progress indicator + method selection cards.
 */
@Composable
private fun MfaMethodSelectionContent(
    availableMethods: List<AvailableMethodDto>,
    currentStep: Int,
    totalSteps: Int,
    onMethodSelected: (String) -> Unit,
    onCancel: () -> Unit
) {
    // Progress
    StepProgressIndicator(currentStep = currentStep, totalSteps = totalSteps)

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = s(StringKey.MFA_SELECT_METHOD),
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = s(StringKey.MFA_SELECT_METHOD_DESC),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Method cards
    availableMethods.forEach { method ->
        MethodCard(
            method = method,
            onClick = { onMethodSelected(method.methodType) }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = onCancel) {
        Text(s(StringKey.CANCEL))
    }
}

/**
 * Step input for the selected method.
 * Renders appropriate input UI based on the method type.
 */
@Composable
private fun MfaStepInputContent(
    method: String,
    currentStep: Int,
    totalSteps: Int,
    viewModel: MfaFlowViewModel,
    onBack: () -> Unit,
    onOpenQrScanner: () -> Unit = {},
    alternativeMethods: List<AvailableMethodDto> = emptyList(),
    onSwitchMethod: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    StepProgressIndicator(currentStep = currentStep, totalSteps = totalSteps)

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = methodDisplayName(method),
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(16.dp))

    when (method) {
        "TOTP" -> {
            TotpStepInput(
                onVerify = { code ->
                    scope.launch {
                        viewModel.verifyStep(method, mapOf("code" to code))
                    }
                }
            )
        }

        "EMAIL_OTP", "SMS_OTP" -> {
            OtpStepInput(
                method = method,
                onSendOtp = {
                    scope.launch { viewModel.sendOtp(method) }
                },
                onVerify = { code ->
                    scope.launch {
                        viewModel.verifyStep(method, mapOf("code" to code))
                    }
                }
            )
        }

        "QR_CODE" -> {
            QrCodeStepInput(
                viewModel = viewModel,
                onOpenQrScanner = onOpenQrScanner,
                onVerify = {
                    scope.launch {
                        viewModel.verifyStep(method)
                    }
                }
            )
        }

        "FACE" -> {
            FaceMfaStepInput(
                onCapture = { jpegBytes ->
                    val base64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
                    scope.launch {
                        // Backend FaceAuthHandler reads `data.get("image")`.
                        viewModel.verifyStep(method, mapOf("image" to base64))
                    }
                }
            )
        }

        "VOICE" -> {
            VoiceMfaStepInput(
                onRecorded = { audioBase64 ->
                    scope.launch {
                        // Backend VoiceAuthHandler reads `data.get("voiceData")`.
                        viewModel.verifyStep(method, mapOf("voiceData" to audioBase64))
                    }
                }
            )
        }

        "FINGERPRINT" -> {
            FingerprintMfaStepInput(
                viewModel = viewModel,
                method = method,
                authenticatorAttachment = "platform",
                onVerify = { assertionPayload ->
                    scope.launch {
                        // Backend FingerprintAuthHandler accepts `fingerprintData` (or `assertion`).
                        viewModel.verifyStep(method, mapOf("fingerprintData" to assertionPayload))
                    }
                }
            )
        }

        "HARDWARE_KEY" -> {
            FingerprintMfaStepInput(
                viewModel = viewModel,
                method = method,
                authenticatorAttachment = "cross-platform",
                onVerify = { assertionPayload ->
                    scope.launch {
                        // Backend HardwareKeyAuthHandler reads `assertion` (base64 JSON blob).
                        viewModel.verifyStep(method, mapOf("assertion" to assertionPayload))
                    }
                }
            )
        }

        "NFC_DOCUMENT" -> {
            // Passport / eID BAC read → payload sent to NfcDocumentAuthHandler.
            NfcStepScreen(
                onVerify = { data ->
                    scope.launch {
                        viewModel.verifyStep(method, data)
                    }
                }
            )
        }

        "PASSWORD" -> {
            // Config-driven flows can require PASSWORD as a step-2+ factor.
            // Backend PasswordVerifyMfaStepHandler reads data.get("password").
            PasswordStepInput(
                onVerify = { password ->
                    scope.launch {
                        viewModel.verifyStep(method, mapOf("password" to password))
                    }
                }
            )
        }

        else -> {
            Text(
                text = "Unsupported method: $method",
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // PR #25: surface "Try a different method" using the latest server-supplied
    // alternativeMethods. Each pick fires POST /auth/mfa/switch-method which
    // re-dispatches OTP for EMAIL/SMS_OTP server-side.
    val filteredAlternatives = alternativeMethods.filter { it.methodType != method }
    if (filteredAlternatives.isNotEmpty()) {
        Text(
            text = s(StringKey.MFA_TRY_DIFFERENT_METHOD),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        filteredAlternatives.forEach { alt ->
            OutlinedButton(
                onClick = { onSwitchMethod(alt.methodType) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = methodIcon(alt.methodType),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(methodDisplayName(alt.methodType))
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    TextButton(onClick = onBack) {
        Text(s(StringKey.MFA_BACK_TO_METHODS))
    }
}

// ── Step Input Composables ──────────────────────────────────────────

@Composable
private fun TotpStepInput(onVerify: (String) -> Unit) {
    var code by remember { mutableStateOf("") }

    OutlinedTextField(
        value = code,
        onValueChange = { if (it.length <= 6) code = it.filter { c -> c.isDigit() } },
        label = { Text(s(StringKey.MFA_ENTER_CODE)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { onVerify(code) },
        modifier = Modifier.fillMaxWidth(),
        enabled = code.length == 6
    ) {
        Text(s(StringKey.MFA_VERIFY))
    }
}

@Composable
private fun PasswordStepInput(onVerify: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text(s(StringKey.MFA_METHOD_PASSWORD)) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { onVerify(password) },
        modifier = Modifier.fillMaxWidth(),
        enabled = password.isNotEmpty()
    ) {
        Text(s(StringKey.MFA_VERIFY))
    }
}

@Composable
private fun OtpStepInput(
    method: String,
    onSendOtp: () -> Unit,
    onVerify: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (!otpSent) {
        Button(
            onClick = {
                onSendOtp()
                otpSent = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(s(StringKey.MFA_SEND_OTP))
        }
    } else {
        Text(
            text = s(StringKey.MFA_OTP_SENT),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 6) code = it.filter { c -> c.isDigit() } },
            label = { Text(s(StringKey.MFA_ENTER_CODE)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onVerify(code) },
            modifier = Modifier.fillMaxWidth(),
            enabled = code.length == 6
        ) {
            Text(s(StringKey.MFA_VERIFY))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onSendOtp) {
            Text(s(StringKey.MFA_RESEND_OTP))
        }
    }
}

@Composable
private fun QrCodeStepInput(
    viewModel: MfaFlowViewModel,
    onOpenQrScanner: () -> Unit,
    onVerify: () -> Unit
) {
    var qrToken by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.generateQr().fold(
            onSuccess = { response ->
                qrToken = response.qrToken
                loading = false
            },
            onFailure = {
                loading = false
            }
        )
    }

    if (loading) {
        CircularProgressIndicator()
    } else if (qrToken != null) {
        Text(
            text = s(StringKey.MFA_SCAN_QR),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Render QR code as a visual image using qrose
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberQrCodePainter(data = qrToken ?: ""),
                contentDescription = s(StringKey.MFA_SCAN_QR),
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary action: scan the QR code shown on the web/desktop with the camera
        Button(
            onClick = onOpenQrScanner,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(s(StringKey.MFA_SCAN_QR_CAMERA))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary action: confirm that another device already scanned this app's QR
        OutlinedButton(onClick = onVerify, modifier = Modifier.fillMaxWidth()) {
            Text(s(StringKey.MFA_VERIFY))
        }
    } else {
        Text(
            text = s(StringKey.ERROR),
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * FACE MFA step — captures a single front-camera frame and returns compressed JPEG bytes.
 * The bytes are then base64-encoded and submitted as `{"image": "<base64>"}` (matching
 * the FaceAuthHandler contract on the backend).
 */
@Composable
private fun FaceMfaStepInput(onCapture: (ByteArray) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    var captureError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraController.unbind() }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = s(StringKey.FACE_VERIFY_INSTRUCTIONS),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            controller = cameraController
                            cameraController.bindToLifecycle(lifecycleOwner)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .border(
                            BorderStroke(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            captureError?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (capturing) return@Button
                    capturing = true
                    captureError = null
                    cameraController.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bytes = image.toCompressedJpegBytes()
                                image.close()
                                capturing = false
                                onCapture(bytes)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                capturing = false
                                captureError = exception.message
                                    ?: StringResources.get(StringKey.FACE_VERIFY_CAPTURE_ERROR, "")
                            }
                        }
                    )
                },
                enabled = !capturing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(s(StringKey.FACE_VERIFY_BUTTON))
            }
        } else {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = s(StringKey.FACE_VERIFY_PERMISSION_RATIONALE),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s(StringKey.COMMON_GRANT_CAMERA_PERMISSION))
            }
        }
    }
}

/**
 * VOICE MFA step — records up to ~5 s of mono 16 kHz AAC-in-MP4 audio and returns it
 * as a base64 string. Submitted as `{"voiceData": "<base64>"}` (matching the
 * VoiceAuthHandler contract on the backend).
 */
@Composable
private fun VoiceMfaStepInput(onRecorded: (String) -> Unit) {
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(false) }
    var recording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var seconds by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(recording) {
        if (recording) {
            seconds = 0
            while (recording) {
                delay(1000)
                seconds += 1
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { recorder?.stop() } catch (_: Exception) {}
            try { recorder?.release() } catch (_: Exception) {}
        }
    }

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "mfa_voice_${System.currentTimeMillis()}.m4a")
            audioFile = file
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setAudioSamplingRate(16000)
            mr.setAudioChannels(1)
            mr.setOutputFile(file.absolutePath)
            mr.prepare()
            mr.start()
            recorder = mr
            recording = true
            error = null
        } catch (e: Exception) {
            error = e.message
            recording = false
        }
    }

    fun stopRecordingAndSubmit() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
        recording = false

        val file = audioFile
        if (file == null || !file.exists()) {
            error = StringResources.get(StringKey.MFA_GENERIC_ERROR)
            return
        }
        val bytes = file.readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        file.delete()
        onRecorded(base64)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.RecordVoiceOver,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = s(StringKey.VOICE_VERIFY_INSTRUCTION),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (recording) {
            Text(
                text = "${s(StringKey.VOICE_RECORDING)}... ${seconds}s",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        error?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (hasPermission) {
            Button(
                onClick = {
                    if (recording) {
                        stopRecordingAndSubmit()
                    } else {
                        startRecording()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (recording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (recording) s(StringKey.MFA_VERIFY) else s(StringKey.VOICE_TAP_TO_RECORD)
                )
            }
        } else {
            Text(
                text = s(StringKey.VOICE_PERMISSION_REQUIRED),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s(StringKey.MFA_VERIFY))
            }
        }
    }
}

/**
 * Shared UI for FINGERPRINT and HARDWARE_KEY MFA steps. Drives a full WebAuthn
 * round trip:
 *   1) POST /auth/mfa/step with `data: { action: "challenge" }` to fetch the challenge.
 *   2) Hand the challenge to the platform [WebAuthnAuthenticator] (Credential Manager)
 *      with the requested authenticator attachment ("platform" for fingerprint /
 *      device biometric, "cross-platform" for USB/NFC/BLE security keys).
 *   3) Pack `{credentialId, authenticatorData, clientDataJSON, signature}` into a
 *      base64-encoded JSON blob and forward it via [onVerify].
 */
@Composable
private fun FingerprintMfaStepInput(
    viewModel: MfaFlowViewModel,
    method: String,
    authenticatorAttachment: String,
    onVerify: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val authenticator = remember { provideWebAuthnAuthenticator() }

    var processing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val isFingerprint = method == "FINGERPRINT"
    val icon = if (isFingerprint) Icons.Default.Fingerprint else Icons.Default.Key
    val instruction = if (isFingerprint) {
        s(StringKey.MFA_METHOD_FINGERPRINT)
    } else {
        s(StringKey.HW_TOKEN_VERIFY_DESC)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        error?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (processing) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = s(StringKey.MFA_VERIFYING),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Button(
                onClick = {
                    processing = true
                    error = null
                    scope.launch {
                        runWebAuthnAssertion(
                            viewModel = viewModel,
                            method = method,
                            authenticator = authenticator,
                            authenticatorAttachment = authenticatorAttachment,
                            onSuccess = { payload ->
                                processing = false
                                onVerify(payload)
                            },
                            onError = { msg ->
                                processing = false
                                error = msg
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isFingerprint) {
                        s(StringKey.MFA_METHOD_FINGERPRINT)
                    } else {
                        s(StringKey.HW_TOKEN_VERIFY_BUTTON)
                    }
                )
            }
        }
    }
}

/**
 * Drive one challenge → assertion round-trip and call [onSuccess] with the base64
 * JSON blob the backend WebAuthn handlers expect.
 */
/**
 * NOTE: [authenticatorAttachment] is currently advisory — the server-side challenge
 * already filters `allowCredentials` by transport type (internal vs. external) so the
 * Credential Manager can only return a credential of the right kind. We still accept
 * it as a parameter for symmetry with the registration flow and for future tightening.
 */
@Suppress("UNUSED_PARAMETER")
private suspend fun runWebAuthnAssertion(
    viewModel: MfaFlowViewModel,
    method: String,
    authenticator: WebAuthnAuthenticator,
    authenticatorAttachment: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val challengeResult = viewModel.requestStepUpChallenge(method)
    val challengeData: MfaChallengeData = challengeResult.fold(
        onSuccess = { it },
        onFailure = { err ->
            onError(err.message ?: StringResources.get(StringKey.MFA_GENERIC_ERROR))
            return
        }
    )

    if (challengeData.challenge.isBlank()) {
        onError(StringResources.get(StringKey.MFA_GENERIC_ERROR))
        return
    }

    val assertion = try {
        authenticator.getAssertion(
            rpId = challengeData.rpId,
            challenge = challengeData.challenge,
            allowCredentialIds = challengeData.allowCredentials,
            userVerification = "required"
        )
    } catch (e: Exception) {
        onError(e.message ?: StringResources.get(StringKey.MFA_GENERIC_ERROR))
        return
    }

    // Backend expects a base64-encoded JSON object exactly matching what the web
    // FingerprintStep/HardwareKeyStep ship: { credentialId, authenticatorData,
    // clientDataJSON, signature }.
    val payloadJson = buildJsonObject {
        put("credentialId", JsonPrimitive(assertion.credentialId))
        put("authenticatorData", JsonPrimitive(assertion.authenticatorData))
        put("clientDataJSON", JsonPrimitive(assertion.clientDataJson))
        put("signature", JsonPrimitive(assertion.signature))
    }.toString()

    val base64Payload = Base64.encodeToString(
        payloadJson.toByteArray(Charsets.UTF_8),
        Base64.NO_WRAP
    )
    onSuccess(base64Payload)
}

// ── Shared UI Components ──────────────────────────────────────────

@Composable
private fun StepProgressIndicator(currentStep: Int, totalSteps: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = StringResources.get(StringKey.MFA_STEP_COUNTER, currentStep, totalSteps),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MethodCard(
    method: AvailableMethodDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (method.preferred)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = methodIcon(method.methodType),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = methodDisplayName(method.methodType),
                    style = MaterialTheme.typography.titleSmall
                )
                if (method.name.isNotBlank() && method.name != method.methodType) {
                    Text(
                        text = method.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (method.preferred) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────

private fun methodIcon(methodType: String): ImageVector = when (methodType) {
    "TOTP" -> Icons.Default.Security
    "EMAIL_OTP" -> Icons.Default.Email
    "SMS_OTP" -> Icons.Default.PhoneAndroid
    "FACE" -> Icons.Default.Face
    "VOICE" -> Icons.Default.RecordVoiceOver
    "FINGERPRINT" -> Icons.Default.Fingerprint
    "QR_CODE" -> Icons.Default.QrCode
    "HARDWARE_KEY" -> Icons.Default.Key
    "NFC_DOCUMENT" -> Icons.Default.Nfc
    "PASSWORD" -> Icons.Default.Lock
    else -> Icons.Default.Security
}

private fun methodDisplayName(methodType: String): String = when (methodType) {
    "TOTP" -> s(StringKey.MFA_METHOD_TOTP)
    "EMAIL_OTP" -> s(StringKey.MFA_METHOD_EMAIL_OTP)
    "SMS_OTP" -> s(StringKey.MFA_METHOD_SMS_OTP)
    "FACE" -> s(StringKey.MFA_METHOD_FACE)
    "VOICE" -> s(StringKey.MFA_METHOD_VOICE)
    "FINGERPRINT" -> s(StringKey.MFA_METHOD_FINGERPRINT)
    "QR_CODE" -> s(StringKey.MFA_METHOD_QR_CODE)
    "HARDWARE_KEY" -> s(StringKey.MFA_METHOD_HARDWARE_KEY)
    "NFC_DOCUMENT" -> s(StringKey.MFA_METHOD_NFC)
    "PASSWORD" -> s(StringKey.MFA_METHOD_PASSWORD)
    else -> methodType
}
