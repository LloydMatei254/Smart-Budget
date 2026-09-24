package com.example.smartbudget.presentation.expenses

import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.example.smartbudget.databinding.FragmentAddEditExpenseBinding
import com.example.smartbudget.presentation.common.BaseFragment
import com.example.smartbudget.presentation.common.UiState
import com.example.smartbudget.presentation.common.collectInLifecycle
import com.example.smartbudget.presentation.common.hide
import com.example.smartbudget.presentation.common.show
import com.example.smartbudget.utils.DateUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.time.LocalDate

/**
 * Fragment for adding/editing expenses
 */
@AndroidEntryPoint
class AddEditExpenseFragment : BaseFragment<FragmentAddEditExpenseBinding, AddEditExpenseViewModel>() {
    
    override val viewModel: AddEditExpenseViewModel by viewModels()
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAddEditExpenseBinding.inflate(inflater, container, false)
    
    override fun setupUI() {
        setupToolbar()
        setupInputListeners()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        binding.toolbar.apply {
            title = if (viewModel.isEditing) "Edit Expense" else "Add Expense"
            setNavigationOnClickListener {
                viewModel.navigateBack()
            }
        }
    }
    
    private fun setupInputListeners() {
        // Amount input
        binding.etAmount.addTextChangedListener { text ->
            viewModel.onAmountChanged(text?.toString() ?: "")
        }
        
        // Description input
        binding.etDescription.addTextChangedListener { text ->
            viewModel.onDescriptionChanged(text?.toString() ?: "")
        }
        
        // Notes input
        binding.etNotes.addTextChangedListener { text ->
            viewModel.onNotesChanged(text?.toString() ?: "")
        }
    }
    
    private fun setupClickListeners() {
        // Category selector
        binding.cvCategorySelector.setOnClickListener {
            showCategorySelector()
        }
        
        // Date selector
        binding.cvDateSelector.setOnClickListener {
            showDatePicker()
        }
        
        // Payment method selector
        binding.cvPaymentMethodSelector.setOnClickListener {
            showPaymentMethodSelector()
        }
        
        // Photo buttons
        binding.btnSelectPhoto.setOnClickListener {
            // TODO: Implement photo picker
            showToast("Photo picker coming soon")
        }
        
        binding.btnTakePhoto.setOnClickListener {
            // TODO: Implement camera
            showToast("Camera coming soon")
        }
        
        binding.btnRemovePhoto.setOnClickListener {
            viewModel.setReceiptPhoto(null)
        }
        
        // Save button
        binding.btnSave.setOnClickListener {
            viewModel.saveExpense()
        }
    }
    
    override fun observeData() {
        // Amount field
        viewModel.amount.collectInLifecycle(viewLifecycleOwner) { amount ->
            if (binding.etAmount.text?.toString() != amount) {
                binding.etAmount.setText(amount)
            }
        }
        
        viewModel.amountError.collectInLifecycle(viewLifecycleOwner) { error ->
            binding.tilAmount.error = error
        }
        
        // Description field
        viewModel.description.collectInLifecycle(viewLifecycleOwner) { description ->
            if (binding.etDescription.text?.toString() != description) {
                binding.etDescription.setText(description)
            }
        }
        
        viewModel.descriptionError.collectInLifecycle(viewLifecycleOwner) { error ->
            binding.tilDescription.error = error
        }
        
        // Selected category
        viewModel.selectedCategory.collectInLifecycle(viewLifecycleOwner) { category ->
            if (category != null) {
                binding.tvSelectedCategory.text = category.name
                binding.tvSelectedCategory.setTextColor(
                    requireContext().getColor(android.R.color.black)
                )
                binding.tvCategoryError.hide()
            } else {
                binding.tvSelectedCategory.text = "Select Category"
                binding.tvSelectedCategory.setTextColor(
                    requireContext().getColor(android.R.color.darker_gray)
                )
            }
        }
        
        viewModel.categoryError.collectInLifecycle(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.tvCategoryError.text = error
                binding.tvCategoryError.show()
            } else {
                binding.tvCategoryError.hide()
            }
        }
        
        // Selected date
        viewModel.date.collectInLifecycle(viewLifecycleOwner) { date ->
            binding.tvSelectedDate.text = DateUtils.formatToDisplay(date)
        }
        
        // Selected payment method
        viewModel.selectedPaymentMethod.collectInLifecycle(viewLifecycleOwner) { paymentMethod ->
            if (paymentMethod != null) {
                binding.tvSelectedPaymentMethod.text = paymentMethod.name
                binding.tvSelectedPaymentMethod.setTextColor(
                    requireContext().getColor(android.R.color.black)
                )
            } else {
                binding.tvSelectedPaymentMethod.text = "Select Payment Method"
                binding.tvSelectedPaymentMethod.setTextColor(
                    requireContext().getColor(android.R.color.darker_gray)
                )
            }
        }
        
        // Notes field
        viewModel.notes.collectInLifecycle(viewLifecycleOwner) { notes ->
            if (binding.etNotes.text?.toString() != notes) {
                binding.etNotes.setText(notes)
            }
        }
        
        // Receipt photo
        viewModel.receiptPhotoUri.collectInLifecycle(viewLifecycleOwner) { uri ->
            if (uri != null) {
                binding.ivReceiptPreview.show()
                binding.btnRemovePhoto.show()
                // TODO: Load image from URI
            } else {
                binding.ivReceiptPreview.hide()
                binding.btnRemovePhoto.hide()
            }
        }
        
        // Form validity
        viewModel.isFormValid.collectInLifecycle(viewLifecycleOwner) { isValid ->
            binding.btnSave.isEnabled = isValid
        }
        
        // Categories list
        viewModel.categories.collectInLifecycle(viewLifecycleOwner) { state ->
            // Categories are loaded for selector
        }
        
        // Payment methods list
        viewModel.paymentMethods.collectInLifecycle(viewLifecycleOwner) { state ->
            // Payment methods are loaded for selector
        }
    }
    
    private fun showCategorySelector() {
        val categoriesState = viewModel.categories.value
        
        if (categoriesState is UiState.Success) {
            val categories = categoriesState.data
            val categoryNames = categories.map { it.name }.toTypedArray()
            val selectedIndex = categories.indexOfFirst { 
                it.id == viewModel.selectedCategory.value?.id 
            }
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Category")
                .setSingleChoiceItems(categoryNames, selectedIndex) { dialog, which ->
                    viewModel.selectCategory(categories[which])
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            showSnackbar("Categories not loaded yet")
        }
    }
    
    private fun showDatePicker() {
        val currentDate = viewModel.date.value
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                viewModel.onDateChanged(selectedDate)
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        ).show()
    }
    
    private fun showPaymentMethodSelector() {
        val paymentMethodsState = viewModel.paymentMethods.value
        
        if (paymentMethodsState is UiState.Success) {
            val paymentMethods = paymentMethodsState.data
            val methodNames = paymentMethods.map { it.name }.toTypedArray()
            val selectedIndex = paymentMethods.indexOfFirst { 
                it.id == viewModel.selectedPaymentMethod.value?.id 
            }
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Payment Method")
                .setSingleChoiceItems(methodNames, selectedIndex) { dialog, which ->
                    viewModel.selectPaymentMethod(paymentMethods[which])
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            showSnackbar("Payment methods not loaded yet")
        }
    }
    
    override fun onLoadingStateChanged(isLoading: Boolean) {
        binding.btnSave.isEnabled = !isLoading && viewModel.isFormValid.value
    }
}
