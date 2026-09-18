/*
 * Copyright (c) 2026 FahimShahrier. All rights reserved.
 * Developed by FahimShahrier
 */

package com.example.ui.model

import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.TransactionWithCategory
import java.util.Locale

enum class RootCauseType {
    UNBUDGETED_EMERGENCY,
    CATEGORY_OVERAGE,
    AD_HOC_SPENDING,
    RECURRING_OVERHEAD
}

enum class AuditSeverity(val label: String) {
    CRITICAL("Critical Driver"),
    HIGH("High Impact"),
    MODERATE("Moderate"),
    LOW("Minor")
}

data class AuditCategoryVariance(
    val categoryId: Long,
    val categoryName: String,
    val iconName: String,
    val colorHex: Long,
    val budgetLimit: Double,
    val actualSpent: Double,
    val variance: Double, // actualSpent - budgetLimit
    val isOverBudget: Boolean,
    val overageAmount: Double,
    val savingsAmount: Double,
    val percentUsed: Float
)

data class UnbudgetedExpenseItem(
    val transactionId: Long,
    val title: String,
    val amount: Double,
    val categoryId: Long?,
    val categoryName: String,
    val dateTimestamp: Long,
    val note: String,
    val paymentMethod: String,
    val isFlaggedAsEmergency: Boolean
)

data class AuditRootCause(
    val title: String,
    val description: String,
    val impactAmount: Double,
    val type: RootCauseType,
    val severity: AuditSeverity
)

data class MonthlyExpenseAuditReport(
    val periodName: String = "This Month",
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val actualAccountBalance: Double = 0.0, // totalIncome - totalSpent
    val budgetHeadroom: Double = 0.0, // totalBudget - budgetedCategoriesSpent
    val withoutCategorySpent: Double = 0.0,
    val withoutCategoryCount: Int = 0,
    val budgetedCategoriesSpent: Double = 0.0,
    val netVariance: Double = 0.0, // totalSpent - totalBudget
    val isOverBudget: Boolean = false,
    val overageAmount: Double = 0.0,
    val savingsAmount: Double = 0.0,
    val budgetedCategoriesVariance: List<AuditCategoryVariance> = emptyList(),
    val overBudgetCategories: List<AuditCategoryVariance> = emptyList(),
    val underBudgetCategories: List<AuditCategoryVariance> = emptyList(),
    val unbudgetedExpenses: List<UnbudgetedExpenseItem> = emptyList(),
    val totalUnbudgetedSpent: Double = 0.0,
    val primaryRootCauses: List<AuditRootCause> = emptyList(),
    val generatedInsightSummary: String = "",
    val financialHealthScore: Int = 100,
    val recommendations: List<String> = emptyList(),
    val topOverageCategory: AuditCategoryVariance? = null,
    val largestUnbudgetedExpense: UnbudgetedExpenseItem? = null
)

object MonthlyAuditAnalyzer {

    private val EMERGENCY_KEYWORDS = listOf(
        "emergency", "urgent", "hospital", "doctor", "dental", "clinic",
        "repair", "plumb", "mechanic", "breakdown", "accident", "fine",
        "penalty", "adhoc", "unplanned", "unbudgeted", "crisis", "leak", "fix", "buffer"
    )

