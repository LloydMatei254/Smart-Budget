package com.example.smartbudget.presentation.common

/**
 * Sealed class representing the state of a UI component
 * Provides a type-safe way to handle loading, success, and error states
 */
sealed class UiState<out T> {
    
    /**
     * Initial state before any action
     */
    object Idle : UiState<Nothing>()
    
    /**
     * Loading state while fetching data or performing an operation
     */
    object Loading : UiState<Nothing>()
    
    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Error state with error message
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()
    
    /**
     * Check if current state is loading
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Check if current state is success
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Check if current state is error
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Get data if state is Success, null otherwise
     */
    fun getDataOrNull(): T? {
        return if (this is Success) data else null
    }
    
    /**
     * Get error message if state is Error, null otherwise
     */
    fun getErrorOrNull(): String? {
        return if (this is Error) message else null
    }
}

/**
 * Extension function to map UiState data
 */
fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> {
    return when (this) {
        is UiState.Idle -> UiState.Idle
        is UiState.Loading -> UiState.Loading
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Error -> UiState.Error(message, throwable)
    }
}

/**
 * Extension function to handle UiState with callbacks
 */
inline fun <T> UiState<T>.onState(
    onIdle: () -> Unit = {},
    onLoading: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    when (this) {
        is UiState.Idle -> onIdle()
        is UiState.Loading -> onLoading()
        is UiState.Success -> onSuccess(data)
        is UiState.Error -> onError(message)
    }
}
