package com.example.smartbudget.presentation.auth

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.usecase.auth.LoginUseCase
import com.example.smartbudget.presentation.common.BaseViewModel
import com.example.smartbudget.presentation.common.UiState
import com.example.smartbudget.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Login screen
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {
    
    // Navigation events
    sealed class NavigationEvent {
        object NavigateToDashboard : NavigationEvent()
    }
    
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()
    
    // UI state
    data class LoginUiState(
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    /**
     * Perform login
     */
    fun login(email: String, password: String) {
        // Validate inputs
        if (!validateInputs(email, password)) {
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = loginUseCase(
                    email = email,
                    password = password,
                    rememberMe = true
                )
                
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _navigationEvents.emit(NavigationEvent.NavigateToDashboard)
                    Timber.i("Login successful")
                }
                
                result.onFailure { error ->
                    val errorMessage = error.message ?: "Login failed"
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMessage)
                    Timber.e(error, "Login failed")
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                _uiState.value = _uiState.value.copy(isLoading = false, error = errorMessage)
                Timber.e(e, "Login exception")
            }
        }
    }
    
    /**
     * Validate input fields
     */
    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Email is required")
            return false
        }
        
        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.isValid) {
            _uiState.value = _uiState.value.copy(error = emailValidation.errorMessage)
            return false
        }
        
        if (password.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Password is required")
            return false
        }
        
        return true
    }
}
