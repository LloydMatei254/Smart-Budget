package com.example.smartbudget.presentation.expenses

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbudget.databinding.ItemExpenseBinding
import com.example.smartbudget.domain.model.Expense
import com.example.smartbudget.utils.CurrencyUtils
import com.example.smartbudget.utils.DateUtils

/**
 * Adapter for expenses list
 */
class ExpenseAdapter(
    private val onExpenseClick: (Expense) -> Unit,
    private val onExpenseLongClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(expense: Expense) {
            binding.apply {
                // Set expense details
                tvExpenseDescription.text = expense.description
                tvExpenseCategory.text = expense.categoryId // Will be replaced with category name
                tvExpenseDate.text = DateUtils.formatToDisplayShort(expense.date)
                tvExpenseAmount.text = CurrencyUtils.formatAmount(expense.amount)
                
                // Show notes indicator if notes exist
                if (!expense.notes.isNullOrEmpty()) {
                    ivNotesIndicator.visibility = android.view.View.VISIBLE
                } else {
                    ivNotesIndicator.visibility = android.view.View.GONE
                }
                
                // Show receipt indicator if photo exists
                if (!expense.receiptPhotoUrl.isNullOrEmpty()) {
                    ivReceiptIndicator.visibility = android.view.View.VISIBLE
                } else {
                    ivReceiptIndicator.visibility = android.view.View.GONE
                }
                
                // Set amount color (red for expenses)
                tvExpenseAmount.setTextColor(Color.parseColor("#E74C3C"))
                
                // Click listeners
                root.setOnClickListener {
                    onExpenseClick(expense)
                }
                
                root.setOnLongClickListener {
                    onExpenseLongClick(expense)
                    true
                }
            }
        }
    }
    
    private class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}
