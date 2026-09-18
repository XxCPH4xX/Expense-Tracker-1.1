package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionWithCategory
import com.example.data.model.QuickAddTemplate
import com.example.ui.components.BudgetIncomeSummaryCard
import com.example.ui.components.CategoryBudgetItem
import com.example.ui.components.CustomDateRangePickerDialog
import com.example.ui.components.DailyPaceBudgetCard
import com.example.ui.components.DailySpendingBarChart
import com.example.ui.components.HeroBalanceCard
import com.example.ui.components.InteractiveDonutChart
import com.example.ui.components.MonthYearPickerDialog
import com.example.ui.components.MonthlyAuditNotificationBanner
import com.example.ui.components.QuickAddShortcutsBar
import com.example.ui.components.TransactionItemCard
import com.example.ui.model.FinanceUiState
import com.example.ui.model.TimeRange
import com.example.ui.theme.Emerald700

@Composable
fun DashboardScreen(
    uiState: FinanceUiState,
    onTimeRangeSelected: (TimeRange) -> Unit,
    onSpecificMonthSelected: (year: Int, month: Int) -> Unit = { _, _ -> },
    onCustomDateRangeSelected: (startTimestamp: Long, endTimestamp: Long) -> Unit = { _, _ -> },
    onAddTransactionClick: () -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onManageCategoriesClick: () -> Unit,
    onViewFullAuditClick: () -> Unit = {},
    onQuickAddTemplate: (QuickAddTemplate) -> Unit = {},
    onCustomizeQuickAdd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMonthPicker by remember { mutableStateOf(false) }
    var showCustomRangePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Filter Chips Row
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TimeRange.values()) { range ->
                    val isSelected = uiState.timeRange == range
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            when (range) {
                                TimeRange.SPECIFIC_MONTH -> showMonthPicker = true
                                TimeRange.CUSTOM_RANGE -> showCustomRangePicker = true
                                else -> onTimeRangeSelected(range)
                            }
                        },
                        leadingIcon = {
                            when (range) {
                                TimeRange.SPECIFIC_MONTH -> Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) androidx.compose.ui.graphics.Color.White else Emerald700
                                )
                                TimeRange.CUSTOM_RANGE -> Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) androidx.compose.ui.graphics.Color.White else Emerald700
                                )
                                else -> null
                            }
                        },
                        label = {
                            val labelText = if (isSelected && (range == TimeRange.SPECIFIC_MONTH || range == TimeRange.CUSTOM_RANGE)) {
                                uiState.periodLabel
                            } else {
                                range.displayName
                            }
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald700,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_${range.name}")
                    )
                }
            }
        }

        // Hero Balance & Cash Flow Card
        item {
            HeroBalanceCard(
                netBalance = uiState.overview.netBalance,
                totalIncome = uiState.overview.totalIncome,
                totalExpense = uiState.overview.totalExpense,
                savingsRate = uiState.overview.savingsRate,
                currency = uiState.currency
            )
        }

        // Daily Pace Budget Alert & Spending Guard
        item {
            DailyPaceBudgetCard(
                overview = uiState.overview,
                currency = uiState.currency
            )
        }

        // Quick-Add Transaction Shortcuts Bar
        item {
            QuickAddShortcutsBar(
                currency = uiState.currency,
                templates = uiState.quickAddTemplates,
                onApplyTemplate = onQuickAddTemplate,
                onCustomizeClick = onCustomizeQuickAdd
            )
        }

        // Budget vs Income & Overall Spending Status Card
        item {
            BudgetIncomeSummaryCard(
                overview = uiState.overview,
                currency = uiState.currency,
                onManageBudgetsClick = onManageCategoriesClick
            )
        }

        // Monthly Expense Audit & Root Cause Analysis Notification Banner
        item {
            MonthlyAuditNotificationBanner(
                auditReport = uiState.overview.monthlyAuditReport,
                currency = uiState.currency,
                onViewFullAuditClick = onViewFullAuditClick
            )
        }

        // Donut Chart - Spending Distribution
        item {
            InteractiveDonutChart(
                spendingList = uiState.overview.categorySpendingList,
                totalExpense = uiState.overview.totalExpense,
                currency = uiState.currency
            )
        }

        // 7-Day Trend Bar Chart
        item {
            DailySpendingBarChart(
                points = uiState.overview.dailyChartPoints,
                currency = uiState.currency
            )
        }

        // Budget Alerts & Progress Gauges (Top 3 with budgets)
        val budgetedCategories = uiState.overview.categorySpendingList.filter { it.budgetLimit > 0 }.take(3)
        if (budgetedCategories.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Category Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onManageCategoriesClick) {
                        Text("Manage Budgets", color = Emerald700)
                    }
                }
            }

            items(budgetedCategories, key = { "budget_${it.categoryId}" }) { catSpending ->
                CategoryBudgetItem(
                    item = catSpending,
                    onClick = onManageCategoriesClick,
                    currency = uiState.currency
                )
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onViewAllTransactionsClick,
                    modifier = Modifier.testTag("view_all_transactions_btn")
                ) {
                    Text("View All (${uiState.overview.totalTransactionCount})", color = Emerald700)
                }
            }
        }

        // Recent Transactions Items (top 5)
        val recentTransactions = uiState.transactions.take(5)
        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.finance_hero),
                            contentDescription = "Financial Overview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No transactions found in this range",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track your daily income, expenses, and monthly budgets with full local privacy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onAddTransactionClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald700)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Transaction")
                        }
                    }
                }
            }
        } else {
            items(recentTransactions, key = { it.transaction.id }) { item ->
                TransactionItemCard(
                    item = item,
                    onEditClick = { onEditTransaction(item.transaction) },
                    onDeleteClick = { onDeleteTransaction(item.transaction) },
                    currency = uiState.currency,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialYear = uiState.specificYear,
            initialMonth = uiState.specificMonth,
            onDismiss = { showMonthPicker = false },
            onSelectMonthYear = { year, month ->
                onSpecificMonthSelected(year, month)
            }
        )
    }

    if (showCustomRangePicker) {
        CustomDateRangePickerDialog(
            initialStartDate = uiState.customStartDate,
            initialEndDate = uiState.customEndDate,
            onDismiss = { showCustomRangePicker = false },
            onSelectDateRange = { start, end ->
                onCustomDateRangeSelected(start, end)
            }
        )
    }
}
