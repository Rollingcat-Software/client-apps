package com.fivucsas.shared.platform

import androidx.fragment.app.FragmentActivity

object AndroidBiometricActivityHolder {
    @Volatile
    var currentActivity: FragmentActivity? = null
}
