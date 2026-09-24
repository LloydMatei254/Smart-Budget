package com.example.smartbudget.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Base ViewModel with common functionality for all ViewModels
 * Provides error handling, loading states, and event management
 */
abstract class BaseViewModel : ViewModel() {
    
    /**
     * Shared flow for one-time UI events
     */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()
    
    /**
     * State flow for loading state
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * State flow for error message
     */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Execute a suspending operation with automatic loading state and error handling
     */
    protected fun <T> launchWithLoading(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend () -> T
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                block()
            } catch (e: Exception) {
                Timber.e(e, "Operation failed")
                _error.value = e.message ?: "An unknown error occurred"
                onError?.invoke(e) ?: handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Execute a suspending operation without loading state
     */
    protected fun <T> launchSilent(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend () -> T
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                Timber.e(e, "Silent operation failed")
                onError?.invoke(e) ?: handleError(e)
            }
        }
    }
    
    /**
     * Execute a Result-based operation with UiState emission
     */
    protected fun <T> executeWithState(
        stateFlow: MutableStateFlow<UiState<T>>,
        block: suspend () -> Result<T>
    ) {
        viewModelScope.launch {
            try {
                stateFlow.value = UiState.Loading
                val result = block()
                
                result.onSuccess { data ->
                    stateFlow.value = UiState.Success(data)
                }
                
                result.onFailure { throwable ->
                    val errorMessage = throwable.message ?: "An unknown error occurred"
                    stateFlow.value = UiState.Error(errorMessage, throwable)
                    Timber.e(throwable, "Operation failed")
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "An unknown error occurred"
                stateFlow.value = UiState.Error(errorMessage, e)
                Timber.e(e, "Operation failed with exception")
            }
        }
    }
    
    /**
     * Send a UI event
     */
    protected fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }
    
    /**
     * Show a toast message
     */
    protected fun showToast(message: String) {
        sendEvent(UiEvent.ShowToast(message))
    }
    
    /**
     * Show a snackbar message
     */
    fun showSnackbar(message: String, actionLabel: String? = null, action: (() -> Unit)? = null) {
        sendEvent(UiEvent.ShowSnackbar(message, actionLabel, action))
    }
    
    /**
     * Show error dialog
     */
    protected fun showError(message: String, title: String = "Error") {
        sendEvent(UiEvent.ShowError(title, message))
    }
    
    /**
     * Navigate to a destination
     */
    protected fun navigate(route: String) {
        sendEvent(UiEvent.Navigate(route))
    }
    
    /**
     * Navigate back
     */
    open fun navigateBack() {
        sendEvent(UiEvent.NavigateBack)
    }
    
    /**
     * Handle errors with default behavior
     */
    protected open fun handleError(throwable: Throwable) {
        val message = when (throwable) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Connection timeout"
            is javax.net.ssl.SSLException -> "Secure connection failed"
            else -> throwable.message ?: "An error occurred"
        }
        showSnackbar(message)
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
}
