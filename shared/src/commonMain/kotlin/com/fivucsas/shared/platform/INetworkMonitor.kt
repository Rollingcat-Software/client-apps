package com.fivucsas.shared.platform

import kotlinx.coroutines.flow.StateFlow

/**
 * Network connectivity monitor interface.
 *
 * Platform implementations use native APIs to observe connectivity:
 * - Android: ConnectivityManager
 * - Desktop: java.net.InetAddress reachability check
 */
interface INetworkMonitor {
    /**
     * Current connectivity state as a flow.
     * True when the device has network connectivity, false otherwise.
     */
    val isOnline: StateFlow<Boolean>

    /**
     * One-shot connectivity check.
     */
    fun checkConnectivity(): Boolean
}
