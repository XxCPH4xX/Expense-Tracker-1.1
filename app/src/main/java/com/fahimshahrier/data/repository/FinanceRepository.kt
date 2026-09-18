package com.fahimshahrier.data.repository

import android.content.Context
import com.fahimshahrier.data.export.DataExportImportHelper
import com.fahimshahrier.data.export.ImportParseResult
import com.fahimshahrier.data.export.SnapshotManager
import com.fahimshahrier.data.local.dao.CategoryDao
import com.fahimshahrier.data.local.dao.TransactionDao
import com.fahimshahrier.data.local.entity.CategoryEntity
import com.fahimshahrier.data.local.entity.CategoryWithTransactions
import com.fahimshahrier.data.local.entity.TransactionEntity
import com.fahimshahrier.data.local.entity.TransactionWithCategory
import com.fahimshahrier.data.sample.SampleDataGenerator
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    // Categories
    val allCategoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()

    val categoriesWithTransactionsFlow: Flow<List<CategoryWithTransactions>> =
        categoryDao.getCategoriesWithTransactionsFlow()

    suspend fun getAllCategoriesList(): List<CategoryEntity> =
        categoryDao.getAllCategoriesList()

    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByTypeFlow(type)

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long> =
        categoryDao.insertCategories(categories)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)

    suspend fun deleteCategoryById(id: Long) =
        categoryDao.deleteCategoryById(id)

    // Transactions
    val allTransactionsWithCategoryFlow: Flow<List<TransactionWithCategory>> =
        transactionDao.getAllTransactionsWithCategoryFlow()

    fun getTransactionsInRange(startTime: Long, endTime: Long): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsInRangeFlow(startTime, endTime)

    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsByCategoryIdFlow(categoryId)

    fun searchTransactions(query: String): Flow<List<TransactionWithCategory>> =
        transactionDao.searchTransactionsFlow(query)

    suspend fun getAllTransactionsForExport(): List<TransactionWithCategory> =
        transactionDao.getAllTransactionsWithCategoryList()

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long> =
        transactionDao.insertTransactions(transactions)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)

    suspend fun clearAllTransactions() =
        transactionDao.clearAllTransactions()

    suspend fun getTransactionCount(): Int =
        transactionDao.getTransactionCount()

    suspend fun getCategoryCount(): Int =
        categoryDao.getCategoryCount()

    /**
     * Initializes database or safely recovers data if empty.
     * Prevents overwriting user info on app re-installation or update.
     */
    suspend fun initializeOrRestoreDatabase(context: Context) {
        val currentTxCount = transactionDao.getTransactionCount()
        if (currentTxCount > 0) {
            // User data is already safe and intact in SQLite database
            return
        }

        // Database has 0 transactions (e.g. fresh reinstall or table recreation).
        // Check if previous auto-backup or snapshot exists before seeding mock data.
        val backupJson = SnapshotManager.getLatestAutoBackupContent(context)
        if (!backupJson.isNullOrBlank()) {
            when (val parsed = DataExportImportHelper.parseImportContent(backupJson)) {
                is ImportParseResult.Success -> {
                    if (parsed.transactions.isNotEmpty() || parsed.categories.isNotEmpty()) {
                        restoreDatabase(
                            importedCategories = parsed.categories,
                            importedTransactions = parsed.transactions,
                            replaceExisting = true
                        )
                        return
                    }
                }
                else -> {}
            }
        }

        // Only for brand-new users with zero prior transactions and zero backups
        if (categoryDao.getCategoryCount() == 0) {
            categoryDao.insertCategories(SampleDataGenerator.defaultCategories)
        }
        if (transactionDao.getTransactionCount() == 0) {
            transactionDao.insertTransactions(SampleDataGenerator.createInitialTransactions())
        }
    }

    suspend fun resetToSampleData() {
        transactionDao.clearAllTransactions()
        categoryDao.insertCategories(SampleDataGenerator.defaultCategories)
        transactionDao.insertTransactions(SampleDataGenerator.createInitialTransactions())
    }

    /**
     * Fault-tolerant restoration of Categories and Transactions.
     * Prevents foreign key mismatches and primary key collision bugs.
     */
    suspend fun restoreDatabase(
        importedCategories: List<CategoryEntity>,
        importedTransactions: List<TransactionEntity>,
        replaceExisting: Boolean
    ): Pair<Int, Int> {
        val existingCategories = categoryDao.getAllCategoriesList()
        val categoryNameToEntityMap = existingCategories.associateBy { "${it.name.lowercase().trim()}_${it.type}" }.toMutableMap()
        val categoryIdMap = mutableMapOf<Long, Long>()

        var insertedCategoryCount = 0

        // Process categories
        for (importedCat in importedCategories) {
            val key = "${importedCat.name.lowercase().trim()}_${importedCat.type}"
            val existing = categoryNameToEntityMap[key]

            if (existing != null) {
                // Category already exists locally, map the backup ID to the local ID
                if (importedCat.id > 0) {
                    categoryIdMap[importedCat.id] = existing.id
                }
            } else {
                // New category to insert
                val newEntity = importedCat.copy(id = 0L)
                val newId = categoryDao.insertCategory(newEntity)
                val saved = newEntity.copy(id = newId)
                categoryNameToEntityMap[key] = saved
                if (importedCat.id > 0) {
                    categoryIdMap[importedCat.id] = newId
                }
                insertedCategoryCount++
            }
        }

        if (replaceExisting) {
            transactionDao.clearAllTransactions()
        }

        // Prepare transactions with re-mapped category foreign keys
        val transactionsToInsert = importedTransactions.map { t ->
            val mappedCatId = if (t.categoryId != null && categoryIdMap.containsKey(t.categoryId)) {
                categoryIdMap[t.categoryId]
            } else {
                t.categoryId
            }

            // Always assign id = 0L so SQLite generates fresh unique auto-increment keys
            t.copy(
                id = 0L,
                categoryId = mappedCatId
            )
        }

        if (transactionsToInsert.isNotEmpty()) {
            transactionDao.insertTransactions(transactionsToInsert)
        }

        return Pair(insertedCategoryCount, transactionsToInsert.size)
    }
}
