package com.example.smartbudget.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.smartbudget.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    
    private val viewModel: DashboardViewModel by viewModels()
    
    private lateinit var greetingText: TextView
    private lateinit var refreshIcon: ImageView
    private lateinit var notificationIcon: ImageView
    private lateinit var balanceAmount: TextView
    private lateinit var balanceChange: TextView
    private lateinit var visibilityToggle: ImageView
    private lateinit var incomeAmount: TextView
    private lateinit var expensesAmount: TextView
    private lateinit var seeAllButton: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    
    private var isBalanceVisible = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
        setupGreeting()
    }
    
    private fun initViews(view: View) {
        greetingText = view.findViewById(R.id.greetingText)
        refreshIcon = view.findViewById(R.id.refreshIcon)
        notificationIcon = view.findViewById(R.id.notificationIcon)
        balanceAmount = view.findViewById(R.id.balanceAmount)
        balanceChange = view.findViewById(R.id.balanceChange)
        visibilityToggle = view.findViewById(R.id.visibilityToggle)
        incomeAmount = view.findViewById(R.id.incomeAmount)
        expensesAmount = view.findViewById(R.id.expensesAmount)
        seeAllButton = view.findViewById(R.id.seeAllButton)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        bottomNavigation = view.findViewById(R.id.bottomNavigation)
        
        // Set initial values
        balanceAmount.text = "****"
        balanceChange.text = "+0.0%"
        incomeAmount.text = "$0.00"
        expensesAmount.text = "$0.00"
        emptyStateText.text = "No transactions yet"
    }
    
    private fun setupListeners() {
        // Refresh icon
        refreshIcon.setOnClickListener {
            // TODO: Implement refresh
        }
        
        // Notification icon
        notificationIcon.setOnClickListener {
            // TODO: Navigate to notifications
        }
        
        // Toggle balance visibility
        visibilityToggle.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            if (isBalanceVisible) {
                balanceAmount.text = "$0.00" // TODO: Show actual balance
                visibilityToggle.setImageResource(android.R.drawable.ic_menu_view)
            } else {
                balanceAmount.text = "****"
                visibilityToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            }
        }
        
        // See all transactions
        seeAllButton.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_transactions)
        }
        
        // Bottom navigation
        setupBottomNavigation()
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.navigation_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_transactions -> {
                    findNavController().navigate(R.id.action_dashboard_to_transactions)
                    true
                }
                R.id.navigation_add -> {
                    findNavController().navigate(R.id.action_dashboard_to_add_expense)
                    true
                }
                R.id.navigation_reports -> {
                    findNavController().navigate(R.id.action_dashboard_to_reports)
                    true
                }
                R.id.navigation_settings -> {
                    findNavController().navigate(R.id.action_dashboard_to_settings)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupGreeting() {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (hourOfDay) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        
        greetingText.text = greeting
    }
}
