package com.example.smartbudget.presentation.categories

import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.smartbudget.R
import com.example.smartbudget.databinding.DialogCategoryBinding
import com.example.smartbudget.databinding.FragmentCategoriesBinding
import com.example.smartbudget.domain.model.Category
import com.example.smartbudget.presentation.common.BaseFragment
import com.example.smartbudget.presentation.common.UiState
import com.example.smartbudget.presentation.common.collectInLifecycle
import com.example.smartbudget.presentation.common.hide
import com.example.smartbudget.presentation.common.show
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Fragment for managing expense categories
 */
@AndroidEntryPoint
class CategoriesFragment : BaseFragment<FragmentCategoriesBinding, CategoriesViewModel>() {
    
    override val viewModel: CategoriesViewModel by viewModels()
    
    private lateinit var categoryAdapter: CategoryAdapter
    private var categoryDialog: Dialog? = null
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCategoriesBinding.inflate(inflater, container, false)
    
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
            
            // Sync button
            inflateMenu(R.menu.menu_categories)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
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
        categoryAdapter = CategoryAdapter(
            onCategoryClick = { category ->
                viewModel.showEditCategoryDialog(category)
            },
            onCategoryLongClick = { category ->
                if (!category.isDefault) {
                    viewModel.deleteCategory(category)
                } else {
                    viewModel.showSnackbar("Default categories cannot be deleted")
                }
            }
        )
        
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
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
        binding.fabAddCategory.setOnClickListener {
            viewModel.showAddCategoryDialog()
        }
    }
    
    override fun observeData() {
        // Categories state
        viewModel.categories.collectInLifecycle(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                    binding.rvCategories.hide()
                    binding.tvEmptyState.hide()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    
                    if (state.data.isEmpty()) {
                        binding.rvCategories.hide()
                        binding.tvEmptyState.show()
                    } else {
                        binding.rvCategories.show()
                        binding.tvEmptyState.hide()
                    }
                }
                is UiState.Error -> {
                    binding.progressBar.hide()
                    binding.rvCategories.hide()
                    binding.tvEmptyState.show()
                    showSnackbar("Failed to load categories: ${state.message}")
                }
                else -> {}
            }
        }
        
        // Filtered categories
        viewModel.filteredCategories.collectInLifecycle(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }
        
        // Dialog state
        viewModel.showCategoryDialog.collectInLifecycle(viewLifecycleOwner) { show ->
            if (show) {
                showCategoryDialog()
            } else {
                categoryDialog?.dismiss()
            }
        }
    }
    
    private fun showCategoryDialog() {
        val dialogBinding = DialogCategoryBinding.inflate(layoutInflater)
        val selectedCategory = viewModel.selectedCategory.value
        val isEditing = selectedCategory != null
        
        // Pre-fill form if editing
        if (isEditing && selectedCategory != null) {
            dialogBinding.etCategoryName.setText(selectedCategory.name)
            dialogBinding.tvSelectedColor.text = selectedCategory.color
            dialogBinding.viewColorPreview.setBackgroundColor(
                try {
                    Color.parseColor(selectedCategory.color)
                } catch (e: Exception) {
                    Color.GRAY
                }
            )
        } else {
            // Default values for new category
            dialogBinding.tvSelectedColor.text = "#3498DB"
            dialogBinding.viewColorPreview.setBackgroundColor(Color.parseColor("#3498DB"))
        }
        
        // Color picker
        setupColorPicker(dialogBinding)
        
        // Icon picker
        setupIconPicker(dialogBinding, selectedCategory?.icon ?: "shopping_cart")
        
        // Create dialog
        categoryDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (isEditing) "Edit Category" else "Add Category")
            .setView(dialogBinding.root)
            .setPositiveButton(if (isEditing) "Update" else "Create") { _, _ ->
                val name = dialogBinding.etCategoryName.text.toString()
                val color = dialogBinding.tvSelectedColor.text.toString()
                val icon = dialogBinding.spinnerIcon.selectedItem.toString()
                
                if (isEditing && selectedCategory != null) {
                    viewModel.updateCategory(selectedCategory, name, color, icon)
                } else {
                    viewModel.createCategory(name, color, icon)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                viewModel.hideCategoryDialog()
            }
            .setOnDismissListener {
                viewModel.hideCategoryDialog()
            }
            .create()
        
        categoryDialog?.show()
    }
    
    private fun setupColorPicker(binding: DialogCategoryBinding) {
        val colors = listOf(
            "#E74C3C", // Red
            "#3498DB", // Blue
            "#2ECC71", // Green
            "#F39C12", // Orange
            "#9B59B6", // Purple
            "#1ABC9C", // Teal
            "#34495E", // Dark Gray
            "#E91E63", // Pink
            "#FF5722", // Deep Orange
            "#795548"  // Brown
        )
        
        binding.btnSelectColor.setOnClickListener {
            val colorNames = colors.map { color ->
                when (color) {
                    "#E74C3C" -> "Red"
                    "#3498DB" -> "Blue"
                    "#2ECC71" -> "Green"
                    "#F39C12" -> "Orange"
                    "#9B59B6" -> "Purple"
                    "#1ABC9C" -> "Teal"
                    "#34495E" -> "Dark Gray"
                    "#E91E63" -> "Pink"
                    "#FF5722" -> "Deep Orange"
                    "#795548" -> "Brown"
                    else -> "Custom"
                }
            }.toTypedArray()
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Color")
                .setItems(colorNames) { _, which ->
                    val selectedColor = colors[which]
                    binding.tvSelectedColor.text = selectedColor
                    binding.viewColorPreview.setBackgroundColor(Color.parseColor(selectedColor))
                }
                .show()
        }
    }
    
    private fun setupIconPicker(binding: DialogCategoryBinding, selectedIcon: String) {
        val icons = listOf(
            "shopping_cart",
            "restaurant",
            "local_gas_station",
            "home",
            "directions_car",
            "phone",
            "shopping_bag",
            "local_hospital",
            "school",
            "sports_esports",
            "flight",
            "movie",
            "fitness_center",
            "pets",
            "card_giftcard"
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            icons
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerIcon.adapter = adapter
        
        // Set selected icon
        val selectedIndex = icons.indexOf(selectedIcon)
        if (selectedIndex >= 0) {
            binding.spinnerIcon.setSelection(selectedIndex)
        }
    }
    
    override fun onLoadingStateChanged(isLoading: Boolean) {
        // Handled by categories state observer
    }
    
    override fun onDestroyView() {
        categoryDialog?.dismiss()
        categoryDialog = null
        super.onDestroyView()
    }
}
