package com.fivucsas.shared.domain.model

/**
 * Loading state with progress
 * Better UX with detailed loading information
 */
sealed class LoadingState {
    data object Idle : LoadingState()

    data class Loading(
        val progress: Float? = null,
        val message: String? = null
    ) : LoadingState()

    data class Success<T>(
        val data: T,
        val message: String? = null
    ) : LoadingState()

    data class Error(
        val error: AppError,
        val canRetry: Boolean = error.isRetryable
    ) : LoadingState()
}

/**
 * Extension functions for LoadingState
 */
val LoadingState.isLoading: Boolean
    get() = this is LoadingState.Loading

val LoadingState.isSuccess: Boolean
    get() = this is LoadingState.Success<*>

val LoadingState.isError: Boolean
    get() = this is LoadingState.Error

val LoadingState.isIdle: Boolean
    get() = this is LoadingState.Idle

/**
 * Get data or null
 */
fun <T> LoadingState.getDataOrNull(): T? {
    return when (this) {
        is LoadingState.Success<*> -> data as? T
        else -> null
    }
}

/**
 * Get error or null
 */
fun LoadingState.getErrorOrNull(): AppError? {
    return when (this) {
        is LoadingState.Error -> error
        else -> null
    }
}
