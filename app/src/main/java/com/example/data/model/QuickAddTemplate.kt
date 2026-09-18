/*
 * Copyright (c) 2026 FahimShahrier. All rights reserved.
 * Developed by FahimShahrier
 */

package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.components.CategoryIconHelper
import org.json.JSONArray
import org.json.JSONObject

/**
 * Customizable Quick-Add Shortcut Template.
 * Can be customized by the user, saved to SharedPreferences, and restored seamlessly.
 */
data class QuickAddTemplate(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: String,
    val categoryName: String,
    val paymentMethod: String,
    val iconName: String = "fastfood",
    val colorHex: Long = 0xFFFF7043L
) {
    val icon: ImageVector
        get() = when (iconName) {
            "coffee" -> Icons.Default.Coffee
            "directions_bus" -> Icons.Default.DirectionsBus
            "local_gas_station" -> Icons.Default.LocalGasStation
            "shopping_cart" -> Icons.Default.ShoppingCart
            "trending_up" -> Icons.Default.TrendingUp
            else -> CategoryIconHelper.getIcon(iconName)
        }

    val color: Color
        get() = CategoryIconHelper.parseColor(colorHex)
}

object QuickTemplatesHelper {
    val defaultPresets = listOf(
        QuickAddTemplate(
            id = "preset_coffee",
            title = "Coffee / Tea",
            amount = 30.0,
            type = "EXPENSE",
            categoryName = "Food & Dining",
            paymentMethod = "bKash",
            iconName = "coffee",
            colorHex = 0xFF8D6E63L
        ),
        QuickAddTemplate(
            id = "preset_lunch",
            title = "Lunch Meal",
            amount = 180.0,
            type = "EXPENSE",
            categoryName = "Food & Dining",
            paymentMethod = "bKash",
            iconName = "fastfood",
            colorHex = 0xFFFF9800L
        ),
        QuickAddTemplate(
            id = "preset_bus",
            title = "Bus / Metro Fare",
            amount = 50.0,
            type = "EXPENSE",
            categoryName = "Transport",
            paymentMethod = "Cash",
            iconName = "directions_bus",
            colorHex = 0xFF29B6F6L
        ),
        QuickAddTemplate(
            id = "preset_grocery",
            title = "Grocery Supplies",
            amount = 500.0,
            type = "EXPENSE",
            categoryName = "Groceries",
            paymentMethod = "Card",
            iconName = "shopping_cart",
            colorHex = 0xFF4CAF50L
        ),
        QuickAddTemplate(
            id = "preset_fuel",
            title = "Fuel / CNG",
            amount = 300.0,
            type = "EXPENSE",
            categoryName = "Transport",
            paymentMethod = "Card",
            iconName = "local_gas_station",
            colorHex = 0xFFEF5350L
        ),
        QuickAddTemplate(
            id = "preset_freelance",
            title = "Freelance Income",
            amount = 2500.0,
            type = "INCOME",
            categoryName = "Salary / Income",
            paymentMethod = "Bank Transfer",
            iconName = "trending_up",
            colorHex = 0xFF2E7D32L
        )
    )

    fun toJson(templates: List<QuickAddTemplate>): String {
        val array = JSONArray()
        for (item in templates) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("amount", item.amount)
                put("type", item.type)
                put("categoryName", item.categoryName)
                put("paymentMethod", item.paymentMethod)
                put("iconName", item.iconName)
                put("colorHex", item.colorHex)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun fromJson(jsonStr: String?): List<QuickAddTemplate> {
        if (jsonStr.isNullOrBlank()) return defaultPresets
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<QuickAddTemplate>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    QuickAddTemplate(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        type = obj.optString("type", "EXPENSE"),
                        categoryName = obj.optString("categoryName", "Uncategorized"),
                        paymentMethod = obj.optString("paymentMethod", "Cash"),
                        iconName = obj.optString("iconName", "fastfood"),
                        colorHex = obj.optLong("colorHex", 0xFFFF7043L)
                    )
                )
            }
            if (list.isEmpty()) defaultPresets else list
        } catch (_: Exception) {
            defaultPresets
        }
    }
}
