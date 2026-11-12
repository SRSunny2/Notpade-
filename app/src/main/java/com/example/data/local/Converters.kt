package com.example.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(category: ExpenseCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): ExpenseCategory = try {
        ExpenseCategory.valueOf(value)
    } catch (e: Exception) {
        ExpenseCategory.OTHERS
    }
}
