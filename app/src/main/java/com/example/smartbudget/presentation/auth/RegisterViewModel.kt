package com.example.smartbudget.presentation.auth

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.model.Currency
import com.example.smartbudget.domain.usecase.auth.RegisterUseCase
import com.example.smartbudget.presentation.common.BaseViewModel
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
 * ViewModel for Register screen
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : BaseViewModel() {
    
    // Navigation events
    sealed class NavigationEvent {
        object NavigateToDashboard : NavigationEvent()
    }
    
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()
    
    // UI state
    data class RegisterUiState(
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    /**
     * Perform registration
     */
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        // Validate inputs
        if (!validateInputs(name, email, password, confirmPassword)) {
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = registerUseCase(
                    fullName = name,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    currency = Currency.USD.code
                )
                
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _navigationEvents.emit(NavigationEvent.NavigateToDashboard)
                    Timber.i("Registration successful")
                }
                
                result.onFailure { error ->
                    val errorMessage = error.message ?: "Registration failed"
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMessage)
                    Timber.e(error, "Registration failed")
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                _uiState.value = _uiState.value.copy(isLoading = false, error = errorMessage)
                Timber.e(e, "Registration exception")
            }
        }
    }
    
    /**
     * Validate input fields
     */
    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.trim().isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Name is required")
            return false
        }
        
        if (name.trim().length < 2) {
            _uiState.value = _uiState.value.copy(error = "Name must be at least 2 characters")
            return false
        }
        
        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.isValid) {
            _uiState.value = _uiState.value.copy(error = emailValidation.errorMessage)
            return false
        }
        
        val passwordValidation = ValidationUtils.validatePassword(password)
        if (!passwordValidation.isValid) {
            _uiState.value = _uiState.value.copy(error = passwordValidation.errorMessage)
            return false
        }
        
        if (confirmPassword != password) {
            _uiState.value = _uiState.value.copy(error = "Passwords do not match")
            return false
        }
        
        return true
    }
}
