package com.example.smartbudget.presentation.income

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.smartbudget.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditIncomeFragment : Fragment() {
    
    private val viewModel: AddEditIncomeViewModel by viewModels()
    
    private lateinit var toolbar: Toolbar
    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var cvSourceSelector: MaterialCardView
    private lateinit var tvSelectedSource: TextView
    private lateinit var cvDateSelector: MaterialCardView
    private lateinit var tvSelectedDate: TextView
    private lateinit var switchRecurring: SwitchMaterial
    private lateinit var etNotes: TextInputEditText
    private lateinit var btnSave: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_income, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        etAmount = view.findViewById(R.id.etAmount)
        etDescription = view.findViewById(R.id.etDescription)
        cvSourceSelector = view.findViewById(R.id.cvSourceSelector)
        tvSelectedSource = view.findViewById(R.id.tvSelectedSource)
        cvDateSelector = view.findViewById(R.id.cvDateSelector)
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        switchRecurring = view.findViewById(R.id.switchRecurring)
        etNotes = view.findViewById(R.id.etNotes)
        btnSave = view.findViewById(R.id.btnSave)
        
        toolbar.title = "Add Income"
    }
    
    private fun setupListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        cvSourceSelector.setOnClickListener {
            // TODO: Show income source selector dialog
        }
        
        cvDateSelector.setOnClickListener {
            // TODO: Show date picker
        }
        
        switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Handle recurring income toggle
        }
        
        btnSave.setOnClickListener {
            // TODO: Validate and save income
            findNavController().navigateUp()
        }
    }
}
