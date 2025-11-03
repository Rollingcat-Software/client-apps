package com.fivucsas.shared.domain.model

/**
 * Network result wrapper
 * Represents the result of a network operation
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: NetworkException) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
    
    /**
     * Check if result is success
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Check if result is error
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Check if result is loading
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Get data or null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}

/**
 * Network exception types
 */
sealed class NetworkException(message: String) : Exception(message) {
    class NetworkError(message: String = "Network error occurred") : NetworkException(message)
    class ServerError(val code: Int, message: String = "Server error: $code") : NetworkException(message)
    class Unauthorized(message: String = "Unauthorized") : NetworkException(message)
    class NotFound(message: String = "Resource not found") : NetworkException(message)
    class Timeout(message: String = "Request timeout") : NetworkException(message)
    class Unknown(message: String = "Unknown error") : NetworkException(message)
}

/**
 * Extension functions for NetworkResult
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (NetworkException) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(exception)
    return this
}

inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) action()
    return this
}

/**
 * Map success data to another type
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> NetworkResult.Error(exception)
        is NetworkResult.Loading -> NetworkResult.Loading
    }
}
