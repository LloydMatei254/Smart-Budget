package com.example.smartbudget.presentation.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.smartbudget.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditExpenseFragment : Fragment() {
    
    private val viewModel: AddEditExpenseViewModel by viewModels()
    
    private lateinit var toolbar: Toolbar
    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var cvCategorySelector: MaterialCardView
    private lateinit var tvSelectedCategory: TextView
    private lateinit var cvDateSelector: MaterialCardView
    private lateinit var tvSelectedDate: TextView
    private lateinit var cvPaymentMethodSelector: MaterialCardView
    private lateinit var tvSelectedPaymentMethod: TextView
    private lateinit var etNotes: TextInputEditText
    private lateinit var btnSelectPhoto: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var ivReceiptPreview: ImageView
    private lateinit var btnRemovePhoto: Button
    private lateinit var btnSave: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_expense, container, false)
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
        cvCategorySelector = view.findViewById(R.id.cvCategorySelector)
        tvSelectedCategory = view.findViewById(R.id.tvSelectedCategory)
        cvDateSelector = view.findViewById(R.id.cvDateSelector)
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        cvPaymentMethodSelector = view.findViewById(R.id.cvPaymentMethodSelector)
        tvSelectedPaymentMethod = view.findViewById(R.id.tvSelectedPaymentMethod)
        etNotes = view.findViewById(R.id.etNotes)
        btnSelectPhoto = view.findViewById(R.id.btnSelectPhoto)
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)
        ivReceiptPreview = view.findViewById(R.id.ivReceiptPreview)
        btnRemovePhoto = view.findViewById(R.id.btnRemovePhoto)
        btnSave = view.findViewById(R.id.btnSave)
        
        toolbar.title = "Add Expense"
    }
    
    private fun setupListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        cvCategorySelector.setOnClickListener {
            // TODO: Show category selector dialog
        }
        
        cvDateSelector.setOnClickListener {
            // TODO: Show date picker
        }
        
        cvPaymentMethodSelector.setOnClickListener {
            // TODO: Show payment method selector
        }
        
        btnSelectPhoto.setOnClickListener {
            // TODO: Implement photo picker
        }
        
        btnTakePhoto.setOnClickListener {
            // TODO: Implement camera
        }
        
        btnRemovePhoto.setOnClickListener {
            ivReceiptPreview.visibility = View.GONE
            btnRemovePhoto.visibility = View.GONE
        }
        
        btnSave.setOnClickListener {
            // TODO: Validate and save expense
            findNavController().navigateUp()
        }
    }
}
