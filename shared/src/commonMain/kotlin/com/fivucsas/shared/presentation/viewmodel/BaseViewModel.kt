package com.fivucsas.shared.presentation.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Base for all presentation ViewModels.
 *
 * Owns a [viewModelScope] (Main dispatcher + a [SupervisorJob] so one failed child
 * coroutine doesn't tear down its siblings) and a [dispose] that cancels it.
 *
 * ViewModels are Koin `factory` instances — a fresh one is created every time a
 * screen is entered. [dispose] MUST run when the owning composable leaves
 * composition (call `disposeOnLeave()` at the injection site, see
 * `com.fivucsas.shared.ui.util.disposeOnLeave`). Otherwise the scope — and any
 * `while (isActive)` poll loop launched in it — leaks for the lifetime of the
 * process, draining battery and network on a screen the user already left.
 *
 * Subclasses that need extra teardown override [dispose], run their cleanup, then
 * call `super.dispose()`:
 * ```
 * override fun dispose() {
 *     stopPolling()
 *     super.dispose()
 * }
 * ```
 */
abstract class BaseViewModel {

    /** Main-thread scope bounded to this ViewModel; cancelled by [dispose]. */
    protected val viewModelScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Cancels [viewModelScope]. Idempotent. Override to add teardown, then call super. */
    open fun dispose() {
        viewModelScope.cancel()
    }
}
