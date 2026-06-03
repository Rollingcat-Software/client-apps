package com.fivucsas.mobile.android.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.domain.model.MrzInputData
import com.fivucsas.shared.domain.model.NfcGenericCardData
import com.fivucsas.shared.domain.model.NfcIdentityDocumentData
import com.fivucsas.shared.domain.model.NfcReadResult
import com.fivucsas.shared.domain.usecase.nfc.EnrollNfcCardUseCase
import com.fivucsas.shared.domain.usecase.nfc.VerifyNfcAuthenticityUseCase
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.platform.INfcService
import com.fivucsas.shared.platform.NfcScanState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcReadScreen(
    onNavigateBack: () -> Unit
) {
    val nfcService = koinInject<INfcService>()
    val enrollUseCase = koinInject<EnrollNfcCardUseCase>()
    val authenticityUseCase = koinInject<VerifyNfcAuthenticityUseCase>()
    val scanState by nfcService.scanState.collectAsState()
    val scope = rememberCoroutineScope()

    var documentNumber by rememberSaveable { mutableStateOf("") }
    var dateOfBirth by rememberSaveable { mutableStateOf("") }
    var dateOfExpiry by rememberSaveable { mutableStateOf("") }
    var showMrzInput by rememberSaveable { mutableStateOf(true) }
    var showMrzScanner by remember { mutableStateOf(false) }

    // "Register this card" state for the result screen. Reset on each new scan.
    var enrollState by remember { mutableStateOf<EnrollUiState>(EnrollUiState.Idle) }

    // Passive-authentication (server-authoritative) verdict state. Reset per scan.
    var authState by remember { mutableStateOf<AuthenticityUiState>(AuthenticityUiState.Idle) }

    val onRegisterCard: (uid: String, cardType: String?) -> Unit = { uid, cardType ->
        enrollState = EnrollUiState.InProgress
        scope.launch {
            enrollUseCase(cardSerial = uid, cardType = cardType).fold(
                onSuccess = { enrollState = EnrollUiState.Success },
                onFailure = { enrollState = EnrollUiState.Error }
            )
        }
    }

    val onVerifyAuthenticity: (doc: NfcIdentityDocumentData) -> Unit = { doc ->
        authState = AuthenticityUiState.InProgress
        scope.launch {
            authenticityUseCase(sod = doc.sodBytes, dg1 = doc.dg1Bytes, dg2 = doc.dg2Bytes).fold(
                onSuccess = { verdict ->
                    authState = if (verdict.authentic) {
                        AuthenticityUiState.Authentic
                    } else {
                        AuthenticityUiState.NotAuthentic(verdict.reasonCode)
                    }
                },
                onFailure = { authState = AuthenticityUiState.NotAuthentic(reasonCode = null) }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { nfcService.stopNfcScan() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = s(StringKey.NFCREAD_TITLE),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s(StringKey.A11Y_NAVIGATE_BACK))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = scanState) {
                is NfcScanState.Idle -> {
                    if (showMrzInput) {
                        MrzInputSection(
                            documentNumber = documentNumber,
                            dateOfBirth = dateOfBirth,
                            dateOfExpiry = dateOfExpiry,
                            onDocumentNumberChange = { documentNumber = it },
                            onDateOfBirthChange = { dateOfBirth = it },
                            onDateOfExpiryChange = { dateOfExpiry = it },
                            onScanMrzWithCamera = { showMrzScanner = true },
                            onStartScan = {
                                val mrz = MrzInputData(documentNumber, dateOfBirth, dateOfExpiry)
                                if (mrz.isValid()) {
                                    nfcService.setMrzData(mrz)
                                }
                                nfcService.startNfcScan()
                                showMrzInput = false
                            },
                            onScanWithoutMrz = {
                                nfcService.clearMrzData()
                                nfcService.startNfcScan()
                                showMrzInput = false
                            }
                        )
                    } else {
                        // Reset to input
                        showMrzInput = true
                    }

                    // Camera MRZ scanner (ML Kit OCR). On a valid read it fills the
                    // document-number / DOB / expiry fields above so the "Scan with
                    // MRZ" button enables itself — previously this affordance only
                    // existed on the orphaned NfcStepScreen, so the button looked
                    // permanently disabled.
                    if (showMrzScanner) {
                        MrzScannerScreen(
                            onMrzScanned = { data ->
                                documentNumber = data.documentNumber
                                dateOfBirth = data.dateOfBirth
                                dateOfExpiry = data.dateOfExpiry
                                showMrzScanner = false
                            },
                            onDismiss = { showMrzScanner = false }
                        )
                    }
                }

                is NfcScanState.WaitingForCard -> {
                    WaitingForCardSection(
                        onCancel = {
                            nfcService.stopNfcScan()
                            showMrzInput = true
                        }
                    )
                }

                is NfcScanState.Reading -> {
                    ReadingSection(cardTypeName = state.cardTypeName)
                }

                is NfcScanState.Completed -> {
                    ResultSection(
                        result = state.result,
                        enrollState = enrollState,
                        onRegisterCard = onRegisterCard,
                        authState = authState,
                        onVerifyAuthenticity = onVerifyAuthenticity,
                        onScanAgain = {
                            nfcService.stopNfcScan()
                            enrollState = EnrollUiState.Idle
                            authState = AuthenticityUiState.Idle
                            showMrzInput = true
                        }
                    )
                }

                is NfcScanState.Error -> {
                    ErrorSection(
                        message = state.message,
                        isRecoverable = state.isRecoverable,
                        onRetry = {
                            nfcService.startNfcScan()
                        },
                        onBack = {
                            nfcService.stopNfcScan()
                            showMrzInput = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MrzInputSection(
    documentNumber: String,
    dateOfBirth: String,
    dateOfExpiry: String,
    onDocumentNumberChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onDateOfExpiryChange: (String) -> Unit,
    onScanMrzWithCamera: () -> Unit,
    onStartScan: () -> Unit,
    onScanWithoutMrz: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ContactPage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        s(StringKey.NFCREAD_DOC_READER_HEADER),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        s(StringKey.NFCREAD_DOC_READER_SUBTITLE),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Primary path: scan the MRZ with the camera (ML Kit OCR) — auto-fills the
    // fields below. Manual entry remains as a fallback.
    FilledTonalButton(
        onClick = onScanMrzWithCamera,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.DocumentScanner, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(s(StringKey.NFCREAD_SCAN_MRZ_CAMERA), fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = documentNumber,
        onValueChange = { if (it.length <= 9) onDocumentNumberChange(it.uppercase()) },
        label = { Text(s(StringKey.NFCREAD_FIELD_DOCUMENT_NUMBER)) },
        placeholder = { Text(s(StringKey.NFCREAD_FIELD_DOCUMENT_NUMBER_HINT)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onDateOfBirthChange(it) },
            label = { Text(s(StringKey.NFCREAD_FIELD_DATE_OF_BIRTH)) },
            placeholder = { Text(s(StringKey.NFCREAD_DATE_HINT_YYMMDD)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = dateOfExpiry,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onDateOfExpiryChange(it) },
            label = { Text(s(StringKey.NFCREAD_FIELD_DATE_OF_EXPIRY)) },
            placeholder = { Text(s(StringKey.NFCREAD_DATE_HINT_YYMMDD)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onStartScan,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = documentNumber.isNotBlank() && dateOfBirth.length == 6 && dateOfExpiry.length == 6
    ) {
        Icon(Icons.Default.Nfc, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(s(StringKey.NFCREAD_SCAN_WITH_MRZ), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = onScanWithoutMrz,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.CreditCard, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(s(StringKey.NFCREAD_SCAN_ANY_CARD))
    }
}

@Composable
private fun WaitingForCardSection(onCancel: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Nfc,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            s(StringKey.NFCREAD_READY_TITLE),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            s(StringKey.NFCREAD_READY_SUBTITLE),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onCancel) {
            Text(s(StringKey.CANCEL))
        }
    }
}

@Composable
private fun ReadingSection(cardTypeName: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            s(StringKey.NFCREAD_READING_TITLE),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            s(StringKey.NFCREAD_READING_SUBTITLE),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (cardTypeName != "Unknown") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                s(StringKey.NFCREAD_DETECTED_PREFIX, cardTypeName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ResultSection(
    result: NfcReadResult,
    enrollState: EnrollUiState,
    onRegisterCard: (uid: String, cardType: String?) -> Unit,
    authState: AuthenticityUiState,
    onVerifyAuthenticity: (doc: NfcIdentityDocumentData) -> Unit,
    onScanAgain: () -> Unit
) {
    when (result) {
        is NfcReadResult.Success -> {
            when (val data = result.cardData) {
                is NfcIdentityDocumentData -> IdentityDocumentResult(
                    data = data,
                    enrollState = enrollState,
                    onRegisterCard = onRegisterCard,
                    authState = authState,
                    onVerifyAuthenticity = onVerifyAuthenticity,
                    onScanAgain = onScanAgain
                )
                is NfcGenericCardData -> GenericCardResult(
                    data = data,
                    enrollState = enrollState,
                    onRegisterCard = onRegisterCard,
                    onScanAgain = onScanAgain
                )
            }
        }
        is NfcReadResult.AuthenticationRequired -> {
            ErrorSection(
                message = result.message,
                isRecoverable = true,
                onRetry = onScanAgain,
                onBack = onScanAgain
            )
        }
        is NfcReadResult.Failure -> {
            ErrorSection(
                message = result.errorMessage,
                isRecoverable = result.isRecoverable,
                onRetry = onScanAgain,
                onBack = onScanAgain
            )
        }
        is NfcReadResult.NfcNotAvailable -> {
            ErrorSection(
                message = s(StringKey.NFCREAD_ERROR_NOT_AVAILABLE),
                isRecoverable = false,
                onRetry = {},
                onBack = onScanAgain
            )
        }
        is NfcReadResult.NfcDisabled -> {
            ErrorSection(
                message = s(StringKey.NFCREAD_ERROR_DISABLED),
                isRecoverable = true,
                onRetry = onScanAgain,
                onBack = onScanAgain
            )
        }
    }
}

@Composable
private fun IdentityDocumentResult(
    data: NfcIdentityDocumentData,
    enrollState: EnrollUiState,
    onRegisterCard: (uid: String, cardType: String?) -> Unit,
    authState: AuthenticityUiState,
    onVerifyAuthenticity: (doc: NfcIdentityDocumentData) -> Unit,
    onScanAgain: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                data.cardTypeName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (data.bacSuccessful) {
                Text(
                    s(StringKey.NFCREAD_BAC_SUCCESSFUL),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Photo
    data.photoBytes?.let { bytes ->
        val bitmap = remember(bytes) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = s(StringKey.NFCREAD_DOCUMENT_PHOTO_DESC),
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Personal data
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(s(StringKey.NFCREAD_PERSONAL_INFO), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (data.fullName.isNotBlank()) DataRow(s(StringKey.NFCREAD_LABEL_NAME), data.fullName)
            if (data.documentNumber.isNotBlank()) DataRow(s(StringKey.NFCREAD_LABEL_DOCUMENT_NO), data.documentNumber)
            if (data.nationality.isNotBlank()) DataRow(s(StringKey.NFCREAD_LABEL_NATIONALITY), data.nationality)
            if (data.dateOfBirth.isNotBlank()) DataRow(s(StringKey.NFCREAD_LABEL_DATE_OF_BIRTH), data.dateOfBirth)
            if (data.sex.isNotBlank()) DataRow(s(StringKey.NFCREAD_LABEL_SEX), data.sex)
            if (data.dateOfExpiry.isNotBlank()) DataRow(s(StringKey.NFCREAD_LABEL_EXPIRY_DATE), data.dateOfExpiry)
            if (data.personalNumber.isNotBlank()) DataRow(s(StringKey.NFCREAD_LABEL_PERSONAL_NO), data.personalNumber)
            if (data.issuingCountry.isNotBlank()) DataRow(s(StringKey.NFCREAD_LABEL_ISSUING_COUNTRY), data.issuingCountry)
        }
    }

    // Security validation
    if (data.sodValid != null || data.dg1HashValid != null || data.dg2HashValid != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(s(StringKey.NFCREAD_SECURITY_VALIDATION), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                data.sodValid?.let { DataRow(s(StringKey.NFCREAD_LABEL_SOD_SIGNATURE), if (it) s(StringKey.NFCREAD_VALID) else s(StringKey.NFCREAD_INVALID)) }
                data.dg1HashValid?.let { DataRow(s(StringKey.NFCREAD_LABEL_MRZ_HASH), if (it) s(StringKey.NFCREAD_VALID) else s(StringKey.NFCREAD_INVALID)) }
                data.dg2HashValid?.let { DataRow(s(StringKey.NFCREAD_LABEL_PHOTO_HASH), if (it) s(StringKey.NFCREAD_VALID) else s(StringKey.NFCREAD_INVALID)) }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    DataRow(s(StringKey.NFCREAD_LABEL_UID), data.uid)
    DataRow(s(StringKey.NFCREAD_LABEL_TECHNOLOGIES), data.technologies.joinToString(", "))

    // Passive authentication (server-authoritative) — only offered when the
    // chip yielded an EF.SOD to verify.
    if (data.sodBytes != null) {
        Spacer(modifier = Modifier.height(24.dp))
        VerifyAuthenticitySection(
            authState = authState,
            onVerify = { onVerifyAuthenticity(data) }
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
    RegisterCardSection(
        enrollState = enrollState,
        onRegisterCard = { onRegisterCard(data.uid, data.cardTypeName) }
    )

    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = onScanAgain,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(s(StringKey.NFCREAD_SCAN_ANOTHER_CARD))
    }
}

@Composable
private fun GenericCardResult(
    data: NfcGenericCardData,
    enrollState: EnrollUiState,
    onRegisterCard: (uid: String, cardType: String?) -> Unit,
    onScanAgain: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CreditCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                data.cardTypeName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            DataRow(s(StringKey.NFCREAD_LABEL_UID), data.uid)
            DataRow(s(StringKey.NFCREAD_LABEL_TECHNOLOGIES), data.technologies.joinToString(", "))
            data.details.forEach { (key, value) ->
                DataRow(key, value)
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    RegisterCardSection(
        enrollState = enrollState,
        onRegisterCard = { onRegisterCard(data.uid, data.cardTypeName) }
    )

    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = onScanAgain,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(s(StringKey.NFCREAD_SCAN_ANOTHER_CARD))
    }
}

/** Local UI state for the "Register this card" enrollment action. */
private sealed interface EnrollUiState {
    data object Idle : EnrollUiState
    data object InProgress : EnrollUiState
    data object Success : EnrollUiState
    data object Error : EnrollUiState
}

/** Local UI state for the passive-authentication (server) verdict. */
private sealed interface AuthenticityUiState {
    data object Idle : AuthenticityUiState
    data object InProgress : AuthenticityUiState
    data object Authentic : AuthenticityUiState
    data class NotAuthentic(val reasonCode: String?) : AuthenticityUiState
}

/**
 * "Verify authenticity" affordance — submits the chip's EF.SOD + DGs to the
 * server for the authoritative, fail-closed passive-auth verdict
 * (POST /nfc/verify-authenticity). The client-side check is advisory; this is
 * the trustworthy result. NOTE: the server returns reasonCode=NO_TRUST_STORE
 * until the operator loads ICAO-PKD CSCA roots into the bio container.
 */
@Composable
private fun VerifyAuthenticitySection(
    authState: AuthenticityUiState,
    onVerify: () -> Unit
) {
    when (authState) {
        is AuthenticityUiState.Authentic -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    s(StringKey.NFC_AUTHENTICITY_AUTHENTIC),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        is AuthenticityUiState.NotAuthentic -> {
            // Known "the operator hasn't loaded the CSCA trust store yet" case —
            // the server returns 422 with reasonCode=NO_TRUST_STORE (and errorCode
            // NFC_PA_NOT_AUTHENTIC). This is NOT a forged chip; it just means the
            // issuer's certificate isn't configured. Show a calm, non-scary message
            // instead of a red "could not be confirmed" + a raw reason code. We do
            // NOT claim the chip is authentic — we only stop alarming the user.
            val isTrustStoreUnavailable = authState.reasonCode == "NO_TRUST_STORE" ||
                authState.reasonCode == "NFC_PA_NOT_AUTHENTIC"
            if (isTrustStoreUnavailable) {
                Text(
                    s(StringKey.NFC_AUTHENTICITY_PA_UNAVAILABLE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            s(StringKey.NFC_AUTHENTICITY_NOT_AUTHENTIC),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    authState.reasonCode?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        else -> {
            OutlinedButton(
                onClick = onVerify,
                enabled = authState != AuthenticityUiState.InProgress,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (authState == AuthenticityUiState.InProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(s(StringKey.NFC_AUTHENTICITY_IN_PROGRESS))
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(s(StringKey.NFC_AUTHENTICITY_VERIFY_BUTTON))
                }
            }
        }
    }
}

/**
 * "Register this card" affordance shown under a successful NFC read. POSTs
 * the (canonical UPPERHEX) serial to /api/v1/nfc/enroll via the use case.
 * Once registered, the button is replaced by a success line.
 */
@Composable
private fun RegisterCardSection(
    enrollState: EnrollUiState,
    onRegisterCard: () -> Unit
) {
    when (enrollState) {
        EnrollUiState.Success -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    s(StringKey.NFC_REGISTER_CARD_SUCCESS),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        else -> {
            Button(
                onClick = onRegisterCard,
                enabled = enrollState != EnrollUiState.InProgress,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (enrollState == EnrollUiState.InProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(s(StringKey.NFC_REGISTER_CARD_IN_PROGRESS))
                } else {
                    Icon(Icons.Default.CreditCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(s(StringKey.NFC_REGISTER_CARD_BUTTON))
                }
            }
            if (enrollState == EnrollUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    s(StringKey.NFC_REGISTER_CARD_ERROR),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ErrorSection(
    message: String,
    isRecoverable: Boolean,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isRecoverable) {
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(s(StringKey.COMMON_TRY_AGAIN))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(s(StringKey.BACK))
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}
