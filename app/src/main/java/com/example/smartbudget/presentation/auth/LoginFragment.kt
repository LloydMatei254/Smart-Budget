package com.example.smartbudget.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartbudget.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    
    private val viewModel: LoginViewModel by viewModels()
    
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var skipLoginTextView: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
        observeViewModel()
    }
    
    private fun initViews(view: View) {
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        registerTextView = view.findViewById(R.id.registerTextView)
        skipLoginTextView = view.findViewById(R.id.skipLoginTextView)
    }
    
    private fun setupListeners() {
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            viewModel.login(email, password)
        }
        
        registerTextView.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        
        // Skip login for development/testing (offline mode)
        skipLoginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_dashboard)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateLoadingState(state.isLoading)
                
                state.error?.let { error ->
                    showError(error)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigationEvents.collect { event ->
                when (event) {
                    LoginViewModel.NavigationEvent.NavigateToDashboard -> {
                        findNavController().navigate(R.id.action_login_to_dashboard)
                    }
                }
            }
        }
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        loginButton.isEnabled = !isLoading
        emailEditText.isEnabled = !isLoading
        passwordEditText.isEnabled = !isLoading
        
        loginButton.text = if (isLoading) "Logging in..." else "Login"
    }
    
    private fun showError(error: String) {
        view?.let {
            Snackbar.make(it, error, Snackbar.LENGTH_LONG).show()
        }
    }
}
