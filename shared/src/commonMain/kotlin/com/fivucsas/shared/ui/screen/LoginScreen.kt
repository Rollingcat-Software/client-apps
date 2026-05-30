package com.fivucsas.shared.ui.screen

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.MfaHandoff
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import kotlinx.coroutines.launch

/**
 * LoginScreen — renders the active auth flow's *primary* step.
 *
 * After PR #18 (server side), tenants can configure ANY auth method
 * (PASSWORD, EMAIL_OTP, FACE, TOTP, ...) as the first step of an
 * APP_LOGIN flow. On mount we ask the backend for the active flow and
 * branch on `primaryStepMethod`:
 *
 *  - `PASSWORD` (or null/legacy / unknown tenant): legacy email + password
 *    form. The backend's `/auth/login` endpoint handles it as today.
 *  - `EMAIL_OTP`, `FACE`, `TOTP`: a thin pre-screen that collects the
 *    user's email and then routes through the existing MFA pipeline,
 *    where the dedicated step composables already live.
 *  - Any other type (SMS_OTP / QR_CODE / NFC_DOCUMENT / HARDWARE_KEY /
 *    FINGERPRINT / VOICE as PRIMARY): a "not supported in this app yet"
 *    fallback with a link to the web sign-in page.
 *
 * Discovery is best-effort. If the backend rejects the unauthenticated
 * call (the per-tenant auth-flows endpoint requires admin auth), the
 * ViewModel collapses the failure to `primaryStepMethod = "PASSWORD"`,
 * which keeps the existing happy-path completely intact.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    onMfaRequired: (MfaHandoff) -> Unit = {},
    onOpenWebSignIn: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    // Kick off active-flow discovery once on mount. The ViewModel guards
    // against duplicate work — once `primaryStepMethod` is set, this is
    // a no-op (so resetState() won't re-trigger a shimmer).
    LaunchedEffect(Unit) {
        viewModel.loadActiveFlow(tenantId = null)
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess && state.tokens != null) {
            onLoginSuccess()
        }
    }

    // Navigate to MFA flow when MFA is required (this fires both for the
    // PASSWORD path's downstream MFA step AND for passwordless primary
    // steps that hand off to the MFA pipeline immediately).
    //
    // The session token + available methods + step counters are carried
    // forward as an explicit [MfaHandoff] payload (encoded into the nav
    // route), NOT read back off a fresh LoginViewModel factory instance on
    // the MfaFlow screen — that was the v5.2.1 login-bounce bug.
    LaunchedEffect(state.mfaRequired) {
        val token = state.mfaSessionToken
        if (state.mfaRequired && token != null) {
            onMfaRequired(
                MfaHandoff(
                    sessionToken = token,
                    methods = state.mfaAvailableMethods ?: emptyList(),
                    step = state.mfaCurrentStep,
                    total = state.mfaTotalSteps
                )
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = s(StringKey.APP_NAME),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = s(StringKey.APP_SUBTITLE),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            when {
                state.flowDiscoveryLoading -> PrimaryStepShimmer()
                else -> when (state.primaryStepMethod) {
                    // EMAIL_OTP / FACE / TOTP — passwordless primaries that
                    // delegate to the MFA pipeline. Until we hoist the
                    // dedicated step composables out of androidApp, these
                    // share a common pre-screen that collects the email.
                    "EMAIL_OTP" -> PasswordlessPrimaryStep(
                        title = s(StringKey.LOGIN_PRIMARY_EMAIL_OTP_TITLE),
                        instruction = s(StringKey.LOGIN_PRIMARY_EMAIL_OTP_INSTRUCTION),
                        viewModel = viewModel,
                        scope = scope,
                        onNavigateToForgotPassword = onNavigateToForgotPassword,
                        onNavigateToRegister = onNavigateToRegister
                    )
                    "FACE" -> PasswordlessPrimaryStep(
                        title = s(StringKey.LOGIN_PRIMARY_FACE_TITLE),
                        instruction = s(StringKey.LOGIN_PRIMARY_FACE_INSTRUCTION),
                        viewModel = viewModel,
                        scope = scope,
                        onNavigateToForgotPassword = onNavigateToForgotPassword,
                        onNavigateToRegister = onNavigateToRegister
                    )
                    "TOTP" -> PasswordlessPrimaryStep(
                        title = s(StringKey.LOGIN_PRIMARY_TOTP_TITLE),
                        instruction = s(StringKey.LOGIN_PRIMARY_TOTP_INSTRUCTION),
                        viewModel = viewModel,
                        scope = scope,
                        onNavigateToForgotPassword = onNavigateToForgotPassword,
                        onNavigateToRegister = onNavigateToRegister
                    )
                    // Methods we don't yet render dynamically as PRIMARY.
                    // SMS_OTP / QR_CODE / NFC_DOCUMENT / HARDWARE_KEY /
                    // FINGERPRINT / VOICE all fall through here.
                    "SMS_OTP", "QR_CODE", "NFC_DOCUMENT", "HARDWARE_KEY",
                    "FINGERPRINT", "VOICE" -> UnsupportedPrimaryStep(
                        method = state.primaryStepMethod ?: "",
                        onOpenWebSignIn = onOpenWebSignIn,
                        onNavigateToRegister = onNavigateToRegister
                    )
                    // PASSWORD, null, or any unrecognised type — render the
                    // legacy email + password form. This is the safe default
                    // and preserves the happy-path login.
                    else -> LegacyPasswordPrimaryStep(
                        viewModel = viewModel,
                        scope = scope,
                        onNavigateToForgotPassword = onNavigateToForgotPassword,
                        onNavigateToRegister = onNavigateToRegister
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryStepShimmer() {
    CircularProgressIndicator(modifier = Modifier.size(40.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = s(StringKey.LOGIN_PRIMARY_LOADING),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

/**
 * Legacy email + password primary step — unchanged behaviour from before
 * PR #18. Calls `viewModel.login(email, password)` which talks to
 * `/auth/login` and either returns tokens directly or escalates to MFA.
 */
