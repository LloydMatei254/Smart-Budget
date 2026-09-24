package com.example.smartbudget.presentation.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbudget.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportsFragment : Fragment() {
    
    private val viewModel: ReportsViewModel by viewModels()
    
    private lateinit var toolbar: Toolbar
    private lateinit var periodTabLayout: TabLayout
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvNetSavings: TextView
    private lateinit var categoryBreakdownRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
        setupRecyclerView()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        periodTabLayout = view.findViewById(R.id.periodTabLayout)
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses)
        tvNetSavings = view.findViewById(R.id.tvNetSavings)
        categoryBreakdownRecyclerView = view.findViewById(R.id.categoryBreakdownRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        bottomNavigation = view.findViewById(R.id.bottomNavigation)
    }
    
    private fun setupListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        periodTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {} // Monthly
                    1 -> {} // Yearly
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        setupBottomNavigation()
    }
    
    private fun setupRecyclerView() {
        categoryBreakdownRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            // TODO: Set adapter when data is available
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.navigation_reports
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    findNavController().navigate(R.id.action_reports_to_dashboard)
                    true
                }
                R.id.navigation_transactions -> {
                    findNavController().navigate(R.id.action_reports_to_transactions)
                    true
                }
                R.id.navigation_add -> {
                    findNavController().navigate(R.id.action_reports_to_addExpense)
                    true
                }
                R.id.navigation_reports -> true
                R.id.navigation_settings -> {
                    findNavController().navigate(R.id.action_reports_to_settings)
                    true
                }
                else -> false
            }
        }
    }
}
