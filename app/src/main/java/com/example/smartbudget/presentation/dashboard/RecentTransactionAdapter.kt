package com.example.smartbudget.presentation.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbudget.databinding.ItemRecentTransactionBinding
import com.example.smartbudget.domain.model.Transaction
import com.example.smartbudget.utils.CurrencyUtils
import com.example.smartbudget.utils.DateUtils

/**
 * Adapter for recent transactions list
 */
class RecentTransactionAdapter(
    private val onTransactionClick: (Transaction) -> Unit
) : ListAdapter<Transaction, RecentTransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemRecentTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class TransactionViewHolder(
        private val binding: ItemRecentTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(transaction: Transaction) {
            binding.apply {
                // Set transaction details
                tvDescription.text = transaction.description
                tvCategory.text = transaction.categoryOrSource
                tvDate.text = DateUtils.formatRelativeDate(transaction.date)
                
                // Set amount with sign
                val isExpense = transaction is Transaction.ExpenseTransaction
                val amountText = if (isExpense) {
                    "-${CurrencyUtils.formatAmount(transaction.amount)}"
                } else {
                    "+${CurrencyUtils.formatAmount(transaction.amount)}"
                }
                tvAmount.text = amountText
                
                // Set amount color
                tvAmount.setTextColor(
                    if (isExpense) {
                        Color.parseColor("#E74C3C") // Red for expenses
                    } else {
                        Color.parseColor("#27AE60") // Green for income
                    }
                )
                
                // Set icon - use placeholder for now
                ivIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                
                // Click listener
                root.setOnClickListener {
                    onTransactionClick(transaction)
                }
            }
        }
    }
    
    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
