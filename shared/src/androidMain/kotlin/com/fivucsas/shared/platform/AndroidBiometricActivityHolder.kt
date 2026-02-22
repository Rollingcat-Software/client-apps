package com.fivucsas.shared.platform

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

object AndroidBiometricActivityHolder {
    @Volatile
    private var currentActivityRef: WeakReference<FragmentActivity>? = null

    fun setCurrentActivity(activity: FragmentActivity) {
        currentActivityRef = WeakReference(activity)
    }

    fun clearCurrentActivity(activity: FragmentActivity) {
        if (currentActivityRef?.get() === activity) {
            currentActivityRef?.clear()
            currentActivityRef = null
        }
    }

    fun getCurrentActivity(): FragmentActivity? = currentActivityRef?.get()
}
