package com.example.ui.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimeRange(val displayName: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    SPECIFIC_MONTH("Pick Month"),
    THIS_YEAR("This Year"),
    LAST_YEAR("Last Year"),
    CUSTOM_RANGE("Custom Range"),
    ALL_TIME("All Time");

    fun getStartAndEndTime(
        customStart: Long? = null,
        customEnd: Long? = null,
        specificYear: Int? = null,
        specificMonth: Int? = null // 0-based: 0 = Jan, 11 = Dec
    ): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (this) {
            THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            SPECIFIC_MONTH -> {
                val year = specificYear ?: cal.get(Calendar.YEAR)
                val month = specificMonth ?: cal.get(Calendar.MONTH)
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.MONTH, 11)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            LAST_YEAR -> {
                cal.add(Calendar.YEAR, -1)
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.MONTH, 11)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            CUSTOM_RANGE -> {
                val start = customStart ?: 0L
                val end = customEnd ?: Long.MAX_VALUE
                Pair(start, end)
            }
            ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }

    fun getPeriodLabel(
        customStart: Long? = null,
        customEnd: Long? = null,
        specificYear: Int? = null,
        specificMonth: Int? = null
    ): String {
        return when (this) {
            THIS_MONTH -> "This Month"
            LAST_MONTH -> "Last Month"
            THIS_YEAR -> "This Year"
            LAST_YEAR -> "Last Year"
            ALL_TIME -> "All Time"
            SPECIFIC_MONTH -> {
                if (specificYear != null && specificMonth != null) {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, specificYear)
                        set(Calendar.MONTH, specificMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    fmt.format(cal.time)
                } else {
                    "Selected Month"
                }
            }
            CUSTOM_RANGE -> {
                if (customStart != null && customEnd != null) {
                    val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    "${fmt.format(Date(customStart))} - ${fmt.format(Date(customEnd))}"
                } else {
                    "Custom Range"
                }
            }
        }
    }
}

enum class TransactionTypeFilter(val displayName: String) {
    ALL("All"),
    EXPENSE("Expenses"),
    INCOME("Income")
}
