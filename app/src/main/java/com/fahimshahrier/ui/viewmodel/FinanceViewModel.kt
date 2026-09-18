package com.fahimshahrier.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fahimshahrier.data.export.DataExportImportHelper
import com.fahimshahrier.data.export.ImportParseResult
import com.fahimshahrier.data.export.SnapshotItem
import com.fahimshahrier.data.export.SnapshotManager
import com.fahimshahrier.data.local.AppDatabase
import com.fahimshahrier.data.local.entity.CategoryEntity
import com.fahimshahrier.data.local.entity.TransactionEntity
import com.fahimshahrier.data.local.entity.TransactionWithCategory
import com.fahimshahrier.data.repository.FinanceRepository
import com.fahimshahrier.data.sample.SampleDataGenerator
import com.fahimshahrier.data.model.QuickAddTemplate
import com.fahimshahrier.data.model.QuickTemplatesHelper
import com.fahimshahrier.ui.components.CategoryIconHelper
import com.fahimshahrier.ui.model.AppCurrency
import com.fahimshahrier.ui.model.CategorySpending
import com.fahimshahrier.ui.model.CurrencyList
import com.fahimshahrier.ui.model.DailyChartPoint
import com.fahimshahrier.ui.model.FinanceOverview
import com.fahimshahrier.ui.model.FinanceUiState
import com.fahimshahrier.ui.model.MonthlyAuditAnalyzer
import com.fahimshahrier.ui.model.TimeRange
import com.fahimshahrier.ui.model.TransactionTypeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

private data class DateFilterValues(
    val customStartDate: Long?,
    val customEndDate: Long?,
    val specificYear: Int?,
    val specificMonth: Int?
)

