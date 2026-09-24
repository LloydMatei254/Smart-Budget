package com.example.smartbudget.presentation.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartbudget.databinding.FragmentDashboardBinding
import com.example.smartbudget.presentation.common.BaseFragment
import com.example.smartbudget.presentation.common.UiState
import com.example.smartbudget.presentation.common.collectInLifecycle
import com.example.smartbudget.presentation.common.hide
import com.example.smartbudget.presentation.common.show
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Dashboard Fragment showing financial overview
 */
@AndroidEntryPoint
class DashboardFragment : BaseFragment<FragmentDashboardBinding, DashboardViewModel>() {
    
    override val viewModel: DashboardViewModel by viewModels()
    
    private lateinit var transactionAdapter: RecentTransactionAdapter
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDashboardBinding.inflate(inflater, container, false)
    
    override fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        setupSwipeRefresh()
    }
    
    private fun setupToolbar() {
        binding.apply {
            // Sync button
            ivSync.setOnClickListener {
                viewModel.refresh()
            }
            
            // Profile/Settings
            ivProfile.setOnClickListener {
                viewModel.navigateToSettings()
            }
        }
    }
    
    private fun setupRecyclerView() {
        transactionAdapter = RecentTransactionAdapter(
            onTransactionClick = { transaction ->
                when (transaction) {
                    is com.example.smartbudget.domain.model.Transaction.ExpenseTransaction -> {
                        viewModel.navigateToTransactionDetail(transaction.id, isExpense = true)
                    }
                    is com.example.smartbudget.domain.model.Transaction.IncomeTransaction -> {
                        viewModel.navigateToTransactionDetail(transaction.id, isExpense = false)
                    }
                }
            }
        )
        
        binding.rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            // Balance card click to toggle visibility
            cvBalance.setOnClickListener {
                viewModel.toggleBalanceVisibility()
            }
            
            // Income card
            cvIncome.setOnClickListener {
                viewModel.navigateToAddIncome()
            }
            
            // Expense card
            cvExpense.setOnClickListener {
                viewModel.navigateToAddExpense()
            }
            
            // See all transactions
            tvSeeAll.setOnClickListener {
                viewModel.navigateToTransactions()
            }
            
            // FAB for add expense
            fabAddExpense.setOnClickListener {
                viewModel.navigateToAddExpense()
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }
    
    override fun observeData() {
        // User data
        viewModel.user.collectInLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    binding.tvGreeting.text = "${viewModel.getGreeting()},\n${state.data.fullName}"
                }
                is UiState.Error -> {
                    binding.tvGreeting.text = viewModel.getGreeting()
                }
                else -> {}
            }
        }
        
        // Financial summary
        viewModel.financialSummary.collectInLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    updateFinancialSummary()
                }
                is UiState.Error -> {
                    binding.progressBar.hide()
                    showSnackbar("Failed to load data: ${state.message}")
                }
                else -> {}
            }
        }
        
        // Recent transactions
        viewModel.recentTransactions.collectInLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show loading in recyclerview
                }
                is UiState.Success -> {
                    transactionAdapter.submitList(state.data)
                    
                    // Show/hide empty state
                    if (state.data.isEmpty()) {
                        binding.tvEmptyTransactions.show()
                        binding.rvRecentTransactions.hide()
                    } else {
                        binding.tvEmptyTransactions.hide()
                        binding.rvRecentTransactions.show()
                    }
                }
                is UiState.Error -> {
                    showSnackbar("Failed to load transactions")
                }
                else -> {}
            }
        }
        
        // Refresh state
        viewModel.isRefreshing.collectInLifecycle(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefresh.isRefreshing = isRefreshing
        }
        
        // Balance visibility
        viewModel.balanceVisible.collectInLifecycle(viewLifecycleOwner) { visible ->
            updateBalanceVisibility(visible)
        }
        
        // Formatted values
        viewModel.formattedBalance.collectInLifecycle(viewLifecycleOwner) { balance ->
            binding.tvBalance.text = balance
        }
        
        viewModel.formattedTotalIncome.collectInLifecycle(viewLifecycleOwner) { income ->
            binding.tvIncomeAmount.text = income
        }
        
        viewModel.formattedTotalExpenses.collectInLifecycle(viewLifecycleOwner) { expenses ->
            binding.tvExpenseAmount.text = expenses
        }
        
        viewModel.balanceChangePercentage.collectInLifecycle(viewLifecycleOwner) { percentage ->
            binding.tvBalanceChange.text = percentage
        }
        
        viewModel.balanceChangePositive.collectInLifecycle(viewLifecycleOwner) { positive ->
            binding.tvBalanceChange.setTextColor(
                if (positive) {
                    requireContext().getColor(android.R.color.holo_green_dark)
                } else {
                    requireContext().getColor(android.R.color.holo_red_dark)
                }
            )
        }
    }
    
    private fun updateFinancialSummary() {
        // Values are automatically updated via StateFlow collectors above
        Timber.d("Financial summary updated")
    }
    
    private fun updateBalanceVisibility(visible: Boolean) {
        if (visible) {
            binding.ivBalanceVisibility.setImageResource(android.R.drawable.ic_menu_view)
        } else {
            binding.ivBalanceVisibility.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            binding.tvBalance.text = "****"
        }
    }
    
    override fun onLoadingStateChanged(isLoading: Boolean) {
        // Handled by progress bar in financial summary observer
    }
}
