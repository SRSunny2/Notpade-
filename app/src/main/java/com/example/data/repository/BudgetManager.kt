package com.example.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetManager(context: Context) {
    private val prefs = context.getSharedPreferences("mypocket_budget_prefs", Context.MODE_PRIVATE)

    fun getCurrentMonthKey(): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    }

    fun getCurrentMonthDisplayName(): String {
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    private val _monthlyBudget = MutableStateFlow(getBudgetForCurrentMonth())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    fun getBudgetForCurrentMonth(): Double {
        val key = "budget_" + getCurrentMonthKey()
        val stored = prefs.getFloat(key, -1f)
        return if (stored >= 0f) {
            stored.toDouble()
        } else {
            // Default initial cash in hand / budget: 30,000 BDT
            30000.0
        }
    }

    fun setBudgetForCurrentMonth(amount: Double) {
        val key = "budget_" + getCurrentMonthKey()
        prefs.edit().putFloat(key, amount.toFloat()).apply()
        _monthlyBudget.value = amount
    }
}
