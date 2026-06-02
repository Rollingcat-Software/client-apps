package com.fivucsas.mobile.android.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fivucsas.mobile.android.auth.HostedAuthManager
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.remote.api.IdentityApi
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.ui.theme.AppColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Hosted-first sign-in launcher. The whole credential + MFA ceremony happens on
 * `verify.fivucsas.com` inside a Chrome Custom Tab (see [HostedAuthManager]); this
 * native screen only kicks it off and, on success, fetches the profile for role
 * routing. Styled to mirror the hosted login card (brand mark, "SECURED BY
 * FIVUCSAS" pill, gradient action, verify.fivucsas.com footer).
 */
@Composable
fun HostedLoginScreen(
    onLoginSuccess: (role: String) -> Unit,
) {
    val context = LocalContext.current
    val tokenManager = koinInject<TokenManager>()
    val identityApi = koinInject<IdentityApi>()
    val scope = rememberCoroutineScope()

    val authManager = remember { HostedAuthManager(context) }
    DisposableEffect(Unit) { onDispose { authManager.dispose() } }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val data = result.data
        if (data == null) {
            loading = false
            error = null // user dismissed the tab
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            try {
                val hosted = authManager.exchange(data)
                // Persist the access token first so the bearer interceptor can
                // authenticate the /auth/me profile fetch.
                tokenManager.saveTokens(
                    AuthTokens(
                        accessToken = hosted.accessToken,
                        refreshToken = hosted.refreshToken ?: "",
                        expiresIn = hosted.expiresIn,
                        oauthSession = true,
                    ),
                )
                val user = runCatching { identityApi.getMyProfile() }.getOrNull()
                val role = user?.role ?: user?.roles?.firstOrNull() ?: "USER"
                val fullName = listOfNotNull(user?.firstName, user?.lastName)
                    .joinToString(" ").ifBlank { user?.email ?: "" }
                tokenManager.saveTokens(
                    AuthTokens(
                        accessToken = hosted.accessToken,
                        refreshToken = hosted.refreshToken ?: "",
                        expiresIn = hosted.expiresIn,
                        role = role,
                        userName = fullName,
                        userEmail = user?.email ?: "",
                        userId = user?.id ?: "",
                        tenantId = user?.tenantId ?: "",
                        oauthSession = true,
                    ),
                )
                loading = false
                onLoginSuccess(role)
            } catch (e: Throwable) {
                loading = false
                error = s(StringKey.LOGIN_ERROR_GENERIC)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Brand mark — gradient rounded square (matches hosted login).
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.PrimaryGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            // "SECURED BY FIVUCSAS" pill.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(AppColors.Primary.copy(alpha = 0.08f))
                    .border(1.dp, AppColors.Primary.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = s(StringKey.LOGIN_SECURED_BY),
                    color = AppColors.Primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = s(StringKey.LOGIN_TITLE),
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.OnBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = s(StringKey.LOGIN_SUBTITLE),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.OnSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            // Card with the gradient sign-in action.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.Surface)
                    .border(1.dp, AppColors.OnSurfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (loading) AppColors.OnSurfaceVariant.copy(alpha = 0.25f).let {
                                androidx.compose.ui.graphics.SolidColor(it)
                            } else AppColors.PrimaryGradient,
                        )
                        .clickable(enabled = !loading) {
                            error = null
                            loading = true
                            runCatching { launcher.launch(authManager.authorizeIntent()) }
                                .onFailure {
                                    loading = false
                                    error = s(StringKey.LOGIN_ERROR_OPEN)
                                }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp),
                        )
                    } else {
                        Text(
                            text = s(StringKey.LOGIN_BUTTON),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = s(StringKey.LOGIN_HOSTED_EXPLAINER),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Error,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "verify.fivucsas.com",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.OnSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
