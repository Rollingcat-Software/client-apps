package com.fivucsas.desktop.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.fivucsas.desktop.data.DesktopCameraService
import com.fivucsas.desktop.ui.components.CameraPreview
import com.fivucsas.desktop.ui.components.DesktopAppShell
import com.fivucsas.desktop.ui.components.DesktopBannerType
import com.fivucsas.desktop.ui.components.DesktopInfoBanner
import com.fivucsas.shared.domain.model.ConfidenceBand
import com.fivucsas.shared.domain.model.GuestFaceCheckOutcome
import com.fivucsas.shared.domain.model.confidenceToBand
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricResult
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.fivucsas.shared.ui.screen.GuestFaceCheckResultScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun GuestFaceCheckScreen(
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: BiometricViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val cameraService = remember { DesktopCameraService() }

    var resultOutcome by remember { mutableStateOf<GuestFaceCheckOutcome?>(null) }
    var resultConfidence by remember { mutableStateOf<ConfidenceBand?>(null) }

    LaunchedEffect(state.isSuccess, state.result) {
        if (!state.isSuccess) return@LaunchedEffect
        val verification = (state.result as? BiometricResult.VerificationSuccess)?.result ?: return@LaunchedEffect
        resultOutcome = if (verification.isVerified) {
            GuestFaceCheckOutcome.FOUND
        } else {
            GuestFaceCheckOutcome.NOT_FOUND
        }
        resultConfidence = confidenceToBand(verification.confidence)
    }

    DesktopAppShell(
        title = "Guest Face Check",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(com.fivucsas.shared.config.UIDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(com.fivucsas.shared.config.UIDimens.SpacingMedium)
        ) {
            if (resultOutcome == null) {
                if (state.error != null) {
                    DesktopInfoBanner(
                        type = DesktopBannerType.Error,
                        text = "Could not reach face-check service. Please check backend/DB connection and try again.",
                    )
                }

                CameraPreview(
                    cameraService = cameraService,
                    onCapture = { imageBytes ->
                        scope.launch {
                            viewModel.verifyFace(imageBytes)
                        }
                    },
                    onClose = onBack
                )
            } else {
                GuestFaceCheckResultScreen(
                    outcome = resultOutcome ?: GuestFaceCheckOutcome.NOT_FOUND,
                    confidenceBand = resultConfidence,
                    onRetry = {
                        resultOutcome = null
                        resultConfidence = null
                        viewModel.clearState()
                    },
                    onLoginToContinue = onBackToLogin
                )
            }
        }
    }
}
