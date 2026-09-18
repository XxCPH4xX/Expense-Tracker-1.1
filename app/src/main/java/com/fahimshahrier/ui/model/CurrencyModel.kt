/*
 * Copyright (c) 2026 FahimShahrier. All rights reserved.
 * Developed by FahimShahrier
 */

package com.fahimshahrier.ui.model

import java.util.Locale

/**
 * Supported base currencies for global users.
 */
data class AppCurrency(
    val code: String,
    val symbol: String,
    val name: String,
    val flag: String,
    val symbolPrefix: Boolean = true
)

object CurrencyList {
    val BDT = AppCurrency(code = "BDT", symbol = "৳", name = "Bangladeshi Taka", flag = "🇧🇩", symbolPrefix = true)
    val USD = AppCurrency(code = "USD", symbol = "$", name = "US Dollar", flag = "🇺🇸", symbolPrefix = true)
    val GBP = AppCurrency(code = "GBP", symbol = "£", name = "British Pound", flag = "🇬🇧", symbolPrefix = true)
    val EUR = AppCurrency(code = "EUR", symbol = "€", name = "Euro", flag = "🇪🇺", symbolPrefix = true)
    val INR = AppCurrency(code = "INR", symbol = "₹", name = "Indian Rupee", flag = "🇮🇳", symbolPrefix = true)
    val CAD = AppCurrency(code = "CAD", symbol = "CA$", name = "Canadian Dollar", flag = "🇨🇦", symbolPrefix = true)
    val AUD = AppCurrency(code = "AUD", symbol = "AU$", name = "Australian Dollar", flag = "🇦🇺", symbolPrefix = true)
    val JPY = AppCurrency(code = "JPY", symbol = "¥", name = "Japanese Yen", flag = "🇯🇵", symbolPrefix = true)
    val AED = AppCurrency(code = "AED", symbol = "AED", name = "UAE Dirham", flag = "🇦🇪", symbolPrefix = false)
    val SAR = AppCurrency(code = "SAR", symbol = "SAR", name = "Saudi Riyal", flag = "🇸🇦", symbolPrefix = false)
    val SGD = AppCurrency(code = "SGD", symbol = "S$", name = "Singapore Dollar", flag = "🇸🇬", symbolPrefix = true)
    val MYR = AppCurrency(code = "MYR", symbol = "RM", name = "Malaysian Ringgit", flag = "🇲🇾", symbolPrefix = true)
    val PKR = AppCurrency(code = "PKR", symbol = "₨", name = "Pakistani Rupee", flag = "🇵🇰", symbolPrefix = true)
    val CNY = AppCurrency(code = "CNY", symbol = "¥", name = "Chinese Yuan", flag = "🇨🇳", symbolPrefix = true)
    val BRL = AppCurrency(code = "BRL", symbol = "R$", name = "Brazilian Real", flag = "🇧🇷", symbolPrefix = true)
    val CHF = AppCurrency(code = "CHF", symbol = "CHF", name = "Swiss Franc", flag = "🇨🇭", symbolPrefix = false)

    val allCurrencies = listOf(
        BDT,
        USD,
        GBP,
        EUR,
        INR,
        CAD,
        AUD,
        JPY,
        AED,
        SAR,
        SGD,
        MYR,
        PKR,
        CNY,
        BRL,
        CHF
    )

    fun fromCode(code: String?): AppCurrency {
        if (code == null) return BDT
        return allCurrencies.find { it.code.equals(code, ignoreCase = true) } ?: BDT
    }
}

object CurrencyHelper {

    /**
     * Formats an amount with the specified base currency.
     * E.g. $1,250.00 or ৳1,250.00 or 1,250.00 SAR
     */
    fun format(
        amount: Double,
        currency: AppCurrency = CurrencyList.BDT,
        includeSign: Boolean = false,
        isExpense: Boolean = false
    ): String {
        val formattedNum = String.format(Locale.US, "%,.2f", kotlin.math.abs(amount))
        val raw = if (currency.symbolPrefix) {
            "${currency.symbol}$formattedNum"
        } else {
            "$formattedNum ${currency.symbol}"
        }

        return if (includeSign) {
            val sign = if (isExpense) "-" else "+"
            "$sign$raw"
        } else {
            raw
        }
    }

    /**
     * Compact format without decimals for budgets / chart axes
     */
    fun formatCompact(
        amount: Double,
        currency: AppCurrency = CurrencyList.BDT
    ): String {
        val formattedNum = String.format(Locale.US, "%,.0f", kotlin.math.abs(amount))
        return if (currency.symbolPrefix) {
            "${currency.symbol}$formattedNum"
        } else {
            "$formattedNum ${currency.symbol}"
        }
    }
}
