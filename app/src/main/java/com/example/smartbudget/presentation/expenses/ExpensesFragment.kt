package com.example.smartbudget.presentation.expenses

import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartbudget.R
import com.example.smartbudget.databinding.FragmentExpensesBinding
import com.example.smartbudget.domain.model.Category
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
 * Fragment for displaying expenses list
 */
@AndroidEntryPoint
class ExpensesFragment : BaseFragment<FragmentExpensesBinding, ExpensesViewModel>() {
    
    override val viewModel: ExpensesViewModel by viewModels()
    
    private lateinit var expenseAdapter: ExpenseAdapter
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentExpensesBinding.inflate(inflater, container, false)
    
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
            
            inflateMenu(R.menu.menu_expenses)
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
        expenseAdapter = ExpenseAdapter(
            onExpenseClick = { expense ->
                viewModel.navigateToExpenseDetail(expense.id)
            },
            onExpenseLongClick = { expense ->
                viewModel.deleteExpense(expense)
            }
        )
        
        binding.rvExpenses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onSearchQueryChanged(newText ?: "")
                return true
            }
        })
    }
    
    private fun setupClickListeners() {
        binding.fabAddExpense.setOnClickListener {
            viewModel.navigateToAddExpense()
        }
        
        binding.btnClearFilters.setOnClickListener {
            viewModel.clearFilters()
        }
    }
    
    override fun observeData() {
        // Expenses state
        viewModel.expenses.collectInLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                    binding.rvExpenses.hide()
                    binding.tvEmptyState.hide()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                }
                is UiState.Error -> {
                    binding.progressBar.hide()
                    binding.rvExpenses.hide()
                    binding.tvEmptyState.show()
                    showSnackbar("Failed to load expenses: ${state.message}")
                }
                else -> {}
            }
        }
        
        // Filtered expenses
        viewModel.filteredExpenses.collectInLifecycle(viewLifecycleOwner) { expenses ->
            expenseAdapter.submitList(expenses)
            
            if (expenses.isEmpty()) {
                binding.rvExpenses.hide()
                binding.tvEmptyState.show()
            } else {
                binding.rvExpenses.show()
                binding.tvEmptyState.hide()
            }
        }
        
        // Total expenses
        viewModel.totalExpenses.collectInLifecycle(viewLifecycleOwner) { total ->
            binding.tvTotalExpenses.text = "Total: ${CurrencyUtils.formatAmount(total)}"
        }
        
        // Categories for filter chips
        viewModel.categories.collectInLifecycle(viewLifecycleOwner) { categories ->
            setupCategoryChips(categories)
        }
        
        // Selected category
        viewModel.selectedCategoryId.collectInLifecycle(viewLifecycleOwner) { categoryId ->
            updateCategoryChips(categoryId)
            updateFilterVisibility()
        }
        
        // Date range
        viewModel.dateRange.collectInLifecycle(viewLifecycleOwner) { dateRange ->
            updateFilterVisibility()
        }
    }
    
    private fun setupCategoryChips(categories: List<com.example.smartbudget.domain.model.Category>) {
        binding.chipGroupCategories.removeAllViews()
        
        // Add "All" chip
        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = viewModel.selectedCategoryId.value == null
            setOnClickListener {
                viewModel.filterByCategory(null)
            }
        }
        binding.chipGroupCategories.addView(allChip)
        
        // Add category chips
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.name
                isCheckable = true
                isChecked = viewModel.selectedCategoryId.value == category.id
                setOnClickListener {
                    viewModel.filterByCategory(category.id)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }
    
    private fun updateCategoryChips(selectedCategoryId: String?) {
        for (i in 0 until binding.chipGroupCategories.childCount) {
            val chip = binding.chipGroupCategories.getChildAt(i) as? Chip
            chip?.isChecked = when {
                i == 0 && selectedCategoryId == null -> true
                selectedCategoryId != null -> {
                    val categories = (viewModel.categories.value as? UiState.Success<List<Category>>)?.data
                    categories?.getOrNull(i - 1)?.id == selectedCategoryId
                }
                else -> false
            }
        }
    }
    
    private fun updateFilterVisibility() {
        val hasFilters = viewModel.selectedCategoryId.value != null || 
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
        
        // Pick start date
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val startDate = LocalDate.of(year, month + 1, day)
                
                // Pick end date
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
        // Handled by expenses state observer
    }
}
