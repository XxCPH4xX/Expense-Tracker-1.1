package com.fahimshahrier.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["dateTimestamp"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val categoryId: Long? = null,
    val dateTimestamp: Long, // Epoch timestamp in millis
    val note: String = "",
    val paymentMethod: String = "Card", // "Cash", "Card", "Bank Transfer", "UPI", "Crypto", "Other"
    val isRecurring: Boolean = false,
    val receiptUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