private data class FilterParams(
    val currency: AppCurrency,
    val timeRange: TimeRange,
    val typeFilter: TransactionTypeFilter,
    val selectedCategoryId: Long?,
    val selectedPaymentMethod: String?,
    val onlyRecurring: Boolean,
    val searchQuery: String,
    val userMessage: String?,
    val customStartDate: Long?,
    val customEndDate: Long?,
    val specificYear: Int?,
    val specificMonth: Int?
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val prefs = application.getSharedPreferences("finance_app_prefs", Context.MODE_PRIVATE)
    private val _snapshots = MutableStateFlow<List<SnapshotItem>>(emptyList())

    init {
        val db = AppDatabase.getInstance(application)
        repository = FinanceRepository(db.categoryDao(), db.transactionDao())

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.initializeOrRestoreDatabase(application)
                refreshSnapshots()
                syncAutoBackup()
            } catch (_: Exception) {
            }
        }
    }

    private fun syncAutoBackup() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cats = repository.getAllCategoriesList()
                val txs = repository.getAllTransactionsForExport()
                if (cats.isNotEmpty() || txs.isNotEmpty()) {
                    SnapshotManager.saveAutoBackup(getApplication(), cats, txs)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun refreshSnapshots() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = SnapshotManager.listSnapshots(getApplication())
                _snapshots.value = list
            } catch (_: Exception) {
            }
        }
    }

    private val _selectedCurrency = MutableStateFlow(
        CurrencyList.fromCode(prefs.getString("pref_base_currency", CurrencyList.BDT.code))
    )
    val selectedCurrency: StateFlow<AppCurrency> = _selectedCurrency

    private val _timeRange = MutableStateFlow(TimeRange.THIS_MONTH)
    private val _customStartDate = MutableStateFlow<Long?>(null)
    private val _customEndDate = MutableStateFlow<Long?>(null)
    private val _specificYear = MutableStateFlow<Int?>(null)
    private val _specificMonth = MutableStateFlow<Int?>(null)

    private val _typeFilter = MutableStateFlow(TransactionTypeFilter.ALL)
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _selectedPaymentMethod = MutableStateFlow<String?>(null)
    private val _onlyRecurring = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _userMessage = MutableStateFlow<String?>(null)

    // Security & App Lock State
    val isAppLockEnabled = MutableStateFlow(prefs.getBoolean("pref_app_lock_enabled", false))
    val appLockPin = MutableStateFlow(prefs.getString("pref_app_lock_pin", "") ?: "")
    val isAppUnlocked = MutableStateFlow(!prefs.getBoolean("pref_app_lock_enabled", false))

    // Quick Add Customizable Templates State
    private val _quickAddTemplates = MutableStateFlow<List<QuickAddTemplate>>(
        QuickTemplatesHelper.fromJson(prefs.getString("pref_quick_add_templates", null))
    )
    val quickAddTemplates: StateFlow<List<QuickAddTemplate>> = _quickAddTemplates

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionWithCategory>> = repository.allTransactionsWithCategoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filterParams = combine(
        combine(_selectedCurrency, _timeRange, _typeFilter, _selectedPaymentMethod, _onlyRecurring) { currency, timeRange, typeFilter, paymentMethod, onlyRecurring ->
            Quint(currency, timeRange, typeFilter, paymentMethod, onlyRecurring)
        },
        combine(_customStartDate, _customEndDate, _specificYear, _specificMonth) { cStart, cEnd, sYear, sMonth ->
            DateFilterValues(cStart, cEnd, sYear, sMonth)
        },
        _selectedCategoryId,
        _searchQuery,
        _userMessage
    ) { (currency, timeRange, typeFilter, paymentMethod, onlyRecurring), dateFilterValues, categoryId, query, userMessage ->
        FilterParams(
            currency = currency,
            timeRange = timeRange,
            typeFilter = typeFilter,
            selectedCategoryId = categoryId,
            selectedPaymentMethod = paymentMethod,
            onlyRecurring = onlyRecurring,
            searchQuery = query,
            userMessage = userMessage,
            customStartDate = dateFilterValues.customStartDate,
            customEndDate = dateFilterValues.customEndDate,
            specificYear = dateFilterValues.specificYear,
            specificMonth = dateFilterValues.specificMonth
        )
    }

    val uiState: StateFlow<FinanceUiState> = combine(
        combine(
            repository.allTransactionsWithCategoryFlow,
            repository.allCategoriesFlow,
            _snapshots
        ) { rawTransactions, categories, snapshotsList ->
            Triple(rawTransactions, categories, snapshotsList)
        },
        _quickAddTemplates,
        _filterParams
    ) { (rawTransactions, categories, snapshotsList), templatesList, filters ->

        val (startTime, endTime) = filters.timeRange.getStartAndEndTime(
            customStart = filters.customStartDate,
            customEnd = filters.customEndDate,
            specificYear = filters.specificYear,
            specificMonth = filters.specificMonth
        )

        val periodLabel = filters.timeRange.getPeriodLabel(
            customStart = filters.customStartDate,
            customEnd = filters.customEndDate,
            specificYear = filters.specificYear,
            specificMonth = filters.specificMonth
        )

        // Filter transactions by date range for overview
        val rangeTransactions = rawTransactions.filter {
            it.transaction.dateTimestamp in startTime..endTime
        }

        // Compute Overview (Totals, Category Breakdown, Daily Chart, Monthly Audit)
        val overview = calculateOverview(rangeTransactions, categories, filters.currency, periodLabel)

        // Filter transactions for List View (Range + Type + Category + Payment Method + Recurring + Search Query)
        val filteredTransactions = rangeTransactions.filter { item ->
            val matchesType = when (filters.typeFilter) {
                TransactionTypeFilter.ALL -> true
                TransactionTypeFilter.EXPENSE -> item.transaction.type == "EXPENSE"
                TransactionTypeFilter.INCOME -> item.transaction.type == "INCOME"
            }
            val matchesCategory = when (filters.selectedCategoryId) {
                null -> true
                -1L -> item.transaction.categoryId == null // Specifically Uncategorized
                else -> item.transaction.categoryId == filters.selectedCategoryId
            }
            val matchesPayment = filters.selectedPaymentMethod == null ||
                item.transaction.paymentMethod.equals(filters.selectedPaymentMethod, ignoreCase = true)
            val matchesRecurring = !filters.onlyRecurring || item.transaction.isRecurring
            val matchesQuery = filters.searchQuery.isBlank() ||
                item.transaction.title.contains(filters.searchQuery, ignoreCase = true) ||
                item.transaction.note.contains(filters.searchQuery, ignoreCase = true) ||
                (item.category?.name?.contains(filters.searchQuery, ignoreCase = true) == true) ||
                (item.transaction.categoryId == null && "uncategorized".contains(filters.searchQuery, ignoreCase = true)) ||
                item.transaction.paymentMethod.contains(filters.searchQuery, ignoreCase = true)

            matchesType && matchesCategory && matchesPayment && matchesRecurring && matchesQuery
        }

        FinanceUiState(
            isLoading = false,
            currency = filters.currency,
            timeRange = filters.timeRange,
            customStartDate = filters.customStartDate,
            customEndDate = filters.customEndDate,
            specificYear = filters.specificYear,
            specificMonth = filters.specificMonth,
            periodLabel = periodLabel,
            typeFilter = filters.typeFilter,
            selectedCategoryId = filters.selectedCategoryId,
            selectedPaymentMethod = filters.selectedPaymentMethod,
            onlyRecurring = filters.onlyRecurring,
            searchQuery = filters.searchQuery,
            overview = overview,
            transactions = filteredTransactions,
            monthlyTransactions = rangeTransactions,
            categories = categories,
            quickAddTemplates = templatesList,
            snapshots = snapshotsList,
            userMessage = filters.userMessage
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinanceUiState(isLoading = true)
    )

    fun setBaseCurrency(currency: AppCurrency) {
        _selectedCurrency.value = currency
        prefs.edit().putString("pref_base_currency", currency.code).apply()
    }

    fun setBaseCurrency(code: String) {
        val currency = CurrencyList.fromCode(code)
        setBaseCurrency(currency)
    }

    private fun calculateOverview(
        transactions: List<TransactionWithCategory>,
        categories: List<CategoryEntity>,
        currency: AppCurrency,
        periodName: String
    ): FinanceOverview {
        var totalIncome = 0.0
        var totalExpense = 0.0

        val categorySpendingMap = mutableMapOf<Long?, Double>()
        val categoryCountMap = mutableMapOf<Long?, Int>()

        for (item in transactions) {
            val t = item.transaction
            if (t.type == "INCOME") {
                totalIncome += t.amount
            } else {
                totalExpense += t.amount
                val catId = t.categoryId
                categorySpendingMap[catId] = (categorySpendingMap[catId] ?: 0.0) + t.amount
                categoryCountMap[catId] = (categoryCountMap[catId] ?: 0) + 1
            }
        }

        val netBalance = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome) * 100.0 else 0.0

        val totalBudget = categories
            .filter { it.type == "EXPENSE" && it.budgetLimit > 0 }
            .sumOf { it.budgetLimit }

        var totalBudgetSpent = 0.0
        for (cat in categories) {
            if (cat.type == "EXPENSE" && cat.budgetLimit > 0) {
                totalBudgetSpent += (categorySpendingMap[cat.id] ?: 0.0)
            }
        }

        val categorySpendingList = categories
            .filter { it.type == "EXPENSE" }
            .map { cat ->
                val spent = categorySpendingMap[cat.id] ?: 0.0
                val count = categoryCountMap[cat.id] ?: 0
                val pct = if (totalExpense > 0) ((spent / totalExpense) * 100.0).toFloat() else 0f
                CategorySpending(
                    categoryId = cat.id,
                    categoryName = cat.name,
                    iconName = cat.iconName,
                    color = CategoryIconHelper.parseColor(cat.colorHex),
                    totalAmount = spent,
                    percentage = pct,
                    transactionCount = count,
                    budgetLimit = cat.budgetLimit
                )
            }
            .filter { it.totalAmount > 0 || it.budgetLimit > 0 }
            .toMutableList()

        // Include Uncategorized Spending if present
        val uncategorizedSpent = categorySpendingMap[null] ?: 0.0
        val uncategorizedCount = categoryCountMap[null] ?: 0
        if (uncategorizedSpent > 0 || uncategorizedCount > 0) {
            val pct = if (totalExpense > 0) ((uncategorizedSpent / totalExpense) * 100.0).toFloat() else 0f
            categorySpendingList.add(
                CategorySpending(
                    categoryId = -1L,
                    categoryName = "Uncategorized",
                    iconName = "category",
                    color = Color(0xFF78909C),
                    totalAmount = uncategorizedSpent,
                    percentage = pct,
                    transactionCount = uncategorizedCount,
                    budgetLimit = 0.0
                )
            )
        }

        val sortedCategorySpendingList = categorySpendingList.sortedByDescending { it.totalAmount }

        val dailyChartPoints = calculateDailyChartPoints(transactions)

        val monthlyAuditReport = MonthlyAuditAnalyzer.analyzeMonthlyExpenses(
            transactions = transactions,
            categories = categories,
            totalIncome = totalIncome,
            currency = currency,
            periodName = periodName
        )

        return FinanceOverview(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            savingsRate = savingsRate,
            totalBudget = totalBudget,
            totalBudgetSpent = totalBudgetSpent,
            categorySpendingList = sortedCategorySpendingList,
            dailyChartPoints = dailyChartPoints,
            totalTransactionCount = transactions.size,
            monthlyAuditReport = monthlyAuditReport
        )
    }

    private fun calculateDailyChartPoints(transactions: List<TransactionWithCategory>): List<DailyChartPoint> {
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val pointsMap = linkedMapOf<String, Triple<Long, Double, Double>>() // key -> (timestamp, expense, income)

        // Populate last 7 days buckets
        val now = System.currentTimeMillis()
        for (i in 6 downTo 0) {
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            val label = dayFormat.format(Date(dayStart))
            pointsMap[label] = Triple(dayStart, 0.0, 0.0)
        }

        for (item in transactions) {
            val t = item.transaction
            val label = dayFormat.format(Date(t.dateTimestamp))
            val current = pointsMap[label]
            if (current != null) {
                val newExp = if (t.type == "EXPENSE") current.second + t.amount else current.second
                val newInc = if (t.type == "INCOME") current.third + t.amount else current.third
                pointsMap[label] = Triple(current.first, newExp, newInc)
            }
        }

        return pointsMap.map { (label, data) ->
            DailyChartPoint(
                dayLabel = label,
                timestamp = data.first,
                expenseAmount = data.second,
                incomeAmount = data.third
            )
        }
    }

    // Filter Setters
    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
        if (range != TimeRange.CUSTOM_RANGE && range != TimeRange.SPECIFIC_MONTH) {
            _customStartDate.value = null
            _customEndDate.value = null
            _specificYear.value = null
            _specificMonth.value = null
        }
    }

    fun setSpecificMonth(year: Int, month: Int) {
        _specificYear.value = year
        _specificMonth.value = month
        _timeRange.value = TimeRange.SPECIFIC_MONTH
    }

    fun setCustomDateRange(startTimestamp: Long, endTimestamp: Long) {
        _customStartDate.value = startTimestamp
        _customEndDate.value = endTimestamp
        _timeRange.value = TimeRange.CUSTOM_RANGE
    }

    fun setTypeFilter(filter: TransactionTypeFilter) {
        _typeFilter.value = filter
    }

    fun setSelectedCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedPaymentMethod(paymentMethod: String?) {
        _selectedPaymentMethod.value = paymentMethod
    }

    fun setOnlyRecurring(onlyRecurring: Boolean) {
        _onlyRecurring.value = onlyRecurring
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // App Lock / Security Management
    fun setAppLock(enabled: Boolean, pin: String) {
        prefs.edit()
            .putBoolean("pref_app_lock_enabled", enabled)
            .putString("pref_app_lock_pin", pin)
            .apply()
        isAppLockEnabled.value = enabled
        appLockPin.value = pin
        isAppUnlocked.value = !enabled
    }

    fun unlockApp() {
        isAppUnlocked.value = true
    }

    fun lockApp() {
        if (isAppLockEnabled.value) {
            isAppUnlocked.value = false
        }
    }

    // Transaction Actions
    fun saveTransaction(
        id: Long = 0,
        title: String,
        amount: Double,
        type: String,
        categoryId: Long?,
        dateTimestamp: Long,
        note: String,
        paymentMethod: String,
        isRecurring: Boolean,
        receiptUri: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = TransactionEntity(
                id = id,
                title = title.trim(),
                amount = amount,
                type = type,
                categoryId = categoryId,
                dateTimestamp = dateTimestamp,
                note = note.trim(),
                paymentMethod = paymentMethod,
                isRecurring = isRecurring,
                receiptUri = receiptUri
            )
            if (id == 0L) {
                repository.insertTransaction(entity)
                _userMessage.value = "Transaction added successfully"
            } else {
                repository.updateTransaction(entity)
                _userMessage.value = "Transaction updated"
            }
            syncAutoBackup()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
            _userMessage.value = "Transaction deleted"
            syncAutoBackup()
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransactionById(id)
            _userMessage.value = "Transaction deleted"
            syncAutoBackup()
        }
    }

    // Category Actions
    fun saveCategory(
        id: Long = 0,
        name: String,
        iconName: String,
        colorHex: Long,
        type: String,
        budgetLimit: Double,
        onSuccess: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val normalizedColor = CategoryIconHelper.normalizeColorHex(colorHex)
            val entity = CategoryEntity(
                id = id,
                name = name.trim(),
                iconName = iconName,
                colorHex = normalizedColor,
                type = type,
                budgetLimit = budgetLimit
            )
            val savedId = if (id == 0L) {
                val newId = repository.insertCategory(entity)
                _userMessage.value = "Category created"
                newId
            } else {
                repository.updateCategory(entity)
                _userMessage.value = "Category updated"
                id
            }
            syncAutoBackup()
            if (onSuccess != null) {
                withContext(Dispatchers.Main) {
                    onSuccess(savedId)
                }
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(category)
            _userMessage.value = "Category '${category.name}' deleted"
            syncAutoBackup()
        }
    }

    // Quick-Add Templates Management
    fun saveQuickAddTemplate(template: QuickAddTemplate) {
        val current = _quickAddTemplates.value.toMutableList()
        val index = current.indexOfFirst { it.id == template.id }
        if (index >= 0) {
            current[index] = template
            _userMessage.value = "Shortcut '${template.title}' updated"
        } else {
            current.add(template)
            _userMessage.value = "Shortcut '${template.title}' added"
        }
        _quickAddTemplates.value = current
        prefs.edit().putString("pref_quick_add_templates", QuickTemplatesHelper.toJson(current)).apply()
    }

    fun deleteQuickAddTemplate(templateId: String) {
        val current = _quickAddTemplates.value.filterNot { it.id == templateId }
        _quickAddTemplates.value = current
        prefs.edit().putString("pref_quick_add_templates", QuickTemplatesHelper.toJson(current)).apply()
        _userMessage.value = "Shortcut removed"
    }

    fun resetQuickAddTemplates() {
        val defaults = QuickTemplatesHelper.defaultPresets
        _quickAddTemplates.value = defaults
        prefs.edit().putString("pref_quick_add_templates", QuickTemplatesHelper.toJson(defaults)).apply()
        _userMessage.value = "Default shortcuts restored"
    }

    // Export, Snapshots & Restoring
    fun exportCsv(onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getAllTransactionsForExport()
            val csv = DataExportImportHelper.generateCsv(list)
            withContext(Dispatchers.Main) {
                onReady(csv)
            }
        }
    }

    fun exportJson(onReady: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val categories = allCategories.value
            val list = repository.getAllTransactionsForExport()
            val json = DataExportImportHelper.generateJson(categories, list)
            withContext(Dispatchers.Main) {
                onReady(json)
            }
        }
    }

    fun createManualSnapshot(note: String = "Manual Snapshot") {
        viewModelScope.launch(Dispatchers.IO) {
            val categories = allCategories.value
            val list = repository.getAllTransactionsForExport()
            val saved = SnapshotManager.saveSnapshot(
                context = getApplication(),
                categories = categories,
                transactions = list,
                note = note.ifBlank { "Manual Snapshot" }
            )
            refreshSnapshots()
            _userMessage.value = "Backup snapshot '${saved.note}' saved"
        }
    }

    private suspend fun autoSaveSafetySnapshot(reason: String) {
        try {
            val categories = allCategories.value
            val transactions = repository.getAllTransactionsForExport()
            if (transactions.isNotEmpty()) {
                SnapshotManager.saveSnapshot(
                    context = getApplication(),
                    categories = categories,
                    transactions = transactions,
                    note = "Auto-Safety: Before $reason"
                )
            }
        } catch (_: Exception) {
        }
    }

    fun restoreSnapshot(
        snapshot: SnapshotItem,
        replaceExisting: Boolean,
        onSuccess: (Int, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = SnapshotManager.readSnapshotContent(getApplication(), snapshot.filename)
                if (content.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        onError("Snapshot data could not be found.")
                    }
                    return@launch
                }

                autoSaveSafetySnapshot("Restoring '${snapshot.note}'")

                when (val parseResult = DataExportImportHelper.parseImportContent(content)) {
                    is ImportParseResult.Success -> {
                        val (cats, trans) = repository.restoreDatabase(
                            importedCategories = parseResult.categories,
                            importedTransactions = parseResult.transactions,
                            replaceExisting = replaceExisting
                        )
                        refreshSnapshots()
                        syncAutoBackup()
                        withContext(Dispatchers.Main) {
                            onSuccess(cats, trans)
                        }
                        _userMessage.value = "Successfully restored $trans records"
                    }
                    is ImportParseResult.Error -> {
                        withContext(Dispatchers.Main) {
                            onError(parseResult.message)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Failed to restore snapshot")
                }
            }
        }
    }

    fun deleteSnapshot(snapshot: SnapshotItem) {
        viewModelScope.launch(Dispatchers.IO) {
            SnapshotManager.deleteSnapshot(getApplication(), snapshot.filename)
            refreshSnapshots()
            _userMessage.value = "Snapshot deleted"
        }
    }

    fun importData(
        content: String,
        replaceExisting: Boolean,
        onSuccess: (Int, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                autoSaveSafetySnapshot(if (replaceExisting) "Replace Restore" else "Merge Import")

                when (val parseResult = DataExportImportHelper.parseImportContent(content)) {
                    is ImportParseResult.Success -> {
                        val (cats, trans) = repository.restoreDatabase(
                            importedCategories = parseResult.categories,
                            importedTransactions = parseResult.transactions,
                            replaceExisting = replaceExisting
                        )
                        refreshSnapshots()
                        syncAutoBackup()
                        withContext(Dispatchers.Main) {
                            onSuccess(cats, trans)
                        }
                        _userMessage.value = "Imported $trans records from ${parseResult.format}"
                    }
                    is ImportParseResult.Error -> {
                        withContext(Dispatchers.Main) {
                            onError(parseResult.message)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Failed to parse import file")
                }
            }
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            autoSaveSafetySnapshot("Reset to Sample Data")
            repository.resetToSampleData()
            refreshSnapshots()
            _userMessage.value = "Sample data restored"
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            autoSaveSafetySnapshot("Clear All Transactions")
            repository.clearAllTransactions()
            refreshSnapshots()
            _userMessage.value = "All transactions cleared"
        }
    }
}