    fun analyzeMonthlyExpenses(
        transactions: List<TransactionWithCategory>,
        categories: List<CategoryEntity>,
        totalIncome: Double,
        currency: AppCurrency,
        periodName: String = "This Month"
    ): MonthlyExpenseAuditReport {
        val expenseCategories = categories.filter { it.type == "EXPENSE" }
        val categoryMap = categories.associateBy { it.id }

        // Filter expense transactions
        val expenseTransactions = transactions.filter { it.transaction.type == "EXPENSE" }

        // Calculate spending per category
        val categorySpendingMap = mutableMapOf<Long?, Double>()
        for (item in expenseTransactions) {
            val catId = item.transaction.categoryId
            categorySpendingMap[catId] = (categorySpendingMap[catId] ?: 0.0) + item.transaction.amount
        }

        val totalSpent = expenseTransactions.sumOf { it.transaction.amount }
        val actualAccountBalance = totalIncome - totalSpent

        // Without Category (Financial Buffer) spending
        val uncategorizedExpenses = expenseTransactions.filter { it.transaction.categoryId == null }
        val withoutCategorySpent = uncategorizedExpenses.sumOf { it.transaction.amount }
        val withoutCategoryCount = uncategorizedExpenses.size

        // Partition categories into budgeted and unbudgeted
        val budgetedCategories = expenseCategories.filter { it.budgetLimit > 0 }
        val budgetedCategoryIds = budgetedCategories.map { it.id }.toSet()
        val budgetedCategoriesSpent = expenseTransactions
            .filter { it.transaction.categoryId != null && budgetedCategoryIds.contains(it.transaction.categoryId) }
            .sumOf { it.transaction.amount }

        val totalBudget = budgetedCategories.sumOf { it.budgetLimit }
        val budgetHeadroom = if (totalBudget > 0) (totalBudget - budgetedCategoriesSpent).coerceAtLeast(0.0) else 0.0

        // 1. Variance Breakdown
        val allVariances = budgetedCategories.map { cat ->
            val spent = categorySpendingMap[cat.id] ?: 0.0
            val variance = spent - cat.budgetLimit
            val isOver = spent > cat.budgetLimit
            val overage = if (isOver) spent - cat.budgetLimit else 0.0
            val savings = if (!isOver) cat.budgetLimit - spent else 0.0
            val percent = if (cat.budgetLimit > 0) (spent / cat.budgetLimit).toFloat() else 0f

            AuditCategoryVariance(
                categoryId = cat.id,
                categoryName = cat.name,
                iconName = cat.iconName,
                colorHex = cat.colorHex,
                budgetLimit = cat.budgetLimit,
                actualSpent = spent,
                variance = variance,
                isOverBudget = isOver,
                overageAmount = overage,
                savingsAmount = savings,
                percentUsed = percent
            )
        }.toMutableList()

        // Include Without-Category (Buffer) in variance breakdown so total category sum matches totalSpent
        if (withoutCategorySpent > 0) {
            allVariances.add(
                AuditCategoryVariance(
                    categoryId = -1L,
                    categoryName = "Without Category (Buffer)",
                    iconName = "category",
                    colorHex = 0xFF78909CL,
                    budgetLimit = 0.0,
                    actualSpent = withoutCategorySpent,
                    variance = withoutCategorySpent,
                    isOverBudget = false, // Informational buffer
                    overageAmount = withoutCategorySpent,
                    savingsAmount = 0.0,
                    percentUsed = 1.0f
                )
            )
        }

        allVariances.sortByDescending { it.actualSpent }

        val overBudgetCategories = allVariances.filter { it.isOverBudget }.sortedByDescending { it.overageAmount }
        val underBudgetCategories = allVariances.filter { !it.isOverBudget && it.budgetLimit > 0 }.sortedByDescending { it.savingsAmount }

        // 2. Unbudgeted & Ad-Hoc Expense Detection
        val unbudgetedList = mutableListOf<UnbudgetedExpenseItem>()
        for (item in expenseTransactions) {
            val t = item.transaction
            val cat = if (t.categoryId != null) categoryMap[t.categoryId] else null
            val isUnbudgetedCat = cat == null || cat.budgetLimit <= 0.0
            val noteLower = t.note.lowercase(Locale.ROOT)
            val titleLower = t.title.lowercase(Locale.ROOT)

            val isEmergencyKeyword = EMERGENCY_KEYWORDS.any { kw ->
                titleLower.contains(kw) || noteLower.contains(kw)
            }

            if (isUnbudgetedCat || isEmergencyKeyword) {
                unbudgetedList.add(
                    UnbudgetedExpenseItem(
                        transactionId = t.id,
                        title = t.title,
                        amount = t.amount,
                        categoryId = t.categoryId,
                        categoryName = cat?.name ?: "Without Category (Buffer)",
                        dateTimestamp = t.dateTimestamp,
                        note = t.note,
                        paymentMethod = t.paymentMethod,
                        isFlaggedAsEmergency = isEmergencyKeyword
                    )
                )
            }
        }

        unbudgetedList.sortByDescending { it.amount }
        val totalUnbudgetedSpent = unbudgetedList.sumOf { it.amount }

        val isOverBudget = totalBudget > 0 && totalSpent > totalBudget
        val netVariance = totalSpent - totalBudget
        val overageAmount = if (isOverBudget) totalSpent - totalBudget else 0.0
        // Real savings: aligns with actual account balance when income is tracked
        val savingsAmount = if (totalIncome > 0) actualAccountBalance.coerceAtLeast(0.0) else if (!isOverBudget && totalBudget > 0) totalBudget - totalSpent else 0.0

        val topOverageCat = overBudgetCategories.firstOrNull()
        val largestUnbudgeted = unbudgetedList.firstOrNull()

        // 3. Root Cause Isolation
        val rootCauses = mutableListOf<AuditRootCause>()

        // Add without-category buffer root cause if significant
        if (withoutCategorySpent > 0) {
            val severity = if (withoutCategorySpent > totalSpent * 0.25) {
                AuditSeverity.CRITICAL
            } else if (withoutCategorySpent > totalSpent * 0.1) {
                AuditSeverity.HIGH
            } else {
                AuditSeverity.MODERATE
            }

            rootCauses.add(
                AuditRootCause(
                    title = "Without-Category Spending (Buffer)",
                    description = "Spent ${CurrencyHelper.format(withoutCategorySpent, currency)} across $withoutCategoryCount transaction(s) without an assigned category. Directly impacts real account balance (${CurrencyHelper.format(actualAccountBalance, currency)} remaining).",
                    impactAmount = withoutCategorySpent,
                    type = RootCauseType.AD_HOC_SPENDING,
                    severity = severity
                )
            )
        }

        // Check for emergency / unbudgeted spikes
        if (largestUnbudgeted != null && largestUnbudgeted.amount > 0 && largestUnbudgeted.categoryId != null) {
            val severity = if (largestUnbudgeted.amount > totalBudget * 0.25 || isOverBudget) {
                AuditSeverity.CRITICAL
            } else if (largestUnbudgeted.amount > totalBudget * 0.1) {
                AuditSeverity.HIGH
            } else {
                AuditSeverity.MODERATE
            }

            rootCauses.add(
                AuditRootCause(
                    title = if (largestUnbudgeted.isFlaggedAsEmergency) "Unbudgeted Emergency Expense" else "Unbudgeted Ad-Hoc Cost",
                    description = "'${largestUnbudgeted.title}' in ${largestUnbudgeted.categoryName} had no dedicated budget buffer.",
                    impactAmount = largestUnbudgeted.amount,
                    type = if (largestUnbudgeted.isFlaggedAsEmergency) RootCauseType.UNBUDGETED_EMERGENCY else RootCauseType.AD_HOC_SPENDING,
                    severity = severity
                )
            )
        }

        // Add top over-budget categories
        for (overCat in overBudgetCategories.take(3)) {
            val severity = if (overCat.overageAmount > totalBudget * 0.15) {
                AuditSeverity.CRITICAL
            } else if (overCat.overageAmount > totalBudget * 0.05) {
                AuditSeverity.HIGH
            } else {
                AuditSeverity.MODERATE
            }

            rootCauses.add(
                AuditRootCause(
                    title = "${overCat.categoryName} Budget Exceeded",
                    description = "Spent ${CurrencyHelper.format(overCat.actualSpent, currency)} against a ${CurrencyHelper.format(overCat.budgetLimit, currency)} budget (${String.format(Locale.US, "%.0f%%", overCat.percentUsed * 100)} utilized).",
                    impactAmount = overCat.overageAmount,
                    type = RootCauseType.CATEGORY_OVERAGE,
                    severity = severity
                )
            )
        }

        // 4. Natural Language Insight Summary Generation
        val generatedInsight = buildInsightSummary(
            isOverBudget = isOverBudget,
            overageAmount = overageAmount,
            savingsAmount = savingsAmount,
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            totalIncome = totalIncome,
            actualAccountBalance = actualAccountBalance,
            budgetHeadroom = budgetHeadroom,
            withoutCategorySpent = withoutCategorySpent,
            withoutCategoryCount = withoutCategoryCount,
            topOverageCat = topOverageCat,
            overBudgetCategories = overBudgetCategories,
            largestUnbudgeted = largestUnbudgeted,
            unbudgetedList = unbudgetedList,
            currency = currency,
            periodName = periodName
        )

        // 5. Financial Health Score (0 - 100)
        var score = 100
        if (totalBudget > 0) {
            val budgetRatio = totalSpent / totalBudget
            if (budgetRatio > 1.0) {
                val penalty = ((budgetRatio - 1.0) * 80).toInt().coerceIn(10, 60)
                score -= penalty
            }
        }
        if (overBudgetCategories.isNotEmpty()) {
            score -= (overBudgetCategories.size * 5).coerceAtMost(20)
        }
        if (totalUnbudgetedSpent > (totalSpent * 0.2)) {
            score -= 15
        }
        if (totalIncome > 0 && actualAccountBalance < 0) {
            score -= 20
        }
        score = score.coerceIn(10, 100)

        // 6. Actionable Recommendations
        val recommendations = mutableListOf<String>()
        if (isOverBudget) {
            if (largestUnbudgeted != null) {
                recommendations.add("Create an 'Emergency Fund' category with at least ${CurrencyHelper.format(largestUnbudgeted.amount * 0.75, currency)} monthly buffer.")
            }
            if (topOverageCat != null) {
                val suggestedLimit = topOverageCat.actualSpent * 1.1
                recommendations.add("Consider adjusting '${topOverageCat.categoryName}' budget up to ${CurrencyHelper.formatCompact(suggestedLimit, currency)} based on actual demand.")
            }
            if (overBudgetCategories.size > 1) {
                recommendations.add("Review the top ${overBudgetCategories.size} over-budget categories to trim discretionary leisure expenses.")
            }
        } else {
            if (totalIncome > 0) {
                val formattedBalance = CurrencyHelper.format(actualAccountBalance, currency)
                val formattedMargin = CurrencyHelper.format(if (totalBudget > totalSpent) totalBudget - totalSpent else budgetHeadroom, currency)
                recommendations.add("Maintain current discipline! You have $formattedBalance in actual net savings in your account (with $formattedMargin budget headroom).")
            } else {
                recommendations.add("Maintain current discipline! You have ${CurrencyHelper.format(savingsAmount, currency)} unallocated savings this month.")
            }

            if (withoutCategorySpent > 0) {
                val formattedBuffer = CurrencyHelper.format(withoutCategorySpent, currency)
                recommendations.add("You spent $formattedBuffer across $withoutCategoryCount transaction(s) without categories. Create a dedicated 'Buffer / Misc' category to keep your budget completely aligned.")
            }

            if (underBudgetCategories.isNotEmpty()) {
                val topSaved = underBudgetCategories.first()
                recommendations.add("Great savings in '${topSaved.categoryName}' (${CurrencyHelper.format(topSaved.savingsAmount, currency)} saved). Consider redirecting savings into investments.")
            }
        }

        return MonthlyExpenseAuditReport(
            periodName = periodName,
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            totalIncome = totalIncome,
            actualAccountBalance = actualAccountBalance,
            budgetHeadroom = budgetHeadroom,
            withoutCategorySpent = withoutCategorySpent,
            withoutCategoryCount = withoutCategoryCount,
            budgetedCategoriesSpent = budgetedCategoriesSpent,
            netVariance = netVariance,
            isOverBudget = isOverBudget,
            overageAmount = overageAmount,
            savingsAmount = savingsAmount,
            budgetedCategoriesVariance = allVariances,
            overBudgetCategories = overBudgetCategories,
            underBudgetCategories = underBudgetCategories,
            unbudgetedExpenses = unbudgetedList,
            totalUnbudgetedSpent = totalUnbudgetedSpent,
            primaryRootCauses = rootCauses,
            generatedInsightSummary = generatedInsight,
            financialHealthScore = score,
            recommendations = recommendations,
            topOverageCategory = topOverageCat,
            largestUnbudgetedExpense = largestUnbudgeted
        )
    }

