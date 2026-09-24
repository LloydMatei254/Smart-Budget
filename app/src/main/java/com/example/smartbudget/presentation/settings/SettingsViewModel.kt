package com.example.smartbudget.presentation.settings

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.model.User
import com.example.smartbudget.domain.usecase.auth.GetCurrentUserUseCase
import com.example.smartbudget.domain.usecase.auth.LogoutUseCase
import com.example.smartbudget.presentation.common.BaseViewModel
import com.example.smartbudget.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel() {
    
    private val _user = MutableStateFlow<UiState<User>>(UiState.Loading)
    val user: StateFlow<UiState<User>> = _user.asStateFlow()
    
    init {
        loadUser()
    }
    
    private fun loadUser() {
        executeWithState(_user) {
            getCurrentUserUseCase().mapCatching { user ->
                user ?: throw IllegalStateException("User not logged in")
            }
        }
    }
    
    fun logout() {
        sendEvent(
            com.example.smartbudget.presentation.common.UiEvent.ShowConfirmation(
                title = "Logout",
                message = "Are you sure you want to logout?",
                confirmLabel = "Logout",
                onConfirm = {
                    performLogout()
                }
            )
        )
    }
    
    private fun performLogout() {
        launchWithLoading {
            try {
                logoutUseCase()
                showToast("Logged out successfully")
                navigate("login")
            } catch (e: Exception) {
                showError("Logout failed: ${e.message}")
                Timber.e(e, "Logout failed")
            }
        }
    }
    
    override fun navigateBack() {
        super.navigateBack()
    }
}
