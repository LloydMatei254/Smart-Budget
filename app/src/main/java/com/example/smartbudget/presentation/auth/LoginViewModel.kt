package com.example.smartbudget.presentation.auth

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.usecase.auth.LoginUseCase
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
 * ViewModel for Login screen
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {
    
    // Login state
    private val _loginState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val loginState: StateFlow<UiState<Unit>> = _loginState.asStateFlow()
    
    // Email field
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    // Password field
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    // Remember me
    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()
    
    // Field errors
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()
    
    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()
    
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
     * Toggle remember me
     */
    fun onRememberMeChanged(rememberMe: Boolean) {
        _rememberMe.value = rememberMe
    }
    
    /**
     * Perform login
     */
    fun login() {
        // Validate inputs
        if (!validateInputs()) {
            return
        }
        
        viewModelScope.launch {
            try {
                _loginState.value = UiState.Loading
                
                val result = loginUseCase(
                    email = _email.value,
                    password = _password.value,
                    rememberMe = _rememberMe.value
                )
                
                result.onSuccess {
                    _loginState.value = UiState.Success(Unit)
                    Timber.i("Login successful")
                    navigate("dashboard")
                }
                
                result.onFailure { error ->
                    val errorMessage = error.message ?: "Login failed"
                    _loginState.value = UiState.Error(errorMessage, error)
                    showSnackbar(errorMessage)
                    Timber.e(error, "Login failed")
                }
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Unknown error", e)
                showSnackbar("An unexpected error occurred")
                Timber.e(e, "Login exception")
            }
        }
    }
    
    /**
     * Navigate to register screen
     */
    fun navigateToRegister() {
        navigate("register")
    }
    
    /**
     * Navigate to forgot password
     */
    fun navigateToForgotPassword() {
        navigate("forgot_password")
    }
    
    /**
     * Validate input fields
     */
    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validate email
        val emailValidation = ValidationUtils.validateEmail(_email.value)
        if (!emailValidation.isValid) {
            _emailError.value = emailValidation.errorMessage
            isValid = false
        }
        
        // Validate password
        if (_password.value.isEmpty()) {
            _passwordError.value = "Password is required"
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