    private fun buildInsightSummary(
        isOverBudget: Boolean,
        overageAmount: Double,
        savingsAmount: Double,
        totalBudget: Double,
        totalSpent: Double,
        totalIncome: Double,
        actualAccountBalance: Double,
        budgetHeadroom: Double,
        withoutCategorySpent: Double,
        withoutCategoryCount: Int,
        topOverageCat: AuditCategoryVariance?,
        overBudgetCategories: List<AuditCategoryVariance>,
        largestUnbudgeted: UnbudgetedExpenseItem?,
        unbudgetedList: List<UnbudgetedExpenseItem>,
        currency: AppCurrency,
        periodName: String
    ): String {
        if (totalBudget <= 0) {
            return "You spent ${CurrencyHelper.format(totalSpent, currency)} across ${unbudgetedList.size} categories. Set up monthly category budgets to enable automated root-cause overage audits."
        }

        if (isOverBudget) {
            val formattedOverage = CurrencyHelper.format(overageAmount, currency)

            return when {
                largestUnbudgeted != null && topOverageCat != null -> {
                    val unbudgetedDesc = if (largestUnbudgeted.isFlaggedAsEmergency) "an unbudgeted ${CurrencyHelper.format(largestUnbudgeted.amount, currency)} emergency expense" else "an unbudgeted ${CurrencyHelper.format(largestUnbudgeted.amount, currency)} (${largestUnbudgeted.title}) expense"
                    val overageDesc = "exceeding your ${topOverageCat.categoryName} category by ${CurrencyHelper.format(topOverageCat.overageAmount, currency)}"
                    "You went $formattedOverage over budget this month, driven primarily by $unbudgetedDesc and $overageDesc."
                }
                largestUnbudgeted != null -> {
                    val unbudgetedDesc = if (largestUnbudgeted.isFlaggedAsEmergency) "unbudgeted emergency costs totaling ${CurrencyHelper.format(largestUnbudgeted.amount, currency)} ('${largestUnbudgeted.title}')" else "unbudgeted ad-hoc spending totaling ${CurrencyHelper.format(largestUnbudgeted.amount, currency)}"
                    "You went $formattedOverage over budget this month, driven primarily by $unbudgetedDesc."
                }
                overBudgetCategories.size >= 2 -> {
                    val first = overBudgetCategories[0]
                    val second = overBudgetCategories[1]
                    "You went $formattedOverage over budget this month, driven primarily by exceeding ${first.categoryName} by ${CurrencyHelper.format(first.overageAmount, currency)} and ${second.categoryName} by ${CurrencyHelper.format(second.overageAmount, currency)}."
                }
                topOverageCat != null -> {
                    "You went $formattedOverage over budget this month, driven primarily by exceeding your ${topOverageCat.categoryName} category by ${CurrencyHelper.format(topOverageCat.overageAmount, currency)}."
                }
                else -> {
                    "You went $formattedOverage over budget this month across multiple minor category variances."
                }
            }
        } else {
            val remainingBudget = if (totalBudget > totalSpent) totalBudget - totalSpent else budgetHeadroom
            val formattedBudgetRemaining = CurrencyHelper.format(remainingBudget, currency)

            if (totalIncome > 0) {
                val formattedBalance = CurrencyHelper.format(actualAccountBalance, currency)
                return if (withoutCategorySpent > 0) {
                    val formattedBuffer = CurrencyHelper.format(withoutCategorySpent, currency)
                    "Great job! You stayed $formattedBudgetRemaining under your planned budget, keeping $formattedBalance in actual account savings ($formattedBuffer spent as without-category buffer)."
                } else if (overBudgetCategories.isNotEmpty()) {
                    val topOver = overBudgetCategories.first()
                    "You stayed $formattedBudgetRemaining under overall budget with $formattedBalance actual account balance, though ${topOver.categoryName} exceeded target by ${CurrencyHelper.format(topOver.overageAmount, currency)}."
                } else {
                    "Great job! You stayed $formattedBudgetRemaining under your monthly budget, maintaining healthy spending limits and $formattedBalance in actual net savings."
                }
            } else {
                val formattedSavings = CurrencyHelper.format(savingsAmount, currency)
                return if (overBudgetCategories.isNotEmpty()) {
                    val topOver = overBudgetCategories.first()
                    "You stayed $formattedSavings under overall budget this month, though ${topOver.categoryName} exceeded its target by ${CurrencyHelper.format(topOver.overageAmount, currency)}."
                } else {
                    "Great job! You stayed $formattedSavings under your monthly budget, maintaining healthy spending limits across all categories."
                }
            }
        }
    }
}
