package com.example.data.repository

import com.example.data.local.ExpenseCategory
import com.example.data.local.ExpenseDao
import com.example.data.local.ExpenseEntity
import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiaryRepository(
    private val noteDao: NoteDao,
    private val expenseDao: ExpenseDao
) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun searchNotes(query: String): Flow<List<NoteEntity>> {
        return if (query.isBlank()) {
            noteDao.getAllNotes()
        } else {
            noteDao.searchNotes(query)
        }
    }

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    val totalExpenseAmount: Flow<Double> = expenseDao.getTotalExpenseAmount().map { it ?: 0.0 }

    suspend fun saveNote(note: NoteEntity): Long {
        return if (note.id == 0L) {
            noteDao.insert(note)
        } else {
            noteDao.update(note)
            note.id
        }
    }

    suspend fun deleteNote(note: NoteEntity) = noteDao.delete(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteById(id)

    suspend fun saveExpense(expense: ExpenseEntity): Long {
        return expenseDao.insert(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.delete(expense)

    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteById(id)

    suspend fun seedInitialDataIfEmpty() {
        if (noteDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            noteDao.insert(
                NoteEntity(
                    title = "Welcome to MyPocket Diary! 📔",
                    content = "Your all-in-one personal diary and daily expense notebook.\n\n" +
                            "• Keep track of daily thoughts, memories & to-dos in the Notepad.\n" +
                            "• Log expenses in Bangladeshi Taka (৳) with dedicated categories.\n" +
                            "• All records are saved securely and locally on your phone.",
                    dateModified = now
                )
            )
            noteDao.insert(
                NoteEntity(
                    title = "Grocery & Shopping List",
                    content = "1. Basmati Rice (5kg)\n2. Mustard oil & spices\n3. Fresh tea leaves from Sylhet\n4. Fresh fruits and nuts",
                    dateModified = now - 3600_000 * 4
                )
            )
        }

        if (expenseDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            expenseDao.insert(
                ExpenseEntity(
                    title = "Afternoon Chai & Snacks",
                    amount = 120.0,
                    category = ExpenseCategory.FOOD,
                    date = now - 3600_000 * 2
                )
            )
            expenseDao.insert(
                ExpenseEntity(
                    title = "Rickshaw & Uber Ride",
                    amount = 250.0,
                    category = ExpenseCategory.TRANSPORT,
                    date = now - 3600_000 * 8
                )
            )
            expenseDao.insert(
                ExpenseEntity(
                    title = "Monthly WiFi Internet Bill",
                    amount = 800.0,
                    category = ExpenseCategory.BILLS,
                    date = now - 86400_000
                )
            )
        }
    }
}
