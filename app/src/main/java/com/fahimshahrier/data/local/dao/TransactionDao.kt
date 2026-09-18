package com.fahimshahrier.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fahimshahrier.data.local.entity.TransactionEntity
import com.fahimshahrier.data.local.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    fun getAllTransactionsWithCategoryFlow(): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        "SELECT * FROM transactions " +
        "WHERE dateTimestamp >= :startTime AND dateTimestamp <= :endTime " +
        "ORDER BY dateTimestamp DESC"
    )
    fun getTransactionsInRangeFlow(startTime: Long, endTime: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        "SELECT * FROM transactions " +
        "WHERE categoryId = :categoryId " +
        "ORDER BY dateTimestamp DESC"
    )
    fun getTransactionsByCategoryIdFlow(categoryId: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        "SELECT * FROM transactions " +
        "WHERE title LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' " +
        "ORDER BY dateTimestamp DESC"
    )
    fun searchTransactionsFlow(query: String): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    suspend fun getAllTransactionsWithCategoryList(): List<TransactionWithCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int
}
