package com.fivucsas.mobile.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.fivucsas.mobile.android.ui.navigation.AppNavigation
import com.fivucsas.mobile.android.ui.theme.FIVUCSASTheme
import com.fivucsas.shared.platform.AndroidBiometricActivityHolder

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FIVUCSASTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AndroidBiometricActivityHolder.setCurrentActivity(this)
    }

    override fun onPause() {
        AndroidBiometricActivityHolder.clearCurrentActivity(this)
        super.onPause()
    }
}
