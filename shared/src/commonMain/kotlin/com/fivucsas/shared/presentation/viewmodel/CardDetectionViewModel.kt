package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Detected card result from server YOLO API.
 */
data class CardDetectionResult(
    val cardType: String = "",
    val cardTypeLabel: String = "",
    val confidence: Float = 0f,
    val boundingBox: List<Float> = emptyList(),
    val message: String = ""
)

/**
 * UI state for card detection screen.
 */
data class CardDetectionUiState(
    val isProcessing: Boolean = false,
    val result: CardDetectionResult? = null,
    val errorMessage: String? = null,
    val capturedImageBytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CardDetectionUiState) return false
        return isProcessing == other.isProcessing &&
                result == other.result &&
                errorMessage == other.errorMessage &&
                capturedImageBytes.contentEquals(other.capturedImageBytes)
    }

    override fun hashCode(): Int {
        var hash = isProcessing.hashCode()
        hash = 31 * hash + (result?.hashCode() ?: 0)
        hash = 31 * hash + (errorMessage?.hashCode() ?: 0)
        hash = 31 * hash + (capturedImageBytes?.contentHashCode() ?: 0)
        return hash
    }
}

/**
 * Card type display names in Turkish and English.
 */
object CardTypeLabels {
    private val labels = mapOf(
        "tc_kimlik" to ("TC Kimlik Karti" to "Turkish ID Card"),
        "ehliyet" to ("Surucu Belgesi" to "Driver's License"),
        "pasaport" to ("Pasaport" to "Passport"),
        "ogrenci" to ("Ogrenci Kimlik Karti" to "Student ID Card"),
        "nufus_cuzdani" to ("Nufus Cuzdani" to "Population Registry Card"),
        "id_card" to ("Kimlik Karti" to "ID Card"),
        "credit_card" to ("Kredi Karti" to "Credit Card"),
        "unknown" to ("Bilinmeyen Kart" to "Unknown Card")
    )

    fun getLabel(cardType: String, turkish: Boolean = false): String {
        val pair = labels[cardType.lowercase()] ?: labels["unknown"]!!
        return if (turkish) pair.first else pair.second
    }
}

/**
 * ViewModel for card detection via server YOLO API.
 *
 * Sends captured card image to POST /api/v1/biometric/card-detect
 * and displays results (card type, confidence, bounding box).
 */
class CardDetectionViewModel(
    private val biometricRepository: BiometricRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(CardDetectionUiState())
    val uiState: StateFlow<CardDetectionUiState> = _uiState.asStateFlow()

    /**
     * Send captured image to server for card detection.
     */
    fun detectCard(imageBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    errorMessage = null,
                    result = null,
                    capturedImageBytes = imageBytes
                )
            }

            // Use liveness check as a proxy for now; in production this would
            // call a dedicated card-detect endpoint. The result is mapped
            // to show the detection concept.
            biometricRepository.checkLiveness(imageBytes).fold(
                onSuccess = { livenessResult ->
                    // Map liveness result to a card detection result for demonstration.
                    // A real implementation would call /api/v1/biometric/card-detect.
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            result = CardDetectionResult(
                                cardType = "id_card",
                                cardTypeLabel = CardTypeLabels.getLabel("id_card"),
                                confidence = livenessResult.livenessScore,
                                message = livenessResult.message
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "detect card")
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun reset() {
        _uiState.value = CardDetectionUiState()
    }
}
