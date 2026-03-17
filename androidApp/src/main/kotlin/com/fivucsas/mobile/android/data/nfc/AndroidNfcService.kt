package com.fivucsas.mobile.android.data.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import com.fivucsas.mobile.android.data.nfc.model.AuthenticationData
import com.fivucsas.mobile.android.data.nfc.model.CardData
import com.fivucsas.mobile.android.data.nfc.model.PassportData
import com.fivucsas.mobile.android.data.nfc.model.TurkishEidData
import com.fivucsas.mobile.android.data.nfc.model.IstanbulkartData
import com.fivucsas.mobile.android.data.nfc.model.MifareDesfireData
import com.fivucsas.mobile.android.data.nfc.model.MifareClassicData
import com.fivucsas.mobile.android.data.nfc.model.MifareUltralightData
import com.fivucsas.mobile.android.data.nfc.model.NdefData
import com.fivucsas.mobile.android.data.nfc.model.StudentCardData
import com.fivucsas.mobile.android.data.nfc.model.GenericCardData
import com.fivucsas.mobile.android.data.nfc.model.Iso15693Data
import com.fivucsas.shared.domain.model.MrzInputData
import com.fivucsas.shared.domain.model.NfcCardData
import com.fivucsas.shared.domain.model.NfcGenericCardData
import com.fivucsas.shared.domain.model.NfcIdentityDocumentData
import com.fivucsas.shared.domain.model.NfcReadResult
import com.fivucsas.shared.platform.INfcService
import com.fivucsas.shared.platform.NfcScanState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * Android implementation of INfcService using the UniversalNfcReader library.
 *
 * This service bridges the Android NFC stack with the shared platform abstraction,
 * converting Android-specific types (Tag, Bitmap) to platform-independent models.
 */
