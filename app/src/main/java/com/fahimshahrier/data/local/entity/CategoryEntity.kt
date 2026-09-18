package com.fahimshahrier.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name", "type"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String, // e.g. "fastfood", "shopping_bag", "directions_car", "home", "payments", "work"
    val colorHex: Long, // ARGB format like 0xFF4CAF50
    val type: String, // "EXPENSE" or "INCOME"
    val budgetLimit: Double = 0.0, // 0.0 means no budget set
    val isDefault: Boolean = false
)
