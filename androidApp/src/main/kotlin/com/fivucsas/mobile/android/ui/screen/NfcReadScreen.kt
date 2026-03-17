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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.fivucsas.shared.platform.INfcService
import com.fivucsas.shared.platform.NfcScanState
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcReadScreen(
    onNavigateBack: () -> Unit
) {
    val nfcService = koinInject<INfcService>()
    val scanState by nfcService.scanState.collectAsState()

    var documentNumber by rememberSaveable { mutableStateOf("") }
    var dateOfBirth by rememberSaveable { mutableStateOf("") }
    var dateOfExpiry by rememberSaveable { mutableStateOf("") }
    var showMrzInput by rememberSaveable { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose { nfcService.stopNfcScan() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NFC Card Reader",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        onScanAgain = {
                            nfcService.stopNfcScan()
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
                        "Identity Document Reader",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Enter MRZ data to read passport or eID chip",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = documentNumber,
        onValueChange = { if (it.length <= 9) onDocumentNumberChange(it.uppercase()) },
        label = { Text("Document Number") },
        placeholder = { Text("e.g. A12345678") },
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
            label = { Text("Date of Birth") },
            placeholder = { Text("YYMMDD") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = dateOfExpiry,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onDateOfExpiryChange(it) },
            label = { Text("Date of Expiry") },
            placeholder = { Text("YYMMDD") },
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
        Text("Scan with MRZ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
        Text("Scan Any NFC Card")
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
            "Ready to Scan",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Hold your card against the back of your phone",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onCancel) {
            Text("Cancel")
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
            "Reading Card...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Keep the card steady. Do not move it.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (cardTypeName != "Unknown") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Detected: $cardTypeName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ResultSection(
    result: NfcReadResult,
    onScanAgain: () -> Unit
) {
    when (result) {
        is NfcReadResult.Success -> {
            when (val data = result.cardData) {
                is NfcIdentityDocumentData -> IdentityDocumentResult(data, onScanAgain)
                is NfcGenericCardData -> GenericCardResult(data, onScanAgain)
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
                message = "NFC is not available on this device.",
                isRecoverable = false,
                onRetry = {},
                onBack = onScanAgain
            )
        }
        is NfcReadResult.NfcDisabled -> {
            ErrorSection(
                message = "NFC is disabled. Please enable it in your device settings.",
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
                    "BAC Authentication Successful",
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
                contentDescription = "Document photo",
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
            Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (data.fullName.isNotBlank()) DataRow("Name", data.fullName)
            if (data.documentNumber.isNotBlank()) DataRow("Document No", data.documentNumber)
            if (data.nationality.isNotBlank()) DataRow("Nationality", data.nationality)
            if (data.dateOfBirth.isNotBlank()) DataRow("Date of Birth", data.dateOfBirth)
            if (data.sex.isNotBlank()) DataRow("Sex", data.sex)
            if (data.dateOfExpiry.isNotBlank()) DataRow("Expiry Date", data.dateOfExpiry)
            if (data.personalNumber.isNotBlank()) DataRow("Personal No", data.personalNumber)
            if (data.issuingCountry.isNotBlank()) DataRow("Issuing Country", data.issuingCountry)
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
                Text("Security Validation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                data.sodValid?.let { DataRow("SOD Signature", if (it) "Valid" else "Invalid") }
                data.dg1HashValid?.let { DataRow("MRZ Hash", if (it) "Valid" else "Invalid") }
                data.dg2HashValid?.let { DataRow("Photo Hash", if (it) "Valid" else "Invalid") }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    DataRow("UID", data.uid)
    DataRow("Technologies", data.technologies.joinToString(", "))

    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onScanAgain,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Scan Another Card")
    }
}

@Composable
private fun GenericCardResult(
    data: NfcGenericCardData,
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
            DataRow("UID", data.uid)
            DataRow("Technologies", data.technologies.joinToString(", "))
            data.details.forEach { (key, value) ->
                DataRow(key, value)
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onScanAgain,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Scan Another Card")
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
                Text("Try Again")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
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
