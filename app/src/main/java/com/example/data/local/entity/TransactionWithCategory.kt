package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Transaction with parent Category details
 */
data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
