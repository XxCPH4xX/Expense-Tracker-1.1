package com.fahimshahrier.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 1-to-Many relationship from Category -> List<TransactionEntity>
 */
data class CategoryWithTransactions(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val transactions: List<TransactionEntity>
)
