package com.fivucsas.shared.presentation.state

data class VoiceUiState(
    val isRecording: Boolean = false,
    val isProcessing: Boolean = false,
    val recordingSeconds: Int = 0,
    val enrollSuccess: Boolean? = null,
    val verifyResult: VoiceVerifyUiResult? = null,
    val searchResult: VoiceSearchUiResult? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedMode: VoiceMode = VoiceMode.ENROLL
)

enum class VoiceMode { ENROLL, VERIFY, SEARCH }

data class VoiceVerifyUiResult(
    val verified: Boolean,
    val confidence: Float,
    val message: String
)

data class VoiceSearchUiResult(
    val found: Boolean,
    val userId: String?,
    val confidence: Float,
    val message: String
)
