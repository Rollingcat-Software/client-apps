package com.fivucsas.shared.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.fivucsas.shared.presentation.viewmodel.BaseViewModel

/**
 * Binds a [BaseViewModel]'s lifetime to the calling composition.
 *
 * When the composable that injected the ViewModel leaves composition (the user
 * navigates away), [BaseViewModel.dispose] is invoked, cancelling its scope.
 * Apply at every injection site:
 * ```
 * val viewModel = koinInject<XxxViewModel>().disposeOnLeave()
 * ```
 *
 * Keyed on the instance, so it is stable across recompositions and only disposes
 * on a genuine exit (Koin `factory` returns the same instance for the life of the
 * composition via `koinInject`'s internal `remember`).
 */
@Composable
fun <T : BaseViewModel> T.disposeOnLeave(): T {
    val vm = this
    DisposableEffect(vm) {
        onDispose { vm.dispose() }
    }
    return vm
}
