package com.example.smartbudget.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.smartbudget.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    
    private val viewModel: SettingsViewModel by viewModels()
    
    private lateinit var toolbar: Toolbar
    private lateinit var profileCard: MaterialCardView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var currencyLayout: LinearLayout
    private lateinit var tvSelectedCurrency: TextView
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var switchBudgetAlerts: SwitchMaterial
    private lateinit var exportDataLayout: LinearLayout
    private lateinit var clearDataLayout: LinearLayout
    private lateinit var btnLogout: Button
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        profileCard = view.findViewById(R.id.profileCard)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        currencyLayout = view.findViewById(R.id.currencyLayout)
        tvSelectedCurrency = view.findViewById(R.id.tvSelectedCurrency)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchBudgetAlerts = view.findViewById(R.id.switchBudgetAlerts)
        exportDataLayout = view.findViewById(R.id.exportDataLayout)
        clearDataLayout = view.findViewById(R.id.clearDataLayout)
        btnLogout = view.findViewById(R.id.btnLogout)
        bottomNavigation = view.findViewById(R.id.bottomNavigation)
    }
    
    private fun setupListeners() {
        profileCard.setOnClickListener {
            // TODO: Navigate to profile edit screen
        }
        
        currencyLayout.setOnClickListener {
            // TODO: Show currency selector dialog
        }
        
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Update notification preference
        }
        
        switchBudgetAlerts.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Update budget alert preference
        }
        
        exportDataLayout.setOnClickListener {
            // TODO: Export data to CSV/JSON
        }
        
        clearDataLayout.setOnClickListener {
            // TODO: Show confirmation dialog and clear data
        }
        
        btnLogout.setOnClickListener {
            // TODO: Handle logout
            findNavController().navigate(R.id.action_settings_to_login)
        }
        
        setupBottomNavigation()
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.navigation_settings
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    findNavController().navigate(R.id.action_settings_to_dashboard)
                    true
                }
                R.id.navigation_transactions -> {
                    findNavController().navigate(R.id.action_settings_to_transactions)
                    true
                }
                R.id.navigation_add -> {
                    findNavController().navigate(R.id.action_settings_to_addExpense)
                    true
                }
                R.id.navigation_reports -> {
                    findNavController().navigate(R.id.action_settings_to_reports)
                    true
                }
                R.id.navigation_settings -> true
                else -> false
            }
        }
    }
}
