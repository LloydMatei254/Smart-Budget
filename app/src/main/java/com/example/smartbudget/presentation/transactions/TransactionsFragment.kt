package com.example.smartbudget.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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
class TransactionsFragment : Fragment() {
    
    private val viewModel: TransactionsViewModel by viewModels()
    
    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var searchEditText: EditText
    private lateinit var filterIcon: ImageView
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
        setupRecyclerView()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        tabLayout = view.findViewById(R.id.tabLayout)
        searchEditText = view.findViewById(R.id.searchEditText)
        filterIcon = view.findViewById(R.id.filterIcon)
        transactionsRecyclerView = view.findViewById(R.id.transactionsRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        bottomNavigation = view.findViewById(R.id.bottomNavigation)
    }
    
    private fun setupListeners() {
        // Toolbar back button
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // TODO: Filter transactions based on tab
                when (tab?.position) {
                    0 -> {} // All
                    1 -> {} // Income
                    2 -> {} // Expenses
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        // Filter icon
        filterIcon.setOnClickListener {
            // TODO: Show filter dialog
        }
        
        // Bottom navigation
        setupBottomNavigation()
    }
    
    private fun setupRecyclerView() {
        transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            // TODO: Set adapter when data is available
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.navigation_transactions
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    findNavController().navigate(R.id.action_transactions_to_dashboard)
                    true
                }
                R.id.navigation_transactions -> true
                R.id.navigation_add -> {
                    findNavController().navigate(R.id.action_transactions_to_addExpense)
                    true
                }
                R.id.navigation_reports -> {
                    findNavController().navigate(R.id.action_transactions_to_reports)
                    true
                }
                R.id.navigation_settings -> {
                    findNavController().navigate(R.id.action_transactions_to_settings)
                    true
                }
                else -> false
            }
        }
    }
}
