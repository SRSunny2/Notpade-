package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.chat.AiChatScreen
import com.example.ui.screens.expenses.AddExpenseSheet
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.expenses.SetBudgetDialog
import com.example.ui.screens.notepad.NoteEditorSheet
import com.example.ui.screens.notepad.NotepadScreen
import com.example.ui.theme.Emerald600
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DiaryViewModel
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    private val viewModel: DiaryViewModel by viewModels {
        DiaryViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check initialization with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MyPocketDiaryApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPocketDiaryApp(viewModel: DiaryViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isNoteEditorOpen by viewModel.isNoteEditorOpen.collectAsStateWithLifecycle()
    val editingNote by viewModel.editingNote.collectAsStateWithLifecycle()
    val isTranscribing by viewModel.isTranscribing.collectAsStateWithLifecycle()

    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val totalExpenseAmount by viewModel.totalExpenseAmount.collectAsStateWithLifecycle()
    val totalExpenseThisMonth by viewModel.totalExpenseThisMonth.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val remainingBalance by viewModel.remainingBalance.collectAsStateWithLifecycle()
    val isSetBudgetOpen by viewModel.isSetBudgetOpen.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val isAddExpenseOpen by viewModel.isAddExpenseOpen.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val selectedModelOption by viewModel.selectedModelOption.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val streamingBotText by viewModel.streamingBotText.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Emerald600,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "৳",
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MyPocket Diary",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(1.dp)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                // Tab 0: Notepad
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (notes.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text("${notes.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.EditNote else Icons.Outlined.EditNote,
                                contentDescription = "Notepad",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Notepad",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_notepad")
                )

                // Tab 1: Expenses
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (allExpenses.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ) {
                                        Text("${allExpenses.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                contentDescription = "Expenses",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = "Expenses (৳)",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_expenses")
                )

                // Tab 2: AI Assistant
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Assistant",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "AI Chat",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_ai_chat")
                )
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTab,
            label = "tab_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tabIndex ->
            when (tabIndex) {
                0 -> NotepadScreen(
                    notes = notes,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onNoteClick = { viewModel.openEditNoteEditor(it) },
                    onDeleteNote = { viewModel.deleteNote(it) },
                    onAddNewNote = { viewModel.openNewNoteEditor() }
                )
                1 -> ExpensesScreen(
                    expenses = filteredExpenses,
                    allExpenses = allExpenses,
                    totalSpentThisMonth = totalExpenseThisMonth,
                    monthlyBudget = monthlyBudget,
                    remainingBalance = remainingBalance,
                    monthName = viewModel.currentMonthDisplayName,
                    selectedCategoryFilter = selectedCategoryFilter,
                    onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onAddNewExpense = { viewModel.openAddExpense() },
                    onEditBudgetClick = { viewModel.openSetBudget() }
                )
                2 -> AiChatScreen(
                    messages = chatMessages,
                    selectedModel = selectedModelOption,
                    isLoading = isChatLoading,
                    streamingText = streamingBotText,
                    errorMessage = chatError,
                    isTranscribing = isTranscribing,
                    onModelSelect = { viewModel.setModelOption(it) },
                    onSendMessage = { viewModel.sendChatMessage(it) },
                    onClearChat = { viewModel.clearChat() },
                    onTranscribeAudio = { file, onResult ->
                        viewModel.transcribeAudioFile(file, onResult)
                    }
                )
            }
        }
    }

    // Set Budget Dialog
    if (isSetBudgetOpen) {
        SetBudgetDialog(
            currentBudget = monthlyBudget,
            monthName = viewModel.currentMonthDisplayName,
            onDismiss = { viewModel.closeSetBudget() },
            onSaveBudget = { newBudget ->
                viewModel.updateMonthlyBudget(newBudget)
            }
        )
    }

    // Note Editor Dialog/Sheet
    if (isNoteEditorOpen) {
        NoteEditorSheet(
            note = editingNote,
            isTranscribing = isTranscribing,
            onDismiss = { viewModel.closeNoteEditor() },
            onSave = { title, content, id ->
                viewModel.saveNote(title, content, id)
            },
            onTranscribeAudio = { file, onResult ->
                viewModel.transcribeAudioFile(file, onResult)
            }
        )
    }

    // Add Expense Dialog/Sheet
    if (isAddExpenseOpen) {
        AddExpenseSheet(
            onDismiss = { viewModel.closeAddExpense() },
            onAddExpense = { title, amount, category, dateMillis ->
                viewModel.saveExpense(title, amount, category, dateMillis)
            }
        )
    }
}