class AndroidNfcService(
    private val context: Context
) : INfcService {

    private val nfcManager = context.getSystemService(Context.NFC_SERVICE) as? NfcManager
    private val nfcAdapter = nfcManager?.defaultAdapter
    private val cardReadingService = NfcCardReadingService()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _scanState = MutableStateFlow<NfcScanState>(NfcScanState.Idle)
    private var currentMrzData: MrzInputData? = null

    override val isNfcAvailable: Boolean
        get() = nfcAdapter != null

    override val isNfcEnabled: Boolean
        get() = nfcAdapter?.isEnabled == true

    override val scanState: StateFlow<NfcScanState> = _scanState.asStateFlow()

    override fun startNfcScan() {
        if (!isNfcAvailable) {
            _scanState.value = NfcScanState.Completed(NfcReadResult.NfcNotAvailable)
            return
        }
        if (!isNfcEnabled) {
            _scanState.value = NfcScanState.Completed(NfcReadResult.NfcDisabled)
            return
        }
        _scanState.value = NfcScanState.WaitingForCard
    }

    override fun stopNfcScan() {
        _scanState.value = NfcScanState.Idle
    }

    override fun setMrzData(mrzData: MrzInputData) {
        currentMrzData = mrzData
    }

    override fun clearMrzData() {
        currentMrzData = null
    }

    /**
     * Enable NFC foreground dispatch on the given activity.
     * Call from Activity.onResume().
     */
    fun enableForegroundDispatch(activity: Activity) {
        val adapter = nfcAdapter ?: return
        val intent = Intent(activity, activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            activity, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        )
        val techLists = arrayOf(
            arrayOf("android.nfc.tech.IsoDep"),
            arrayOf("android.nfc.tech.MifareClassic"),
            arrayOf("android.nfc.tech.MifareUltralight"),
            arrayOf("android.nfc.tech.Ndef"),
            arrayOf("android.nfc.tech.NfcA"),
            arrayOf("android.nfc.tech.NfcB"),
            arrayOf("android.nfc.tech.NfcV")
        )
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists)
    }

    /**
     * Disable NFC foreground dispatch.
     * Call from Activity.onPause().
     */
    fun disableForegroundDispatch(activity: Activity) {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    /**
     * Handle an NFC intent (tag discovered).
     * Call from Activity.onNewIntent().
     */
    fun handleIntent(intent: Intent) {
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return

        if (_scanState.value !is NfcScanState.WaitingForCard) return

        _scanState.value = NfcScanState.Reading()

        scope.launch {
            try {
                val mrzData = currentMrzData
                val readResult = if (mrzData != null && mrzData.isValid()) {
                    // Read with MRZ authentication
                    val authData = AuthenticationData.MrzData(
                        documentNumber = mrzData.documentNumber,
                        dateOfBirth = mrzData.dateOfBirth,
                        dateOfExpiry = mrzData.dateOfExpiry
                    )
                    cardReadingService.readCardWithAuth(tag, authData)
                } else {
                    // Read without authentication
                    cardReadingService.readCard(tag)
                }

                val nfcResult = when (readResult) {
                    is CardReadResult.Success -> {
                        NfcReadResult.Success(convertCardData(readResult.cardData))
                    }
                    is CardReadResult.AuthenticationRequired -> {
                        NfcReadResult.AuthenticationRequired(
                            cardTypeName = readResult.cardType.displayName,
                            message = "This card requires MRZ data to read. Enter document number, date of birth, and date of expiry."
                        )
                    }
                    is CardReadResult.Failure -> {
                        NfcReadResult.Failure(
                            errorMessage = readResult.error.message,
                            isRecoverable = readResult.error.isRecoverable
                        )
                    }
                    is CardReadResult.UnsupportedCard -> {
                        NfcReadResult.Failure(
                            errorMessage = "Unsupported card type: ${readResult.cardType.displayName}",
                            isRecoverable = false
                        )
                    }
                    is CardReadResult.Exception -> {
                        NfcReadResult.Failure(
                            errorMessage = readResult.exception.message ?: "Unknown error",
                            isRecoverable = true
                        )
                    }
                }

                _scanState.value = NfcScanState.Completed(nfcResult)
            } catch (e: Exception) {
                _scanState.value = NfcScanState.Error(
                    message = e.message ?: "NFC read failed",
                    isRecoverable = true
                )
            }
        }
    }

    /**
     * Convert Android-specific CardData to platform-independent NfcCardData.
     */
    private fun convertCardData(cardData: CardData): NfcCardData {
        return when (cardData) {
            is PassportData -> NfcIdentityDocumentData(
                uid = cardData.uid,
                cardTypeName = cardData.cardType.displayName,
                technologies = cardData.technologies,
                readTimestamp = cardData.readTimestamp,
                documentType = cardData.documentType,
                issuingCountry = cardData.issuingCountry,
                documentNumber = cardData.documentNumber,
                surname = cardData.surname,
                givenNames = cardData.givenNames,
                nationality = cardData.nationality,
                dateOfBirth = cardData.dateOfBirth,
                sex = cardData.sex,
                dateOfExpiry = cardData.dateOfExpiry,
                personalNumber = cardData.personalNumber,
                photoBytes = cardData.photo?.toJpegBytes(),
                bacSuccessful = cardData.bacSuccessful,
                sodValid = cardData.sodValid,
                dg1HashValid = cardData.dg1HashValid,
                dg2HashValid = cardData.dg2HashValid
            )
            is TurkishEidData -> NfcIdentityDocumentData(
                uid = cardData.uid,
                cardTypeName = cardData.cardType.displayName,
                technologies = cardData.technologies,
                readTimestamp = cardData.readTimestamp,
                documentNumber = cardData.documentNumber,
                surname = cardData.surname,
                givenNames = cardData.givenNames,
                nationality = cardData.nationality,
                dateOfBirth = cardData.dateOfBirth,
                sex = cardData.sex,
                dateOfExpiry = cardData.dateOfExpiry,
                personalNumber = cardData.personalNumber,
                photoBytes = cardData.photo?.toJpegBytes(),
                bacSuccessful = cardData.bacSuccessful,
                sodValid = cardData.sodValid
            )
            is IstanbulkartData -> NfcGenericCardData(
                uid = cardData.uid,
                cardTypeName = cardData.cardType.displayName,
                technologies = cardData.technologies,
                readTimestamp = cardData.readTimestamp,
                details = buildMap {
                    cardData.desfireVersion?.let {
                        put("Hardware Version", "${it.hardwareMajorVersion}.${it.hardwareMinorVersion}")
                        put("Storage", "${it.storageSizeBytes} bytes")
                    }
                    if (cardData.applicationIds.isNotEmpty()) {
                        put("Applications", cardData.applicationIds.joinToString(", "))
                    }
                    cardData.freeMemory?.let { put("Free Memory", "$it bytes") }
                }
            )
            is MifareDesfireData -> NfcGenericCardData(
                uid = cardData.uid,
                cardTypeName = cardData.cardType.displayName,
                technologies = cardData.technologies,
                readTimestamp = cardData.readTimestamp,
                details = buildMap {
                    cardData.cardSize?.let { put("Card Size", "$it bytes") }
                    if (cardData.applicationIds.isNotEmpty()) {
                        put("Applications", cardData.applicationIds.joinToString(", "))
                    }
                    cardData.freeMemory?.let { put("Free Memory", "$it bytes") }
                }
            )
            is MifareClassicData -> NfcGenericCardData(
                uid = cardData.uid,
                cardTypeName = cardData.cardType.displayName,
                technologies = cardData.technologies,
                readTimestamp = cardData.readTimestamp,
                details = mapOf(
                    "Size" to "${cardData.size} bytes",
                    "Sectors" to "${cardData.accessibleSectors}/${cardData.sectorCount}",
                    "Blocks" to "${cardData.blockCount}"
                )
            )
            is MifareUltralightData -> NfcGenericCardData(
                uid = cardData.uid,
                cardTypeName = cardData.cardType.displayName,
                technologies = cardData.technologies,
                readTimestamp = cardData.readTimestamp,
                details = buildMap {
                    put("Type", cardData.ultralightType.name)
                    put("Pages", "${cardData.pageCount}")
                    cardData.ndefMessage?.let { put("NDEF", it) }
                }
            )
            is NdefData -> NfcGenericCardData(
                uid = cardData.uid,
                cardTypeName = cardData.cardType.displayName,
                technologies = cardData.technologies,
                readTimestamp = cardData.readTimestamp,
                details = buildMap {
                    put("Records", "${cardData.records.size}")
                    put("Writable", if (cardData.isWritable) "Yes" else "No")
                    put("Size", "${cardData.usedSize}/${cardData.maxSize} bytes")
                    cardData.records.forEachIndexed { i, record ->
                        record.payloadAsString?.let { put("Record $i", it) }
                    }
                }
            )
            is StudentCardData -> NfcGenericCardData(
                uid = cardData.uid,
                cardTypeName = cardData.cardType.displayName,
                technologies = cardData.technologies,
                readTimestamp = cardData.readTimestamp,
                details = buildMap {
                    cardData.studentId?.let { put("Student ID", it) }
                    cardData.studentName?.let { put("Name", it) }
                    cardData.universityName?.let { put("University", it) }
                    put("Sectors", "${cardData.sectorsRead}/${cardData.totalSectors}")
                }
            )
            else -> NfcGenericCardData(
                uid = cardData.uid,
                cardTypeName = cardData.cardType.displayName,
                technologies = cardData.technologies,
                readTimestamp = cardData.readTimestamp
            )
        }
    }

    /**
     * Convert Bitmap to JPEG byte array.
     */
    private fun Bitmap.toJpegBytes(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }
}
