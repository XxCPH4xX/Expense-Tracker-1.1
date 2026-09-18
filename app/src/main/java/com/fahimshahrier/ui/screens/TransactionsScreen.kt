package com.fahimshahrier.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.fahimshahrier.data.local.entity.TransactionEntity
import com.fahimshahrier.ui.components.CategoryIconHelper
import com.fahimshahrier.ui.components.CustomDateRangePickerDialog
import com.fahimshahrier.ui.components.MonthYearPickerDialog
import com.fahimshahrier.ui.components.TransactionItemCard
import com.fahimshahrier.ui.model.CurrencyHelper
import com.fahimshahrier.ui.model.FinanceUiState
import com.fahimshahrier.ui.model.TimeRange
import com.fahimshahrier.ui.model.TransactionTypeFilter
import com.fahimshahrier.ui.theme.Emerald700
import com.fahimshahrier.ui.theme.ExpenseRed
import com.fahimshahrier.ui.theme.IncomeGreen

@Composable
fun TransactionsScreen(
    uiState: FinanceUiState,
    onSearchQueryChange: (String) -> Unit,
    onTimeRangeSelected: (TimeRange) -> Unit,
    onSpecificMonthSelected: (year: Int, month: Int) -> Unit,
    onCustomDateRangeSelected: (startTimestamp: Long, endTimestamp: Long) -> Unit,
    onTypeFilterChange: (TransactionTypeFilter) -> Unit,
    onCategoryFilterChange: (Long?) -> Unit,
    onPaymentMethodFilterChange: (String?) -> Unit = {},
    onOnlyRecurringFilterChange: (Boolean) -> Unit = {},
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMonthPicker by remember { mutableStateOf(false) }
    var showCustomRangePicker by remember { mutableStateOf(false) }

    val totalFilteredAmount = uiState.transactions.sumOf {
        if (it.transaction.type == "EXPENSE") -it.transaction.amount else it.transaction.amount
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("transactions_screen")
    ) {
        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search transactions, notes, merchant...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("transaction_search_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // Time Filter Horizontal Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TimeRange.values()) { range ->
                val isSelected = uiState.timeRange == range
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        when (range) {
                            TimeRange.SPECIFIC_MONTH -> {
                                showMonthPicker = true
                            }
                            TimeRange.CUSTOM_RANGE -> {
                                showCustomRangePicker = true
                            }
                            else -> {
                                onTimeRangeSelected(range)
                            }
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
                    ),
                    modifier = Modifier.testTag("time_filter_${range.name}")
                )
            }
        }

        // Type Tabs: All, Expenses, Income
        TabRow(
            selectedTabIndex = uiState.typeFilter.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Emerald700,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            TransactionTypeFilter.values().forEach { filter ->
                Tab(
                    selected = uiState.typeFilter == filter,
                    onClick = { onTypeFilterChange(filter) },
                    text = {
                        Text(
                            text = filter.displayName,
                            fontWeight = if (uiState.typeFilter == filter) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("filter_tab_${filter.name}")
                )
            }
        }

        // Category Filter Horizontal Row (All, Uncategorized, Categories)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All Categories" Chip
            item {
                val isAllSelected = uiState.selectedCategoryId == null
                FilterChip(
                    selected = isAllSelected,
                    onClick = { onCategoryFilterChange(null) },
                    label = { Text("All Categories") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald700,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("category_filter_all")
                )
            }

            // "Uncategorized" Chip (represented by -1L in ViewModel)
            item {
                val isUncatSelected = uiState.selectedCategoryId == -1L
                val uncatColor = Color(0xFF78909C)
                FilterChip(
                    selected = isUncatSelected,
                    onClick = {
                        onCategoryFilterChange(if (isUncatSelected) null else -1L)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = if (isUncatSelected) Color.White else uncatColor,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Uncategorized") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = uncatColor,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("category_filter_uncategorized")
                )
            }

            items(uiState.categories, key = { it.id }) { cat ->
                val isSelected = uiState.selectedCategoryId == cat.id
                val catColor = CategoryIconHelper.parseColor(cat.colorHex)

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onCategoryFilterChange(if (isSelected) null else cat.id)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = CategoryIconHelper.getIcon(cat.iconName),
                            contentDescription = null,
                            tint = if (isSelected) Color.White else catColor,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(cat.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = catColor,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("category_filter_${cat.id}")
                )
            }
        }

        // Payment Method & Recurring Filter Row
        val paymentMethods = listOf("bKash", "Nagad", "Card", "Cash", "Bank Transfer")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recurring Toggle Chip
            item {
                FilterChip(
                    selected = uiState.onlyRecurring,
                    onClick = { onOnlyRecurringFilterChange(!uiState.onlyRecurring) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (uiState.onlyRecurring) Color.White else MaterialTheme.colorScheme.primary
                        )
                    },
                    label = { Text("Recurring Only") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_recurring_only")
                )
            }

            // "All Methods" Chip
            item {
                val isAllMethod = uiState.selectedPaymentMethod == null
                FilterChip(
                    selected = isAllMethod,
                    onClick = { onPaymentMethodFilterChange(null) },
                    label = { Text("All Methods") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald700,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_method_all")
                )
            }

            items(paymentMethods) { method ->
                val isSelected = uiState.selectedPaymentMethod.equals(method, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onPaymentMethodFilterChange(if (isSelected) null else method)
                    },
                    label = { Text(method) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald700,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_method_$method")
                )
            }
        }

        // Summary Bar (Active Period, Count and Net total)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${uiState.transactions.size} records",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " • ${uiState.periodLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Net: ${if (totalFilteredAmount >= 0) "+" else "-"}${CurrencyHelper.format(kotlin.math.abs(totalFilteredAmount), uiState.currency)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (totalFilteredAmount >= 0) IncomeGreen else ExpenseRed
                )
            }
        }

        // Transactions List
        if (uiState.transactions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No matching transactions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Try adjusting your search query, type filter, or date range (${uiState.periodLabel}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.transactions, key = { it.transaction.id }) { item ->
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
    }

    // Month & Year Picker Dialog
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

    // Custom Date Range Picker Dialog
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
