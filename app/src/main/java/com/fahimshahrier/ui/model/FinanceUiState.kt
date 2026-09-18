package com.fahimshahrier.ui.model

import androidx.compose.ui.graphics.Color
import com.fahimshahrier.data.export.SnapshotItem
import com.fahimshahrier.data.local.entity.CategoryEntity
import com.fahimshahrier.data.local.entity.TransactionWithCategory

data class CategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val iconName: String,
    val color: Color,
    val totalAmount: Double,
    val percentage: Float, // 0f to 100f
    val transactionCount: Int,
    val budgetLimit: Double
) {
    val isOverBudget: Boolean get() = budgetLimit > 0 && totalAmount > budgetLimit
    val budgetProgress: Float get() = if (budgetLimit > 0) (totalAmount / budgetLimit).toFloat() else 0f
    val moneyLeft: Double get() = if (budgetLimit > 0) budgetLimit - totalAmount else 0.0
}

data class DailyChartPoint(
    val dayLabel: String, // "Mon", "Tue" or "Aug 15"
    val timestamp: Long,
    val expenseAmount: Double,
    val incomeAmount: Double
)

data class FinanceOverview(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val savingsRate: Double = 0.0, // (Income - Expense) / Income * 100
    val totalBudget: Double = 0.0, // Sum of all category budgets
    val totalBudgetSpent: Double = 0.0, // Total expense on budgeted categories
    val categorySpendingList: List<CategorySpending> = emptyList(),
    val dailyChartPoints: List<DailyChartPoint> = emptyList(),
    val totalTransactionCount: Int = 0,
    val monthlyAuditReport: MonthlyExpenseAuditReport = MonthlyExpenseAuditReport()
) {
    // Budget vs Income Analysis
    val budgetVsIncomeDifference: Double get() = totalIncome - totalBudget
    val isBudgetExceedingIncome: Boolean get() = totalBudget > totalIncome && totalIncome > 0
    val extraIncomeAfterBudget: Double get() = if (totalIncome >= totalBudget) totalIncome - totalBudget else 0.0
    val budgetExceedsIncomeBy: Double get() = if (totalBudget > totalIncome) totalBudget - totalIncome else 0.0

    // Actual Expense vs Budget Analysis
    val actualBudgetRemaining: Double get() = totalBudget - totalExpense
    val isActualSpendingOverBudget: Boolean get() = totalBudget > 0 && totalExpense > totalBudget
    val extraMoneyLeftFromBudget: Double get() = if (totalBudget >= totalExpense) totalBudget - totalExpense else 0.0
    val budgetExceededByAmount: Double get() = if (totalExpense > totalBudget) totalExpense - totalBudget else 0.0
    val overallBudgetProgress: Float get() = if (totalBudget > 0) (totalExpense / totalBudget).toFloat().coerceIn(0f, 2f) else 0f
}

data class FinanceUiState(
    val isLoading: Boolean = false,
    val currency: AppCurrency = CurrencyList.BDT,
    val timeRange: TimeRange = TimeRange.THIS_MONTH,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val specificYear: Int? = null,
    val specificMonth: Int? = null,
    val periodLabel: String = "This Month",
    val typeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val selectedCategoryId: Long? = null,
    val selectedPaymentMethod: String? = null,
    val onlyRecurring: Boolean = false,
    val searchQuery: String = "",
    val overview: FinanceOverview = FinanceOverview(),
    val transactions: List<TransactionWithCategory> = emptyList(),
    val monthlyTransactions: List<TransactionWithCategory> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val quickAddTemplates: List<com.fahimshahrier.data.model.QuickAddTemplate> = com.fahimshahrier.data.model.QuickTemplatesHelper.defaultPresets,
    val snapshots: List<SnapshotItem> = emptyList(),
    val userMessage: String? = null
)
