package com.example.smartbudget.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Base Fragment with common functionality for all fragments
 * Handles view binding, event collection, and common UI operations
 */
abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment() {
    
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    
    protected abstract val viewModel: VM
    
    /**
     * Inflate the view binding
     */
    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    
    /**
     * Setup UI components and listeners
     */
    protected abstract fun setupUI()
    
    /**
     * Observe ViewModel data
     */
    protected abstract fun observeData()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeData()
        observeEvents()
        observeLoadingState()
    }
    
    /**
     * Observe UI events from ViewModel
     */
    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }
    
    /**
     * Observe loading state
     */
    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    onLoadingStateChanged(isLoading)
                }
            }
        }
    }
    
    /**
     * Handle UI events
     */
    protected open fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowToast -> {
                showToast(event.message)
            }
            is UiEvent.ShowSnackbar -> {
                showSnackbar(event.message, event.actionLabel, event.action)
            }
            is UiEvent.Navigate -> {
                onNavigate(event.route)
            }
            is UiEvent.NavigateBack -> {
                navigateBack()
            }
            is UiEvent.ShowError -> {
                showErrorDialog(event.title, event.message, event.dismissLabel)
            }
            is UiEvent.ShowConfirmation -> {
                showConfirmationDialog(
                    event.title,
                    event.message,
                    event.confirmLabel,
                    event.cancelLabel,
                    event.onConfirm
                )
            }
            is UiEvent.ShowLoading -> {
                showLoadingDialog(event.message)
            }
            is UiEvent.HideLoading -> {
                hideLoadingDialog()
            }
        }
    }
    
    /**
     * Called when loading state changes
     */
    protected open fun onLoadingStateChanged(isLoading: Boolean) {
        // Override in subclasses to show/hide loading indicator
    }
    
    /**
     * Show a toast message
     */
    protected fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message, duration).show()
    }
    
    /**
     * Show a snackbar message
     */
    protected fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        action: (() -> Unit)? = null,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        val snackbar = Snackbar.make(binding.root, message, duration)
        if (actionLabel != null && action != null) {
            snackbar.setAction(actionLabel) { action() }
        }
        snackbar.show()
    }
    
    /**
     * Navigate to a destination
     * Override in subclasses to implement navigation
     */
    protected open fun onNavigate(route: String) {
        Timber.d("Navigate to: $route")
        // Navigation implementation will be added later
    }
    
    /**
     * Navigate back
     */
    protected open fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
    
    /**
     * Show error dialog
     * Override in subclasses to implement custom error dialog
     */
    protected open fun showErrorDialog(title: String, message: String, dismissLabel: String) {
        // Default implementation using AlertDialog
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(dismissLabel, null)
            .show()
    }
    
    /**
     * Show confirmation dialog
     */
    protected open fun showConfirmationDialog(
        title: String,
        message: String,
        confirmLabel: String,
        cancelLabel: String,
        onConfirm: () -> Unit
    ) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(confirmLabel) { _, _ -> onConfirm() }
            .setNegativeButton(cancelLabel, null)
            .show()
    }
    
    /**
     * Show loading dialog
     */
    protected open fun showLoadingDialog(message: String) {
        // Implementation can be added later with a custom loading dialog
        Timber.d("Show loading: $message")
    }
    
    /**
     * Hide loading dialog
     */
    protected open fun hideLoadingDialog() {
        // Implementation can be added later
        Timber.d("Hide loading")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
