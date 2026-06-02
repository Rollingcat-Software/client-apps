package com.fivucsas.mobile.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fivucsas.mobile.android.data.nfc.AndroidNfcService
import com.fivucsas.mobile.android.ui.navigation.AppNavigation
import com.fivucsas.mobile.android.ui.theme.FIVUCSASTheme
import com.fivucsas.shared.platform.INfcService
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    // The Android override in FIVUCSASApplication binds INfcService to
    // AndroidNfcService; the dispatch/reader-mode/handleIntent helpers live on the
    // concrete impl (not the cross-platform interface), so we cast to use them.
    private val nfcService: INfcService by inject()
    private val androidNfcService: AndroidNfcService?
        get() = nfcService as? AndroidNfcService

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

        // The activity may have been launched by a tag tapped while it was closed.
        androidNfcService?.handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Reader mode is the preferred path for passport/eID (MRTD) reads — it
        // skips the platform NDEF probe that blocks encrypted identity documents.
        // The ReaderCallback routes through the same pipeline as foreground
        // dispatch, so MRZ/BAC handling is identical.
        androidNfcService?.enableReaderMode(this)
    }

    override fun onPause() {
        super.onPause()
        androidNfcService?.disableReaderMode(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Covers the foreground-dispatch path (TECH_DISCOVERED via the manifest
        // filter) for devices/situations where reader mode is not active.
        androidNfcService?.handleIntent(intent)
    }
}
