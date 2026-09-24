package com.example.smartbudget.presentation.income

import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartbudget.R
import com.example.smartbudget.databinding.FragmentIncomeBinding
import com.example.smartbudget.domain.model.IncomeSource
import com.example.smartbudget.presentation.common.BaseFragment
import com.example.smartbudget.presentation.common.UiState
import com.example.smartbudget.presentation.common.collectInLifecycle
import com.example.smartbudget.presentation.common.hide
import com.example.smartbudget.presentation.common.show
import com.example.smartbudget.utils.CurrencyUtils
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

/**
 * Fragment for displaying income list
 */
@AndroidEntryPoint
class IncomeFragment : BaseFragment<FragmentIncomeBinding, IncomeViewModel>() {
    
    override val viewModel: IncomeViewModel by viewModels()
    
    private lateinit var incomeAdapter: IncomeAdapter
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentIncomeBinding.inflate(inflater, container, false)
    
    override fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                viewModel.navigateBack()
            }
            
            inflateMenu(R.menu.menu_income)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_filter -> {
                        showFilterDialog()
                        true
                    }
                    R.id.action_sort -> {
                        showSortDialog()
                        true
                    }
                    R.id.action_sync -> {
                        viewModel.refresh()
                        true
                    }
                    else -> false
                }
            }
        }
    }
    
    private fun setupRecyclerView() {
        incomeAdapter = IncomeAdapter(
            onIncomeClick = { income ->
                viewModel.navigateToIncomeDetail(income.id)
            },
            onIncomeLongClick = { income ->
                viewModel.deleteIncome(income)
            }
        )
        
        binding.rvIncome.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = incomeAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onSearchQueryChanged(newText ?: "")
                return true
            }
        })
    }
    
    private fun setupClickListeners() {
        binding.fabAddIncome.setOnClickListener {
            viewModel.navigateToAddIncome()
        }
        
        binding.btnClearFilters.setOnClickListener {
            viewModel.clearFilters()
        }
    }
    
    override fun observeData() {
        // Income state
        viewModel.incomes.collectInLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                    binding.rvIncome.hide()
                    binding.tvEmptyState.hide()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                }
                is UiState.Error -> {
                    binding.progressBar.hide()
                    binding.rvIncome.hide()
                    binding.tvEmptyState.show()
                    showSnackbar("Failed to load income: ${state.message}")
                }
                else -> {}
            }
        }
        
        // Filtered income
        viewModel.filteredIncome.collectInLifecycle(viewLifecycleOwner) { income ->
            incomeAdapter.submitList(income)
            
            if (income.isEmpty()) {
                binding.rvIncome.hide()
                binding.tvEmptyState.show()
            } else {
                binding.rvIncome.show()
                binding.tvEmptyState.hide()
            }
        }
        
        // Total income
        viewModel.totalIncome.collectInLifecycle(viewLifecycleOwner) { total ->
            binding.tvTotalIncome.text = "Total: ${CurrencyUtils.formatAmount(total)}"
        }
        
        // Setup source chips
        setupSourceChips()
        
        // Selected source
        viewModel.selectedSource.collectInLifecycle(viewLifecycleOwner) { source ->
            updateSourceChips(source)
            updateFilterVisibility()
        }
        
        // Date range
        viewModel.dateRange.collectInLifecycle(viewLifecycleOwner) { dateRange ->
            updateFilterVisibility()
        }
    }
    
    private fun setupSourceChips() {
        binding.chipGroupSources.removeAllViews()
        
        // Add "All" chip
        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = viewModel.selectedSource.value == null
            setOnClickListener {
                viewModel.filterBySource(null)
            }
        }
        binding.chipGroupSources.addView(allChip)
        
        // Add source chips
        viewModel.availableSources.forEach { source ->
            val chip = Chip(requireContext()).apply {
                text = source.displayName
                isCheckable = true
                isChecked = viewModel.selectedSource.value == source
                setOnClickListener {
                    viewModel.filterBySource(source)
                }
            }
            binding.chipGroupSources.addView(chip)
        }
    }
    
    private fun updateSourceChips(selectedSource: IncomeSource?) {
        for (i in 0 until binding.chipGroupSources.childCount) {
            val chip = binding.chipGroupSources.getChildAt(i) as? Chip
            chip?.isChecked = when {
                i == 0 && selectedSource == null -> true
                selectedSource != null -> {
                    viewModel.availableSources.getOrNull(i - 1) == selectedSource
                }
                else -> false
            }
        }
    }
    
    private fun updateFilterVisibility() {
        val hasFilters = viewModel.selectedSource.value != null || 
                        viewModel.dateRange.value != null
        
        if (hasFilters) {
            binding.btnClearFilters.show()
        } else {
            binding.btnClearFilters.hide()
        }
    }
    
    private fun showFilterDialog() {
        val items = arrayOf(
            "This Month",
            "Last Month",
            "Last 3 Months",
            "Custom Range",
            "Clear Date Filter"
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter by Date")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> filterThisMonth()
                    1 -> filterLastMonth()
                    2 -> filterLast3Months()
                    3 -> showCustomDateRangePicker()
                    4 -> viewModel.clearDateRangeFilter()
                }
            }
            .show()
    }
    
    private fun filterThisMonth() {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1)
        val endOfMonth = now.withDayOfMonth(now.lengthOfMonth())
        viewModel.filterByDateRange(startOfMonth, endOfMonth)
    }
    
    private fun filterLastMonth() {
        val now = LocalDate.now()
        val lastMonth = now.minusMonths(1)
        val startOfMonth = lastMonth.withDayOfMonth(1)
        val endOfMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
        viewModel.filterByDateRange(startOfMonth, endOfMonth)
    }
    
    private fun filterLast3Months() {
        val now = LocalDate.now()
        val threeMonthsAgo = now.minusMonths(3)
        viewModel.filterByDateRange(threeMonthsAgo, now)
    }
    
    private fun showCustomDateRangePicker() {
        val now = LocalDate.now()
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val startDate = LocalDate.of(year, month + 1, day)
                
                DatePickerDialog(
                    requireContext(),
                    { _, endYear, endMonth, endDay ->
                        val endDate = LocalDate.of(endYear, endMonth + 1, endDay)
                        viewModel.filterByDateRange(startDate, endDate)
                    },
                    now.year,
                    now.monthValue - 1,
                    now.dayOfMonth
                ).show()
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        ).show()
    }
    
    private fun showSortDialog() {
        val items = arrayOf(
            "Date (Newest First)",
            "Date (Oldest First)",
            "Amount (Highest First)",
            "Amount (Lowest First)"
        )
        
        val currentSort = when (viewModel.sortBy.value) {
            SortOption.DATE_DESC -> 0
            SortOption.DATE_ASC -> 1
            SortOption.AMOUNT_DESC -> 2
            SortOption.AMOUNT_ASC -> 3
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sort By")
            .setSingleChoiceItems(items, currentSort) { dialog, which ->
                val sortOption = when (which) {
                    0 -> SortOption.DATE_DESC
                    1 -> SortOption.DATE_ASC
                    2 -> SortOption.AMOUNT_DESC
                    3 -> SortOption.AMOUNT_ASC
                    else -> SortOption.DATE_DESC
                }
                viewModel.changeSortOption(sortOption)
                dialog.dismiss()
            }
            .show()
    }
    
    override fun onLoadingStateChanged(isLoading: Boolean) {
        // Handled by income state observer
    }
}
