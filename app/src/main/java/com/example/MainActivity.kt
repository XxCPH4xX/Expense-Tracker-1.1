/*
 * Copyright (c) 2026 FahimShahrier. All rights reserved.
 * Developed by FahimShahrier
 */

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.QuickAddTemplate
import com.example.ui.components.AppLockOverlay
import com.example.ui.components.CategoryDialog
import com.example.ui.components.CustomizeShortcutsDialog
import com.example.ui.components.MonthlyExpenseAuditDialog
import com.example.ui.components.TransactionDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExportBackupScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald700
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    TRANSACTIONS("Records", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_transactions"),
    ANALYTICS("Analytics", Icons.Filled.Assessment, Icons.Outlined.Assessment, "nav_analytics"),
    CATEGORIES("Budgets", Icons.Filled.Category, Icons.Outlined.Category, "nav_categories"),
    BACKUP("Export", Icons.Filled.Storage, Icons.Outlined.Storage, "nav_backup")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FinanceAppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAppRoot(
    viewModel: FinanceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
    val appLockPin by viewModel.appLockPin.collectAsStateWithLifecycle()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    // Dialog States
    var showTransactionDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showAuditDialog by remember { mutableStateOf(false) }
    var showCustomizeShortcutsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    if (isAppLockEnabled && !isAppUnlocked) {
        AppLockOverlay(
            storedPin = appLockPin,
            onUnlockSuccess = viewModel::unlockApp
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            NavigationTab.DASHBOARD -> "Expense Tracker"
                            NavigationTab.TRANSACTIONS -> "All Transactions"
                            NavigationTab.ANALYTICS -> "Financial Insights"
                            NavigationTab.CATEGORIES -> "Categories & Budgets"
                            NavigationTab.BACKUP -> "Data & Backup"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald700,
                            selectedTextColor = Emerald700,
                            indicatorColor = Emerald700.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == NavigationTab.DASHBOARD || currentTab == NavigationTab.TRANSACTIONS) {
                FloatingActionButton(
                    onClick = {
                        editingTransaction = null
                        showTransactionDialog = true
                    },
                    containerColor = Emerald700,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_transaction")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            } else if (currentTab == NavigationTab.CATEGORIES) {
                FloatingActionButton(
                    onClick = {
                        editingCategory = null
                        showCategoryDialog = true
                    },
                    containerColor = Emerald700,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_category")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    (slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetX = { fullWidth -> if (forward) fullWidth / 4 else -fullWidth / 4 }
                    ) + fadeIn(
                        animationSpec = tween(200)
                    )).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(160),
                            targetOffsetX = { fullWidth -> if (forward) -fullWidth / 4 else fullWidth / 4 }
                        ) + fadeOut(
                            animationSpec = tween(160)
                        )
                    )
                },
                label = "tab_content_transition"
            ) { targetTab ->
                when (targetTab) {
                    NavigationTab.DASHBOARD -> {
                        DashboardScreen(
                            uiState = uiState,
                            onTimeRangeSelected = viewModel::setTimeRange,
                            onSpecificMonthSelected = viewModel::setSpecificMonth,
                            onCustomDateRangeSelected = viewModel::setCustomDateRange,
                            onAddTransactionClick = {
                                editingTransaction = null
                                showTransactionDialog = true
                            },
                            onViewAllTransactionsClick = {
                                currentTab = NavigationTab.TRANSACTIONS
                            },
                            onEditTransaction = { trans ->
                                editingTransaction = trans
                                showTransactionDialog = true
                            },
                            onDeleteTransaction = { trans ->
                                transactionToDelete = trans
                            },
                            onManageCategoriesClick = {
                                currentTab = NavigationTab.CATEGORIES
                            },
                            onViewFullAuditClick = {
                                showAuditDialog = true
                            },
                            onCustomizeQuickAdd = {
                                showCustomizeShortcutsDialog = true
                            },
                            onQuickAddTemplate = { template ->
                                val matchedCat = allCategories.find { it.name.equals(template.categoryName, ignoreCase = true) }
                                editingTransaction = TransactionEntity(
                                    id = 0L,
                                    title = template.title,
                                    amount = template.amount,
                                    type = template.type,
                                    categoryId = matchedCat?.id,
                                    dateTimestamp = System.currentTimeMillis(),
                                    note = "Quick log",
                                    paymentMethod = template.paymentMethod,
                                    isRecurring = false
                                )
                                showTransactionDialog = true
                            }
                        )
                    }

                    NavigationTab.TRANSACTIONS -> {
                        TransactionsScreen(
                            uiState = uiState,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onTimeRangeSelected = viewModel::setTimeRange,
                            onSpecificMonthSelected = viewModel::setSpecificMonth,
                            onCustomDateRangeSelected = viewModel::setCustomDateRange,
                            onTypeFilterChange = viewModel::setTypeFilter,
                            onCategoryFilterChange = viewModel::setSelectedCategory,
                            onPaymentMethodFilterChange = viewModel::setSelectedPaymentMethod,
                            onOnlyRecurringFilterChange = viewModel::setOnlyRecurring,
                            onEditTransaction = { trans ->
                                editingTransaction = trans
                                showTransactionDialog = true
                            },
                            onDeleteTransaction = { trans ->
                                transactionToDelete = trans
                            }
                        )
                    }

                    NavigationTab.ANALYTICS -> {
                        AnalyticsScreen(
                            uiState = uiState,
                            onTimeRangeSelected = viewModel::setTimeRange,
                            onSpecificMonthSelected = viewModel::setSpecificMonth,
                            onCustomDateRangeSelected = viewModel::setCustomDateRange,
                            onViewFullAuditClick = {
                                showAuditDialog = true
                            }
                        )
                    }

                    NavigationTab.CATEGORIES -> {
                        CategoriesScreen(
                            uiState = uiState,
                            onCreateCategoryClick = {
                                editingCategory = null
                                showCategoryDialog = true
                            },
                            onEditCategory = { cat ->
                                editingCategory = cat
                                showCategoryDialog = true
                            },
                            onDeleteCategory = viewModel::deleteCategory,
                            onEditTransaction = { trans ->
                                editingTransaction = trans
                                showTransactionDialog = true
                            },
                            onDeleteTransaction = { trans ->
                                transactionToDelete = trans
                            },
                            onAddTransactionForCategory = { cat ->
                                editingTransaction = TransactionEntity(
                                    id = 0L,
                                    title = "",
                                    amount = 0.0,
                                    type = cat.type,
                                    categoryId = cat.id,
                                    dateTimestamp = System.currentTimeMillis(),
                                    note = "",
                                    paymentMethod = "bKash",
                                    isRecurring = false
                                )
                                showTransactionDialog = true
                            },
                            onSpecificMonthSelected = viewModel::setSpecificMonth,
                            onTimeRangeSelected = viewModel::setTimeRange
                        )
                    }

                    NavigationTab.BACKUP -> {
                        ExportBackupScreen(
                            uiState = uiState,
                            onSetCurrency = viewModel::setBaseCurrency,
                            onExportCsv = viewModel::exportCsv,
                            onExportJson = viewModel::exportJson,
                            onImportData = viewModel::importData,
                            onCreateSnapshot = viewModel::createManualSnapshot,
                            onRestoreSnapshot = viewModel::restoreSnapshot,
                            onDeleteSnapshot = viewModel::deleteSnapshot,
                            onResetToSampleData = viewModel::resetToSampleData,
                            onClearAllData = viewModel::clearAllData,
                            isAppLockEnabled = isAppLockEnabled,
                            onSetAppLock = viewModel::setAppLock,
                            onCustomizeQuickAdd = {
                                showCustomizeShortcutsDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Transaction Dialog
    if (showTransactionDialog) {
        TransactionDialog(
            initialTransaction = editingTransaction,
            categories = allCategories,
            currency = uiState.currency,
            onDismiss = {
                showTransactionDialog = false
                editingTransaction = null
            },
            onSaveCategory = { name, iconName, colorHex, type, budgetLimit, onCreated ->
                viewModel.saveCategory(
                    id = 0L,
                    name = name,
                    iconName = iconName,
                    colorHex = colorHex,
                    type = type,
                    budgetLimit = budgetLimit,
                    onSuccess = onCreated
                )
            },
            onSave = { id, title, amount, type, categoryId, dateTimestamp, note, paymentMethod, isRecurring, receiptUri ->
                viewModel.saveTransaction(
                    id = id,
                    title = title,
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    dateTimestamp = dateTimestamp,
                    note = note,
                    paymentMethod = paymentMethod,
                    isRecurring = isRecurring,
                    receiptUri = receiptUri
                )
            }
        )
    }

    // Add / Edit Category Dialog
    if (showCategoryDialog) {
        CategoryDialog(
            initialCategory = editingCategory,
            currency = uiState.currency,
            onDismiss = {
                showCategoryDialog = false
                editingCategory = null
            },
            onSave = { id, name, iconName, colorHex, type, budgetLimit ->
                viewModel.saveCategory(
                    id = id,
                    name = name,
                    iconName = iconName,
                    colorHex = colorHex,
                    type = type,
                    budgetLimit = budgetLimit
                )
            }
        )
    }

    // Transaction Delete Dialog
    transactionToDelete?.let { trans ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction?") },
            text = { Text("Are you sure you want to delete '${trans.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(trans)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Monthly Expense Audit Dialog
    if (showAuditDialog) {
        MonthlyExpenseAuditDialog(
            auditReport = uiState.overview.monthlyAuditReport,
            currency = uiState.currency,
            onDismiss = { showAuditDialog = false }
        )
    }

    // Customizable 1-Tap Quick Log Dialog
    if (showCustomizeShortcutsDialog) {
        CustomizeShortcutsDialog(
            templates = uiState.quickAddTemplates,
            categories = allCategories,
            currency = uiState.currency,
            onSaveTemplate = viewModel::saveQuickAddTemplate,
            onDeleteTemplate = viewModel::deleteQuickAddTemplate,
            onResetDefaults = viewModel::resetQuickAddTemplates,
            onDismiss = { showCustomizeShortcutsDialog = false }
        )
    }
}
