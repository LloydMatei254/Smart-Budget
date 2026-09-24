package com.example.smartbudget.presentation.income

import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.example.smartbudget.databinding.FragmentAddEditIncomeBinding
import com.example.smartbudget.domain.model.IncomeSource
import com.example.smartbudget.presentation.common.BaseFragment
import com.example.smartbudget.presentation.common.collectInLifecycle
import com.example.smartbudget.utils.DateUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

/**
 * Fragment for adding/editing income
 */
@AndroidEntryPoint
class AddEditIncomeFragment : BaseFragment<FragmentAddEditIncomeBinding, AddEditIncomeViewModel>() {
    
    override val viewModel: AddEditIncomeViewModel by viewModels()
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAddEditIncomeBinding.inflate(inflater, container, false)
    
    override fun setupUI() {
        setupToolbar()
        setupInputListeners()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        binding.toolbar.apply {
            title = if (viewModel.isEditing) "Edit Income" else "Add Income"
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
        
        // Recurring switch
        binding.switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleRecurring(isChecked)
        }
    }
    
    private fun setupClickListeners() {
        // Source selector
        binding.cvSourceSelector.setOnClickListener {
            showSourceSelector()
        }
        
        // Date selector
        binding.cvDateSelector.setOnClickListener {
            showDatePicker()
        }
        
        // Save button
        binding.btnSave.setOnClickListener {
            viewModel.saveIncome()
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
        
        // Selected source
        viewModel.selectedSource.collectInLifecycle(viewLifecycleOwner) { source ->
            binding.tvSelectedSource.text = source.displayName
        }
        
        // Selected date
        viewModel.date.collectInLifecycle(viewLifecycleOwner) { date ->
            binding.tvSelectedDate.text = DateUtils.formatToDisplay(date)
        }
        
        // Notes field
        viewModel.notes.collectInLifecycle(viewLifecycleOwner) { notes ->
            if (binding.etNotes.text?.toString() != notes) {
                binding.etNotes.setText(notes)
            }
        }
        
        // Recurring state
        viewModel.isRecurring.collectInLifecycle(viewLifecycleOwner) { isRecurring ->
            if (binding.switchRecurring.isChecked != isRecurring) {
                binding.switchRecurring.isChecked = isRecurring
            }
        }
        
        // Form validity
        viewModel.isFormValid.collectInLifecycle(viewLifecycleOwner) { isValid ->
            binding.btnSave.isEnabled = isValid
        }
    }
    
    private fun showSourceSelector() {
        val sources = viewModel.availableSources
        val sourceNames = sources.map { it.displayName }.toTypedArray()
        val selectedIndex = sources.indexOf(viewModel.selectedSource.value)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Source")
            .setSingleChoiceItems(sourceNames, selectedIndex) { dialog, which ->
                viewModel.selectSource(sources[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
    
    override fun onLoadingStateChanged(isLoading: Boolean) {
        binding.btnSave.isEnabled = !isLoading && viewModel.isFormValid.value
    }
}
