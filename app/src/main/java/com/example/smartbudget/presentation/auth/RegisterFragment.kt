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
class RegisterFragment : Fragment() {
    
    private val viewModel: RegisterViewModel by viewModels()
    
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var loginTextView: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
        observeViewModel()
    }
    
    private fun initViews(view: View) {
        nameEditText = view.findViewById(R.id.nameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        registerButton = view.findViewById(R.id.registerButton)
        loginTextView = view.findViewById(R.id.loginTextView)
    }
    
    private fun setupListeners() {
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            
            viewModel.register(name, email, password, confirmPassword)
        }
        
        loginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
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
                    RegisterViewModel.NavigationEvent.NavigateToDashboard -> {
                        findNavController().navigate(R.id.action_register_to_dashboard)
                    }
                }
            }
        }
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        registerButton.isEnabled = !isLoading
        nameEditText.isEnabled = !isLoading
        emailEditText.isEnabled = !isLoading
        passwordEditText.isEnabled = !isLoading
        confirmPasswordEditText.isEnabled = !isLoading
        
        registerButton.text = if (isLoading) "Registering..." else "Register"
    }
    
    private fun showError(error: String) {
        view?.let {
            Snackbar.make(it, error, Snackbar.LENGTH_LONG).show()
        }
    }
}