@Composable
private fun LegacyPasswordPrimaryStep(
    viewModel: LoginViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(s(StringKey.EMAIL)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text(s(StringKey.PASSWORD)) },
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                enabled = !state.isLoading
            ) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    },
                    contentDescription = if (passwordVisible) {
                        s(StringKey.HIDE_PASSWORD)
                    } else {
                        s(StringKey.SHOW_PASSWORD)
                    }
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(
        onClick = onNavigateToForgotPassword,
        enabled = !state.isLoading
    ) {
        Text(s(StringKey.FORGOT_PASSWORD))
    }

    if (state.error != null) {
        Text(
            text = state.error!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = {
            scope.launch {
                viewModel.login(email, password)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(s(StringKey.LOGIN))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(
        onClick = onNavigateToRegister,
        enabled = !state.isLoading
    ) {
        Text(s(StringKey.DONT_HAVE_ACCOUNT))
    }
}

/**
 * Passwordless primary step (EMAIL_OTP / FACE / TOTP) — collects the
 * user's email, then submits via the legacy `/auth/login` endpoint with
 * an empty password.
 *
 * The backend's adaptive-MFA path responds with an MFA challenge whose
 * first step is the configured primary method, so the existing
 * `MfaFlowScreen` picks it up and renders the dedicated step UI
 * (EmailOtpStep / FaceCaptureStep / TotpStepInput) without changes.
 *
 * NOTE: this avoids duplicating each method-specific UI inside the
 * shared LoginScreen. A future pass can hoist the method composables
 * out of `androidApp/MfaFlowScreen.kt` and render them here directly,
 * skipping the `mfa_session_token` round-trip.
 */
@Composable
private fun PasswordlessPrimaryStep(
    title: String,
    instruction: String,
    viewModel: LoginViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = instruction,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(s(StringKey.EMAIL)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading
    )

    if (state.error != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.error!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = {
            scope.launch {
                // Empty password triggers the adaptive-MFA path on the
                // backend: the response is an MFA challenge starting with
                // the configured primary method. The existing onMfaRequired
                // navigation handler in this screen will route to MfaFlowScreen.
                viewModel.login(email, password = "")
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isLoading && email.isNotBlank()
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(s(StringKey.LOGIN_PRIMARY_CONTINUE))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = onNavigateToForgotPassword, enabled = !state.isLoading) {
        Text(s(StringKey.FORGOT_PASSWORD))
    }
    TextButton(onClick = onNavigateToRegister, enabled = !state.isLoading) {
        Text(s(StringKey.DONT_HAVE_ACCOUNT))
    }
}

/**
 * Fallback for primary methods we cannot yet render natively.
 *
 * The user is told their tenant has configured a method this app version
 * doesn't support as PRIMARY, and offered a link to the web sign-in page.
 */
@Composable
private fun UnsupportedPrimaryStep(
    method: String,
    onOpenWebSignIn: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Text(
        text = s(StringKey.LOGIN_PRIMARY_UNSUPPORTED_TITLE),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = method,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = s(StringKey.LOGIN_PRIMARY_UNSUPPORTED_BODY),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onOpenWebSignIn,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(s(StringKey.LOGIN_PRIMARY_OPEN_WEB))
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = onNavigateToRegister) {
        Text(s(StringKey.DONT_HAVE_ACCOUNT))
    }
}
