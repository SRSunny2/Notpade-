package com.example.data.local

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.CategoryBills
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryOthers
import com.example.ui.theme.CategoryShopping
import com.example.ui.theme.CategoryTransport

enum class ExpenseCategory(val displayName: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    OTHERS("Others");

    fun getColor(): Color = when (this) {
        FOOD -> CategoryFood
        TRANSPORT -> CategoryTransport
        SHOPPING -> CategoryShopping
        BILLS -> CategoryBills
        OTHERS -> CategoryOthers
    }
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val date: Long = System.currentTimeMillis()
)
