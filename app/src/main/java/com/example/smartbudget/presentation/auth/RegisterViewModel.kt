package com.example.smartbudget.presentation.auth

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.model.Currency
import com.example.smartbudget.domain.usecase.auth.RegisterUseCase
import com.example.smartbudget.presentation.common.BaseViewModel
import com.example.smartbudget.presentation.common.UiState
import com.example.smartbudget.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    
    // Register state
    private val _registerState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val registerState: StateFlow<UiState<Unit>> = _registerState.asStateFlow()
    
    // Form fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()
    
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()
    
    private val _currency = MutableStateFlow(Currency.USD)
    val currency: StateFlow<Currency> = _currency.asStateFlow()
    
    private val _acceptTerms = MutableStateFlow(false)
    val acceptTerms: StateFlow<Boolean> = _acceptTerms.asStateFlow()
    
    // Field errors
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()
    
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()
    
    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()
    
    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()
    
    /**
     * Available currencies
     */
    val availableCurrencies = Currency.values().toList()
    
    /**
     * Update name field
     */
    fun onNameChanged(name: String) {
        _name.value = name
        _nameError.value = null
    }
    
    /**
     * Update email field
     */
    fun onEmailChanged(email: String) {
        _email.value = email
        _emailError.value = null
    }
    
    /**
     * Update password field
     */
    fun onPasswordChanged(password: String) {
        _password.value = password
        _passwordError.value = null
    }
    
    /**
     * Update confirm password field
     */
    fun onConfirmPasswordChanged(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        _confirmPasswordError.value = null
    }
    
    /**
     * Update currency
     */
    fun onCurrencyChanged(currency: Currency) {
        _currency.value = currency
    }
    
    /**
     * Toggle accept terms
     */
    fun onAcceptTermsChanged(accept: Boolean) {
        _acceptTerms.value = accept
    }
    
    /**
     * Perform registration
     */
    fun register() {
        // Validate inputs
        if (!validateInputs()) {
            return
        }
        
        viewModelScope.launch {
            try {
                _registerState.value = UiState.Loading
                
                val result = registerUseCase(
                    fullName = _name.value,
                    email = _email.value,
                    password = _password.value,
                    confirmPassword = _confirmPassword.value,
                    currency = _currency.value.code
                )
                
                result.onSuccess {
                    _registerState.value = UiState.Success(Unit)
                    Timber.i("Registration successful")
                    showToast("Account created successfully!")
                    navigate("dashboard")
                }
                
                result.onFailure { error ->
                    val errorMessage = error.message ?: "Registration failed"
                    _registerState.value = UiState.Error(errorMessage, error)
                    showSnackbar(errorMessage)
                    Timber.e(error, "Registration failed")
                }
            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.message ?: "Unknown error", e)
                showSnackbar("An unexpected error occurred")
                Timber.e(e, "Registration exception")
            }
        }
    }
    
    /**
     * Navigate to login screen
     */
    fun navigateToLogin() {
        navigateBack()
    }
    
    /**
     * Validate input fields
     */
    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validate name
        if (_name.value.trim().isEmpty()) {
            _nameError.value = "Name is required"
            isValid = false
        } else if (_name.value.trim().length < 2) {
            _nameError.value = "Name must be at least 2 characters"
            isValid = false
        }
        
        // Validate email
        val emailValidation = ValidationUtils.validateEmail(_email.value)
        if (!emailValidation.isValid) {
            _emailError.value = emailValidation.errorMessage
            isValid = false
        }
        
        // Validate password
        val passwordValidation = ValidationUtils.validatePassword(_password.value)
        if (!passwordValidation.isValid) {
            _passwordError.value = passwordValidation.errorMessage
            isValid = false
        }
        
        // Validate confirm password
        if (_confirmPassword.value != _password.value) {
            _confirmPasswordError.value = "Passwords do not match"
            isValid = false
        }
        
        // Validate terms acceptance
        if (!_acceptTerms.value) {
            showSnackbar("Please accept the terms and conditions")
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Continue with Google (placeholder)
     */
    fun continueWithGoogle() {
        showToast("Google Sign-In coming soon")
    }
}
