package com.fivucsas.shared.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Default network monitor that always reports online.
 * Used on platforms where native connectivity monitoring is not available
 * (e.g. desktop, iOS stub).
 */
class DefaultNetworkMonitor : INetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    override fun checkConnectivity(): Boolean = true
}
