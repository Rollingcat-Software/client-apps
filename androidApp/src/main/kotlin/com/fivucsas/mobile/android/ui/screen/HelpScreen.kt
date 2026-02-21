package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.molecules.ExpandableCard
import com.fivucsas.shared.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & FAQ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            ExpandableCard(
                title = "Why do we need camera access?",
                subtitle = "Permissions"
            ) {
                Text("We use the camera to securely capture your face for verification.")
            }
            ExpandableCard(
                title = "How long does verification take?",
                subtitle = "Timing"
            ) {
                Text("Most verifications complete in under one minute.")
            }
            ExpandableCard(
                title = "What if verification fails?",
                subtitle = "Recovery"
            ) {
                Text("Try again in better lighting or remove any obstructions like glasses.")
            }
        }
    }
}
