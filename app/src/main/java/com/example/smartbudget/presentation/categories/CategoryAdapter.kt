package com.example.smartbudget.presentation.categories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbudget.databinding.ItemCategoryBinding
import com.example.smartbudget.domain.model.Category

/**
 * Adapter for categories grid
 */
class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit,
    private val onCategoryLongClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(category: Category) {
            binding.apply {
                // Set category name
                tvCategoryName.text = category.name
                
                // Set icon (first letter as placeholder)
                tvCategoryIcon.text = category.name.firstOrNull()?.toString() ?: "?"
                
                // Set background color
                try {
                    val color = Color.parseColor(category.color)
                    cvCategory.setCardBackgroundColor(color)
                    
                    // Adjust text color based on background brightness
                    val brightness = ((Color.red(color) * 299) + 
                                     (Color.green(color) * 587) + 
                                     (Color.blue(color) * 114)) / 1000
                    
                    val textColor = if (brightness > 128) Color.BLACK else Color.WHITE
                    tvCategoryIcon.setTextColor(textColor)
                    tvCategoryName.setTextColor(textColor)
                } catch (e: Exception) {
                    cvCategory.setCardBackgroundColor(Color.GRAY)
                }
                
                // Show default badge
                if (category.isDefault) {
                    tvDefaultBadge.visibility = android.view.View.VISIBLE
                } else {
                    tvDefaultBadge.visibility = android.view.View.GONE
                }
                
                // Click listeners
                root.setOnClickListener {
                    onCategoryClick(category)
                }
                
                root.setOnLongClickListener {
                    onCategoryLongClick(category)
                    true
                }
            }
        }
    }
    
    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
