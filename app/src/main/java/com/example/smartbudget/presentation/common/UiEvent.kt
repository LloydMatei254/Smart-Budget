package com.example.smartbudget.presentation.common

/**
 * Sealed class representing one-time UI events
 * Used for navigation, showing toasts, dialogs, etc.
 */
sealed class UiEvent {
    
    /**
     * Show a toast message
     */
    data class ShowToast(val message: String) : UiEvent()
    
    /**
     * Show a snackbar message with optional action
     */
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val action: (() -> Unit)? = null
    ) : UiEvent()
    
    /**
     * Navigate to a destination
     */
    data class Navigate(val route: String) : UiEvent()
    
    /**
     * Navigate back
     */
    object NavigateBack : UiEvent()
    
    /**
     * Show error dialog
     */
    data class ShowError(
        val title: String = "Error",
        val message: String,
        val dismissLabel: String = "OK"
    ) : UiEvent()
    
    /**
     * Show confirmation dialog
     */
    data class ShowConfirmation(
        val title: String,
        val message: String,
        val confirmLabel: String = "Confirm",
        val cancelLabel: String = "Cancel",
        val onConfirm: () -> Unit
    ) : UiEvent()
    
    /**
     * Show loading dialog
     */
    data class ShowLoading(val message: String = "Loading...") : UiEvent()
    
    /**
     * Hide loading dialog
     */
    object HideLoading : UiEvent()
}
