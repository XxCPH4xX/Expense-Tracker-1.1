package com.fahimshahrier.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.fahimshahrier.data.local.entity.CategoryEntity
import com.fahimshahrier.data.local.entity.TransactionEntity
import com.fahimshahrier.data.local.entity.TransactionWithCategory
import com.fahimshahrier.ui.components.CategoryIconHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ImportParseResult {
    data class Success(
        val format: String,
        val categories: List<CategoryEntity>,
        val transactions: List<TransactionEntity>,
        val dateRangeSummary: String,
        val totalExpenseSum: Double,
        val totalIncomeSum: Double
    ) : ImportParseResult()

    data class Error(val message: String) : ImportParseResult()
}

object DataExportImportHelper {

    private val defaultDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val fileTimestampFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    private val supportedDateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()),
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()),
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
        SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US),
        SimpleDateFormat("MMM d, yyyy", Locale.US),
        SimpleDateFormat("d MMM yyyy", Locale.US)
    )

    fun generateCsv(transactions: List<TransactionWithCategory>): String {
        val sb = StringBuilder()
        // Standard CSV Header
        sb.append("ID,Date,Title,Type,Category,Amount,Payment Method,Recurring,Note\n")

        for (item in transactions) {
            val t = item.transaction
            val catName = item.category?.name ?: "Uncategorized"
            val formattedDate = defaultDateFormatter.format(Date(t.dateTimestamp))
            val escapedTitle = escapeCsvField(t.title)
            val escapedCategory = escapeCsvField(catName)
            val escapedNote = escapeCsvField(t.note)
            val escapedPayment = escapeCsvField(t.paymentMethod)

            sb.append("${t.id},\"$formattedDate\",\"$escapedTitle\",${t.type},\"$escapedCategory\",${t.amount},\"$escapedPayment\",${t.isRecurring},\"$escapedNote\"\n")
        }

        return sb.toString()
    }

    private fun escapeCsvField(field: String): String {
        return field.replace("\"", "\"\"")
    }

    fun generateJson(
        categories: List<CategoryEntity>,
        transactions: List<TransactionWithCategory>,
        note: String = "Exported Backup"
    ): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("appName", "Expense Tracker")
        root.put("note", note)

        val categoriesArray = JSONArray()
        for (cat in categories) {
            val catObj = JSONObject()
            catObj.put("id", cat.id)
            catObj.put("name", cat.name)
            catObj.put("iconName", cat.iconName)
            catObj.put("colorHex", cat.colorHex)
            catObj.put("type", cat.type)
            catObj.put("budgetLimit", cat.budgetLimit)
            catObj.put("isDefault", cat.isDefault)
            categoriesArray.put(catObj)
        }
        root.put("categories", categoriesArray)

        val transactionsArray = JSONArray()
        for (item in transactions) {
            val t = item.transaction
            val tObj = JSONObject()
            tObj.put("id", t.id)
            tObj.put("title", t.title)
            tObj.put("amount", t.amount)
            tObj.put("type", t.type)
            if (t.categoryId != null) {
                tObj.put("categoryId", t.categoryId)
            } else {
                tObj.put("categoryId", JSONObject.NULL)
            }
            if (item.category != null) {
                tObj.put("categoryName", item.category.name)
            }
            tObj.put("dateTimestamp", t.dateTimestamp)
            tObj.put("formattedDate", defaultDateFormatter.format(Date(t.dateTimestamp)))
            tObj.put("note", t.note)
            tObj.put("paymentMethod", t.paymentMethod)
            tObj.put("isRecurring", t.isRecurring)
            if (t.receiptUri != null) {
                tObj.put("receiptUri", t.receiptUri)
            }
            tObj.put("createdAt", t.createdAt)
            transactionsArray.put(tObj)
        }
        root.put("transactions", transactionsArray)

        return root.toString(2)
    }

    /**
     * Unified parser that auto-detects JSON or CSV and parses records gracefully.
     */
    fun parseImportContent(rawContent: String): ImportParseResult {
        val trimmed = rawContent.trim()
        if (trimmed.isEmpty()) {
            return ImportParseResult.Error("The selected content is empty.")
        }

        return if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            parseJson(trimmed)
        } else {
            parseCsv(trimmed)
        }
    }

    private fun parseJson(jsonString: String): ImportParseResult {
        return try {
            val categoriesList = mutableListOf<CategoryEntity>()
            val transactionsList = mutableListOf<TransactionEntity>()

            if (jsonString.trim().startsWith("[")) {
                // Array of transactions
                val transArray = JSONArray(jsonString)
                parseJsonTransactionsArray(transArray, transactionsList, categoriesList)
            } else {
                val root = JSONObject(jsonString)

                if (root.has("categories")) {
                    val catArray = root.getJSONArray("categories")
                    for (i in 0 until catArray.length()) {
                        val obj = catArray.getJSONObject(i)
                        categoriesList.add(
                            CategoryEntity(
                                id = obj.optLong("id", 0L),
                                name = obj.optString("name", "Unnamed Category").trim(),
                                iconName = obj.optString("iconName", "payments"),
                                colorHex = CategoryIconHelper.normalizeColorHex(obj.optLong("colorHex", 0xFF4CAF50)),
                                type = obj.optString("type", "EXPENSE").uppercase(),
                                budgetLimit = obj.optDouble("budgetLimit", 0.0),
                                isDefault = obj.optBoolean("isDefault", false)
                            )
                        )
                    }
                }

                if (root.has("transactions")) {
                    val transArray = root.getJSONArray("transactions")
                    parseJsonTransactionsArray(transArray, transactionsList, categoriesList)
                }
            }

            if (categoriesList.isEmpty() && transactionsList.isEmpty()) {
                return ImportParseResult.Error("No valid transactions or categories were found in the JSON file.")
            }

            calculateSummary("JSON Backup", categoriesList, transactionsList)
        } catch (e: Exception) {
            ImportParseResult.Error("Failed to parse JSON backup: ${e.localizedMessage ?: "Invalid structure"}")
        }
    }

    private fun parseJsonTransactionsArray(
        transArray: JSONArray,
        transactionsList: MutableList<TransactionEntity>,
        categoriesList: MutableList<CategoryEntity>
    ) {
        val existingCatNames = categoriesList.map { it.name.lowercase() }.toMutableSet()

        for (i in 0 until transArray.length()) {
            val obj = transArray.getJSONObject(i)
            val rawCatId = if (!obj.has("categoryId") || obj.isNull("categoryId")) null else obj.optLong("categoryId")
            val catName = obj.optString("categoryName", obj.optString("category", "")).trim()

            // If a category name is given and doesn't exist in categoriesList, register it
            if (catName.isNotEmpty() && !existingCatNames.contains(catName.lowercase())) {
                val newCatId = if (rawCatId != null && rawCatId > 0) rawCatId else (categoriesList.size + 100L)
                categoriesList.add(
                    CategoryEntity(
                        id = newCatId,
                        name = catName,
                        iconName = CategoryIconHelper.suggestIconForName(catName),
                        colorHex = CategoryIconHelper.suggestColorForName(catName),
                        type = obj.optString("type", "EXPENSE").uppercase(),
                        budgetLimit = 0.0,
                        isDefault = false
                    )
                )
                existingCatNames.add(catName.lowercase())
            }

            val rawDate = obj.opt("dateTimestamp")
            val timestamp = parseFlexibleDate(rawDate ?: obj.opt("date") ?: obj.opt("formattedDate"))

            val rawAmount = obj.opt("amount")
            val amount = cleanAmountNumber(rawAmount)

            transactionsList.add(
                TransactionEntity(
                    id = obj.optLong("id", 0L),
                    title = obj.optString("title", obj.optString("description", "Untitled Transaction")).trim(),
                    amount = amount,
                    type = obj.optString("type", "EXPENSE").uppercase(),
                    categoryId = rawCatId,
                    dateTimestamp = timestamp,
                    note = obj.optString("note", obj.optString("memo", "")).trim(),
                    paymentMethod = obj.optString("paymentMethod", "Card").trim(),
                    isRecurring = obj.optBoolean("isRecurring", false),
                    receiptUri = if (obj.has("receiptUri") && !obj.isNull("receiptUri")) obj.optString("receiptUri") else null,
                    createdAt = obj.optLong("createdAt", timestamp)
                )
            )
        }
    }

    private fun parseCsv(csvString: String): ImportParseResult {
        return try {
            val lines = parseCsvLines(csvString)
            if (lines.isEmpty()) {
                return ImportParseResult.Error("CSV file is empty.")
            }

            val header = lines.first().map { it.trim().lowercase() }
            val dataRows = if (isHeaderRow(header)) lines.drop(1) else lines

            var dateIdx = findColumnIndex(header, listOf("date", "time", "timestamp", "datetime", "created", "trans_date"))
            var titleIdx = findColumnIndex(header, listOf("title", "description", "name", "payee", "merchant", "item", "memo"))
            var typeIdx = findColumnIndex(header, listOf("type", "transaction type", "kind"))
            var catIdx = findColumnIndex(header, listOf("category", "category name", "categoryname", "tag"))
            var amountIdx = findColumnIndex(header, listOf("amount", "value", "total", "cost", "price"))
            var paymentIdx = findColumnIndex(header, listOf("payment method", "payment", "method", "account", "mode"))
            var recurringIdx = findColumnIndex(header, listOf("recurring", "is_recurring", "repeat"))
            var noteIdx = findColumnIndex(header, listOf("note", "notes", "comment", "details", "memo"))

            // Fallback for standard 9-column format
            if (dateIdx == -1 && dataRows.firstOrNull()?.size == 9) {
                dateIdx = 1
                titleIdx = 2
                typeIdx = 3
                catIdx = 4
                amountIdx = 5
                paymentIdx = 6
                recurringIdx = 7
                noteIdx = 8
            }

            val categoriesList = mutableListOf<CategoryEntity>()
            val transactionsList = mutableListOf<TransactionEntity>()
            val categoryMap = mutableMapOf<String, Long>()

            for (row in dataRows) {
                if (row.isEmpty() || row.all { it.isBlank() }) continue

                val rawTitle = if (titleIdx in row.indices) row[titleIdx] else "Transaction"
                val rawAmountStr = if (amountIdx in row.indices) row[amountIdx] else "0"
                val rawDateStr = if (dateIdx in row.indices) row[dateIdx] else ""
                val rawTypeStr = if (typeIdx in row.indices) row[typeIdx] else ""
                val rawCatStr = if (catIdx in row.indices) row[catIdx].trim() else "General"
                val rawPayment = if (paymentIdx in row.indices) row[paymentIdx].trim() else "Card"
                val rawRecurring = if (recurringIdx in row.indices) row[recurringIdx].trim().equals("true", ignoreCase = true) else false
                val rawNote = if (noteIdx in row.indices) row[noteIdx].trim() else ""

                val amount = cleanAmountNumber(rawAmountStr)
                if (amount <= 0 && rawAmountStr.isBlank()) continue

                val type = if (rawTypeStr.contains("INCOME", ignoreCase = true)) {
                    "INCOME"
                } else if (rawTypeStr.contains("EXPENSE", ignoreCase = true)) {
                    "EXPENSE"
                } else if (rawAmountStr.startsWith("-")) {
                    "EXPENSE"
                } else {
                    "EXPENSE"
                }

                val timestamp = parseFlexibleDate(rawDateStr)

                // Category management
                val cleanCatName = if (rawCatStr.isNotBlank() && !rawCatStr.equals("null", ignoreCase = true)) rawCatStr else "General"
                val catKey = cleanCatName.lowercase()
                var categoryId = categoryMap[catKey]
                if (categoryId == null) {
                    val newId = (categoriesList.size + 1).toLong()
                    categoryMap[catKey] = newId
                    categoryId = newId
                    categoriesList.add(
                        CategoryEntity(
                            id = newId,
                            name = cleanCatName,
                            iconName = CategoryIconHelper.suggestIconForName(cleanCatName),
                            colorHex = CategoryIconHelper.suggestColorForName(cleanCatName),
                            type = type,
                            budgetLimit = 0.0,
                            isDefault = false
                        )
                    )
                }

                transactionsList.add(
                    TransactionEntity(
                        id = 0L,
                        title = rawTitle.ifBlank { "Expense" },
                        amount = amount,
                        type = type,
                        categoryId = categoryId,
                        dateTimestamp = timestamp,
                        note = rawNote,
                        paymentMethod = rawPayment.ifBlank { "Card" },
                        isRecurring = rawRecurring,
                        createdAt = timestamp
                    )
                )
            }

            if (transactionsList.isEmpty()) {
                return ImportParseResult.Error("No valid transaction rows found in CSV. Please verify column layout.")
            }

            calculateSummary("CSV Spreadsheet", categoriesList, transactionsList)
        } catch (e: Exception) {
            ImportParseResult.Error("CSV Parsing error: ${e.localizedMessage ?: "Invalid CSV format"}")
        }
    }

    private fun isHeaderRow(firstRow: List<String>): Boolean {
        val keywords = listOf("date", "title", "amount", "category", "type", "description", "id", "price")
        return firstRow.any { col -> keywords.any { kw -> col.contains(kw) } }
    }

    private fun findColumnIndex(header: List<String>, candidates: List<String>): Int {
        for ((idx, col) in header.withIndex()) {
            for (candidate in candidates) {
                if (col.equals(candidate, ignoreCase = true) || col.contains(candidate, ignoreCase = true)) {
                    return idx
                }
            }
        }
        return -1
    }

    private fun parseCsvLines(csv: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < csv.length) {
            val c = csv[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < csv.length && csv[i + 1] == '"') {
                        currentField.append('"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    currentRow.add(currentField.toString().trim())
                    currentField.clear()
                }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    if (c == '\r' && i + 1 < csv.length && csv[i + 1] == '\n') {
                        i++
                    }
                    currentRow.add(currentField.toString().trim())
                    currentField.clear()
                    if (currentRow.isNotEmpty() && currentRow.any { it.isNotEmpty() }) {
                        result.add(currentRow.toList())
                    }
                    currentRow.clear()
                }
                else -> {
                    currentField.append(c)
                }
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString().trim())
            if (currentRow.any { it.isNotEmpty() }) {
                result.add(currentRow.toList())
            }
        }

        return result
    }

    private fun cleanAmountNumber(raw: Any?): Double {
        if (raw == null) return 0.0
        if (raw is Number) return Math.abs(raw.toDouble())
        val str = raw.toString().trim()
        val cleaned = str.replace("$", "")
            .replace("€", "")
            .replace("£", "")
            .replace("৳", "")
            .replace("₹", "")
            .replace(" ", "")
            .replace(",", "")
        return Math.abs(cleaned.toDoubleOrNull() ?: 0.0)
    }

    private fun parseFlexibleDate(raw: Any?): Long {
        if (raw == null) return System.currentTimeMillis()
        if (raw is Number) {
            val longVal = raw.toLong()
            return if (longVal > 100000000000L) longVal else longVal * 1000L
        }
        val str = raw.toString().trim()
        val numericLong = str.toLongOrNull()
        if (numericLong != null && numericLong > 100000000000L) {
            return numericLong
        }

        for (format in supportedDateFormats) {
            try {
                val parsed = format.parse(str)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
            }
        }

        return System.currentTimeMillis()
    }

    private fun calculateSummary(
        format: String,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>
    ): ImportParseResult.Success {
        var expenseSum = 0.0
        var incomeSum = 0.0
        var minTime = Long.MAX_VALUE
        var maxTime = Long.MIN_VALUE

        for (t in transactions) {
            if (t.type == "INCOME") incomeSum += t.amount else expenseSum += t.amount
            if (t.dateTimestamp < minTime) minTime = t.dateTimestamp
            if (t.dateTimestamp > maxTime) maxTime = t.dateTimestamp
        }

        val rangeStr = if (transactions.isNotEmpty() && minTime != Long.MAX_VALUE) {
            val shortFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            "${shortFormat.format(Date(minTime))} — ${shortFormat.format(Date(maxTime))}"
        } else {
            "No dated entries"
        }

        return ImportParseResult.Success(
            format = format,
            categories = categories,
            transactions = transactions,
            dateRangeSummary = rangeStr,
            totalExpenseSum = expenseSum,
            totalIncomeSum = incomeSum
        )
    }

    fun shareExportFile(context: Context, content: String, isCsv: Boolean) {
        val extension = if (isCsv) "csv" else "json"
        val mimeType = if (isCsv) "text/csv" else "application/json"
        val timestamp = fileTimestampFormatter.format(Date())
        val fileName = "expense_tracker_export_$timestamp.$extension"

        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val file = File(exportDir, fileName)
        file.writeText(content)

        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_SUBJECT, "Expense Tracker Data Export ($fileName)")
            if (uri != null) {
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                putExtra(Intent.EXTRA_TEXT, content)
            }
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export Financial Data"))
    }
}
