package com.example.smartbudget.presentation.income

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbudget.databinding.ItemIncomeBinding
import com.example.smartbudget.domain.model.Income
import com.example.smartbudget.utils.CurrencyUtils
import com.example.smartbudget.utils.DateUtils

/**
 * Adapter for income list
 */
class IncomeAdapter(
    private val onIncomeClick: (Income) -> Unit,
    private val onIncomeLongClick: (Income) -> Unit
) : ListAdapter<Income, IncomeAdapter.IncomeViewHolder>(IncomeDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val binding = ItemIncomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IncomeViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class IncomeViewHolder(
        private val binding: ItemIncomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(income: Income) {
            binding.apply {
                // Set income details
                tvIncomeDescription.text = income.description
                tvIncomeSource.text = income.source.displayName
                tvIncomeDate.text = DateUtils.formatToDisplayShort(income.date)
                tvIncomeAmount.text = CurrencyUtils.formatAmount(income.amount)
                
                // Show recurring indicator if recurring
                if (income.isRecurring) {
                    ivRecurringIndicator.visibility = android.view.View.VISIBLE
                } else {
                    ivRecurringIndicator.visibility = android.view.View.GONE
                }
                
                // Show notes indicator if notes exist
                if (!income.notes.isNullOrEmpty()) {
                    ivNotesIndicator.visibility = android.view.View.VISIBLE
                } else {
                    ivNotesIndicator.visibility = android.view.View.GONE
                }
                
                // Set amount color (green for income)
                tvIncomeAmount.setTextColor(Color.parseColor("#27AE60"))
                
                // Click listeners
                root.setOnClickListener {
                    onIncomeClick(income)
                }
                
                root.setOnLongClickListener {
                    onIncomeLongClick(income)
                    true
                }
            }
        }
    }
    
    private class IncomeDiffCallback : DiffUtil.ItemCallback<Income>() {
        override fun areItemsTheSame(oldItem: Income, newItem: Income): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Income, newItem: Income): Boolean {
            return oldItem == newItem
        }
    }
}
