package com.fivucsas.mobile.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fivucsas.mobile.android.ui.navigation.AppNavigation
import com.fivucsas.mobile.android.ui.theme.FIVUCSASTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize dependencies (simple DI for MVP)
        val appDependencies = AppDependencies(applicationContext)

        setContent {
            FIVUCSASTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(appDependencies)
                }
            }
        }
    }
}
