package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiModelOption
import com.example.data.ai.ChatMessage
import com.example.data.ai.GeminiAiService
import com.example.data.ai.MessageSender
import com.example.data.local.AppDatabase
import com.example.data.local.ExpenseCategory
import com.example.data.local.ExpenseEntity
import com.example.data.local.NoteEntity
import com.example.data.repository.BudgetManager
import com.example.data.repository.DiaryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryViewModel(
    application: Application,
    private val repository: DiaryRepository,
    private val budgetManager: BudgetManager = BudgetManager(application),
    private val aiService: GeminiAiService = GeminiAiService()
) : AndroidViewModel(application) {

    // Tab state: 0 = Notepad, 1 = Expenses, 2 = AI Assistant
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    // --- Notepad State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val notes: StateFlow<List<NoteEntity>> = _searchQuery
        .flatMapLatest { query ->
            repository.searchNotes(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _editingNote = MutableStateFlow<NoteEntity?>(null)
    val editingNote: StateFlow<NoteEntity?> = _editingNote.asStateFlow()

    private val _isNoteEditorOpen = MutableStateFlow(false)
    val isNoteEditorOpen: StateFlow<Boolean> = _isNoteEditorOpen.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openNewNoteEditor() {
        _editingNote.value = null
        _isNoteEditorOpen.value = true
    }

    fun openEditNoteEditor(note: NoteEntity) {
        _editingNote.value = note
        _isNoteEditorOpen.value = true
    }

    fun closeNoteEditor() {
        _isNoteEditorOpen.value = false
        _editingNote.value = null
    }

    fun saveNote(title: String, content: String, id: Long = 0L) {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()
        if (trimmedTitle.isEmpty() && trimmedContent.isEmpty()) return

        viewModelScope.launch {
            val note = NoteEntity(
                id = id,
                title = trimmedTitle,
                content = trimmedContent,
                dateModified = System.currentTimeMillis()
            )
            repository.saveNote(note)
            closeNoteEditor()
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // --- Expenses State ---
    private val _selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategoryFilter: StateFlow<ExpenseCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _isAddExpenseOpen = MutableStateFlow(false)
    val isAddExpenseOpen: StateFlow<Boolean> = _isAddExpenseOpen.asStateFlow()

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses,
        _selectedCategoryFilter
    ) { list, filter ->
        if (filter == null) list else list.filter { it.category == filter }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Month Budget and Calculations
    val monthlyBudget: StateFlow<Double> = budgetManager.monthlyBudget

    val currentMonthDisplayName: String
        get() = budgetManager.getCurrentMonthDisplayName()

    private val _isSetBudgetOpen = MutableStateFlow(false)
    val isSetBudgetOpen: StateFlow<Boolean> = _isSetBudgetOpen.asStateFlow()

    fun openSetBudget() {
        _isSetBudgetOpen.value = true
    }

    fun closeSetBudget() {
        _isSetBudgetOpen.value = false
    }

    fun updateMonthlyBudget(amount: Double) {
        if (amount >= 0) {
            budgetManager.setBudgetForCurrentMonth(amount)
            closeSetBudget()
        }
    }

    // Expenses in current month
    val totalExpenseThisMonth: StateFlow<Double> = allExpenses.combine(monthlyBudget) { list, _ ->
        val currentMonthKey = budgetManager.getCurrentMonthKey()
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        list.filter { sdf.format(Date(it.date)) == currentMonthKey }
            .sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Total expenses according to category filter
    val totalExpenseAmount: StateFlow<Double> = allExpenses.combine(_selectedCategoryFilter) { list, filter ->
        val targetList = if (filter == null) list else list.filter { it.category == filter }
        targetList.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Remaining cash in hand (Budget - Expenses this month)
    val remainingBalance: StateFlow<Double> = combine(
        monthlyBudget,
        totalExpenseThisMonth
    ) { budget, spent ->
        budget - spent
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun setCategoryFilter(category: ExpenseCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun openAddExpense() {
        _isAddExpenseOpen.value = true
    }

    fun closeAddExpense() {
        _isAddExpenseOpen.value = false
    }

    fun saveExpense(title: String, amount: Double, category: ExpenseCategory, dateMillis: Long) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty() || amount <= 0) return

        viewModelScope.launch {
            val expense = ExpenseEntity(
                id = 0L,
                title = trimmedTitle,
                amount = amount,
                category = category,
                date = dateMillis
            )
            repository.saveExpense(expense)
            closeAddExpense()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // --- Audio Transcription State ---
    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _transcriptionError = MutableStateFlow<String?>(null)
    val transcriptionError: StateFlow<String?> = _transcriptionError.asStateFlow()

    fun transcribeAudioFile(file: File, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isTranscribing.value = true
            _transcriptionError.value = null
            try {
                val bytes = file.readBytes()
                val text = aiService.transcribeAudio(bytes)
                onComplete(text)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _transcriptionError.value = e.localizedMessage ?: "Failed to transcribe audio."
            } finally {
                _isTranscribing.value = false
                try { file.delete() } catch (_: Exception) {}
            }
        }
    }

    fun clearTranscriptionError() {
        _transcriptionError.value = null
    }

    // --- Gemini Chatbot State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.BOT,
                text = "Hello! I am your MyPocket Diary Assistant. I can help you summarize notes, reflect on your day, or plan and budget your expenses in Bangladeshi Taka (৳). How can I help you today?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _selectedModelOption = MutableStateFlow(AiModelOption.GENERAL)
    val selectedModelOption: StateFlow<AiModelOption> = _selectedModelOption.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _streamingBotText = MutableStateFlow<String?>(null)
    val streamingBotText: StateFlow<String?> = _streamingBotText.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    fun setModelOption(option: AiModelOption) {
        _selectedModelOption.value = option
    }

    fun sendChatMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isChatLoading.value) return

        val userMessage = ChatMessage(sender = MessageSender.USER, text = trimmed)
        val updatedHistory = _chatMessages.value + userMessage
        _chatMessages.value = updatedHistory

        viewModelScope.launch {
            _isChatLoading.value = true
            _chatError.value = null
            _streamingBotText.value = ""
            val accumulated = StringBuilder()

            try {
                aiService.streamChatMessage(
                    history = updatedHistory,
                    userMessage = trimmed,
                    modelOption = _selectedModelOption.value
                ).collect { chunk ->
                    accumulated.append(chunk)
                    _streamingBotText.value = accumulated.toString()
                }

                val finalBotReply = accumulated.toString().trim()
                if (finalBotReply.isNotEmpty()) {
                    _chatMessages.value = updatedHistory + ChatMessage(
                        sender = MessageSender.BOT,
                        text = finalBotReply
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _chatError.value = e.localizedMessage ?: "Failed to generate AI response."
            } finally {
                _isChatLoading.value = false
                _streamingBotText.value = null
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.BOT,
                text = "Chat history cleared. How can I help you next?"
            )
        )
    }

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    val repo = DiaryRepository(db.noteDao(), db.expenseDao())
                    val budget = BudgetManager(application)
                    return DiaryViewModel(application, repo, budget) as T
                }
            }
    }
}
