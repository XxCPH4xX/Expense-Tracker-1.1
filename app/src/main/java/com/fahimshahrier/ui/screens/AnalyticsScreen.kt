package com.fahimshahrier.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahimshahrier.ui.components.BudgetIncomeSummaryCard
import com.fahimshahrier.ui.components.CategoryBudgetItem
import com.fahimshahrier.ui.components.CategoryIconHelper
import com.fahimshahrier.ui.components.CustomDateRangePickerDialog
import com.fahimshahrier.ui.components.DailySpendingBarChart
import com.fahimshahrier.ui.components.InteractiveDonutChart
import com.fahimshahrier.ui.components.MonthYearPickerDialog
import com.fahimshahrier.ui.components.MonthlyAuditNotificationBanner
import com.fahimshahrier.ui.model.CurrencyHelper
import com.fahimshahrier.ui.model.FinanceUiState
import com.fahimshahrier.ui.model.TimeRange
import com.fahimshahrier.ui.theme.Emerald700
import com.fahimshahrier.ui.theme.ExpenseRed
import com.fahimshahrier.ui.theme.IncomeGreen
import java.util.Locale

@Composable
fun AnalyticsScreen(
    uiState: FinanceUiState,
    onTimeRangeSelected: (TimeRange) -> Unit,
    onSpecificMonthSelected: (year: Int, month: Int) -> Unit = { _, _ -> },
    onCustomDateRangeSelected: (startTimestamp: Long, endTimestamp: Long) -> Unit = { _, _ -> },
    onViewFullAuditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMonthPicker by remember { mutableStateOf(false) }
    var showCustomRangePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Filter Chips
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
                                    tint = if (isSelected) Color.White else Emerald700
                                )
                                TimeRange.CUSTOM_RANGE -> Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Color.White else Emerald700
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
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Summary Key Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Income Metric
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = CurrencyHelper.format(uiState.overview.totalIncome, uiState.currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }

                // Total Expense Metric
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = CurrencyHelper.format(uiState.overview.totalExpense, uiState.currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }

        // Donut Chart
        item {
            InteractiveDonutChart(
                spendingList = uiState.overview.categorySpendingList,
                totalExpense = uiState.overview.totalExpense,
                currency = uiState.currency
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

        // Budget vs Income & Cash Flow Plan Card
        item {
            BudgetIncomeSummaryCard(
                overview = uiState.overview,
                currency = uiState.currency
            )
        }

        // Daily Trend Bar Chart
        item {
            DailySpendingBarChart(
                points = uiState.overview.dailyChartPoints,
                currency = uiState.currency
            )
        }

        // Category Breakdown Detailed List Header
        item {
            Text(
                text = "Detailed Category Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (uiState.overview.categorySpendingList.isEmpty()) {
            item {
                Text(
                    text = "No category data available for this range.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(uiState.overview.categorySpendingList, key = { "cat_analytics_${it.categoryId}" }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(item.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryIconHelper.getIcon(item.iconName),
                                        contentDescription = item.categoryName,
                                        tint = item.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${item.transactionCount} transaction${if (item.transactionCount != 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyHelper.format(item.totalAmount, uiState.currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f%% of total", item.percentage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Proportion Bar
                        LinearProgressIndicator(
                            progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = item.color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        if (item.budgetLimit > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val moneyLeft = item.moneyLeft
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Budget: ${CurrencyHelper.formatCompact(item.budgetLimit, uiState.currency)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (moneyLeft >= 0) IncomeGreen.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (moneyLeft >= 0) {
                                            "Money Left: ${CurrencyHelper.format(moneyLeft, uiState.currency)}"
                                        } else {
                                            "Over by: ${CurrencyHelper.format(-moneyLeft, uiState.currency)}"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (moneyLeft >= 0) IncomeGreen else ExpenseRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
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
