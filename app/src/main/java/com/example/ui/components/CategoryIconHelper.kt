package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {

    val availableIcons: List<Pair<String, ImageVector>> = listOf(
        "fastfood" to Icons.Default.Fastfood,
        "shopping_cart" to Icons.Default.ShoppingCart,
        "shopping_bag" to Icons.Default.ShoppingBag,
        "home" to Icons.Default.Home,
        "directions_car" to Icons.Default.DirectionsCar,
        "payments" to Icons.Default.Payments,
        "work" to Icons.Default.Work,
        "trending_up" to Icons.Default.TrendingUp,
        "savings" to Icons.Default.Savings,
        "receipt_long" to Icons.Default.ReceiptLong,
        "sports_esports" to Icons.Default.SportsEsports,
        "subscriptions" to Icons.Default.Subscriptions,
        "fitness_center" to Icons.Default.FitnessCenter,
        "local_hospital" to Icons.Default.LocalHospital,
        "school" to Icons.Default.School,
        "flight" to Icons.Default.Flight,
        "card_giftcard" to Icons.Default.CardGiftcard,
        "account_balance" to Icons.Default.AccountBalance,
        "monetization_on" to Icons.Default.MonetizationOn,
        "more_horiz" to Icons.Default.MoreHoriz
    )

    fun getIcon(iconName: String): ImageVector {
        return availableIcons.find { it.first.equals(iconName, ignoreCase = true) }?.second
            ?: Icons.Default.Category
    }

    /**
     * Safely normalizes 32-bit ARGB hex Long values, resolving any 64-bit Compose value encoding.
     */
    fun normalizeColorHex(colorHex: Long): Long {
        val clean = if (colorHex > 0xFFFFFFFFL || colorHex < 0L) {
            (colorHex ushr 32) and 0xFFFFFFFFL
        } else {
            colorHex and 0xFFFFFFFFL
        }
        return if (clean == 0L) 0xFFFF7043L else clean
    }

    /**
     * Converts a database colorHex value into a valid Compose Color.
     */
    fun parseColor(colorHex: Long): Color {
        val normalized = normalizeColorHex(colorHex)
        return Color(normalized)
    }

    /**
     * Converts a Compose Color into a 32-bit ARGB hex Long.
     */
    fun colorToHex(color: Color): Long {
        val hex = (color.value shr 32).toLong() and 0xFFFFFFFFL
        return if (hex == 0L) 0xFFFF7043L else hex
    }

    /**
     * Suggests a matching icon string key based on a category name.
     */
    fun suggestIconForName(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("food") || lower.contains("din") || lower.contains("rest") || lower.contains("meal") || lower.contains("grocer") -> "fastfood"
            lower.contains("shop") || lower.contains("buy") || lower.contains("cloth") || lower.contains("mart") -> "shopping_cart"
            lower.contains("rent") || lower.contains("home") || lower.contains("house") || lower.contains("util") -> "home"
            lower.contains("car") || lower.contains("auto") || lower.contains("fuel") || lower.contains("gas") || lower.contains("transit") || lower.contains("transport") -> "directions_car"
            lower.contains("salary") || lower.contains("wage") || lower.contains("pay") || lower.contains("income") -> "payments"
            lower.contains("work") || lower.contains("freelance") || lower.contains("job") || lower.contains("client") -> "work"
            lower.contains("invest") || lower.contains("stock") || lower.contains("crypto") || lower.contains("trade") -> "trending_up"
            lower.contains("save") || lower.contains("deposit") || lower.contains("interest") -> "savings"
            lower.contains("bill") || lower.contains("receipt") || lower.contains("tax") || lower.contains("fee") -> "receipt_long"
            lower.contains("game") || lower.contains("play") || lower.contains("entert") -> "sports_esports"
            lower.contains("sub") || lower.contains("netfl") || lower.contains("spot") || lower.contains("stream") -> "subscriptions"
            lower.contains("gym") || lower.contains("fit") || lower.contains("sport") || lower.contains("health") -> "fitness_center"
            lower.contains("med") || lower.contains("doc") || lower.contains("pharm") || lower.contains("hosp") -> "local_hospital"
            lower.contains("edu") || lower.contains("school") || lower.contains("course") || lower.contains("book") -> "school"
            lower.contains("travel") || lower.contains("flight") || lower.contains("trip") || lower.contains("hotel") || lower.contains("vacation") -> "flight"
            lower.contains("gift") || lower.contains("donat") || lower.contains("charity") -> "card_giftcard"
            lower.contains("bank") || lower.contains("loan") || lower.contains("mortgage") -> "account_balance"
            else -> "more_horiz"
        }
    }

    /**
     * Suggests a vibrant ARGB hex color Long based on category name.
     */
    fun suggestColorForName(name: String): Long {
        val lower = name.lowercase()
        return when {
            lower.contains("food") || lower.contains("grocer") -> 0xFFE65100L // Orange
            lower.contains("shop") || lower.contains("cloth") -> 0xFF8E24AAL // Purple
            lower.contains("home") || lower.contains("rent") || lower.contains("util") -> 0xFF1E88E5L // Blue
            lower.contains("car") || lower.contains("transport") || lower.contains("fuel") -> 0xFFD81B60L // Pink
            lower.contains("salary") || lower.contains("income") || lower.contains("pay") -> 0xFF2E7D32L // Green
            lower.contains("work") || lower.contains("freelance") -> 0xFF00897BL // Teal
            lower.contains("invest") || lower.contains("stock") -> 0xFF00ACC1L // Cyan
            lower.contains("bill") || lower.contains("fee") || lower.contains("tax") -> 0xFFC2185BL // Magenta
            lower.contains("health") || lower.contains("med") -> 0xFFE53935L // Red
            lower.contains("edu") || lower.contains("course") -> 0xFF5E35B1L // Deep Purple
            lower.contains("travel") || lower.contains("trip") -> 0xFF039BE5L // Light Blue
            lower.contains("gift") -> 0xFFF4511EL // Deep Orange
            else -> 0xFF546E7AL // Blue Grey
        }
    }
}
