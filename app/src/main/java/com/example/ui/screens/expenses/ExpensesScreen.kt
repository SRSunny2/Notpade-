package com.example.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.ExpenseCategory
import com.example.data.local.ExpenseEntity

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    allExpenses: List<ExpenseEntity>,
    totalSpentThisMonth: Double,
    monthlyBudget: Double,
    remainingBalance: Double,
    monthName: String,
    selectedCategoryFilter: ExpenseCategory?,
    onCategoryFilterChange: (ExpenseCategory?) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onAddNewExpense: () -> Unit,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("expenses_list"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 88.dp // Space for FAB
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header item: Total Expense & Monthly Cash Card
                item {
                    TotalExpenseCard(
                        totalSpentThisMonth = totalSpentThisMonth,
                        monthlyBudget = monthlyBudget,
                        remainingBalance = remainingBalance,
                        monthName = monthName,
                        transactionCount = expenses.size,
                        expenses = allExpenses,
                        onEditBudgetClick = onEditBudgetClick
                    )
                }

                // Category Filter Chips
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "All" filter chip
                        val isAllSelected = selectedCategoryFilter == null
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { onCategoryFilterChange(null) }
                                .testTag("filter_category_all")
                        ) {
                            Text(
                                text = "All (${allExpenses.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        // Category chips
                        ExpenseCategory.values().forEach { category ->
                            val isSelected = selectedCategoryFilter == category
                            val count = allExpenses.count { it.category == category }
                            val catColor = category.getColor()

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) catColor else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                  .clickable {
                                      onCategoryFilterChange(if (isSelected) null else category)
                                  }
                                  .testTag("filter_category_${category.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (isSelected) Color.White else catColor,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${category.displayName} ($count)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Section Title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategoryFilter == null) "Recent Expenses" else "${selectedCategoryFilter.displayName} Expenses",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "${expenses.size} ${if (expenses.size == 1) "item" else "items"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Empty state or Items
                if (expenses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.size(76.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.ReceiptLong,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = if (selectedCategoryFilter == null) "No Expenses Logged" else "No ${selectedCategoryFilter.displayName} Expenses",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (selectedCategoryFilter == null) {
                                        "Tap the + button to log your daily spending in Bangladeshi Taka (৳)."
                                    } else {
                                        "No records found in this category. Tap + to add an expense."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = expenses,
                        key = { it.id }
                    ) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            onDelete = { onDeleteExpense(expense) }
                        )
                    }
                }
            }
        }

        // FAB to Add Expense
        FloatingActionButton(
            onClick = onAddNewExpense,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_expense_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
