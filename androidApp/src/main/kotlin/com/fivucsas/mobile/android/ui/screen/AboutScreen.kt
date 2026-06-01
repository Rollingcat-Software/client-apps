package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fivucsas.mobile.android.BuildConfig
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s(StringKey.NAV_ABOUT)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s(StringKey.A11Y_NAVIGATE_BACK))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.OnSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UIDimens.SpacingMedium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            Text(
                text = "FIVUCSAS",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
            Text(
                text = s(StringKey.ABOUT_VERSION, BuildConfig.VERSION_NAME),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = AppColors.OnSurfaceVariant
            )
            Text(
                text = s(StringKey.ABOUT_DESCRIPTION),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            Text(
                text = s(StringKey.ABOUT_DEVELOPED_AT),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = AppColors.OnSurfaceVariant
            )
        }
    }
}
