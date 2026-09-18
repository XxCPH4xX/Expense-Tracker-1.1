package com.fahimshahrier.data.export

import android.content.Context
import com.fahimshahrier.data.local.entity.CategoryEntity
import com.fahimshahrier.data.local.entity.TransactionWithCategory
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SnapshotItem(
    val id: String,
    val filename: String,
    val timestamp: Long,
    val formattedDate: String,
    val transactionCount: Int,
    val categoryCount: Int,
    val note: String
)

object SnapshotManager {

    private val displayDateFormatter = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())

    private fun getSnapshotsDir(context: Context): File {
        val dir = File(context.filesDir, "snapshots")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun saveSnapshot(
        context: Context,
        categories: List<CategoryEntity>,
        transactions: List<TransactionWithCategory>,
        note: String = "Manual Snapshot"
    ): SnapshotItem {
        val dir = getSnapshotsDir(context)
        val timestamp = System.currentTimeMillis()
        val filename = "snapshot_${timestamp}.json"
        val file = File(dir, filename)

        val jsonString = DataExportImportHelper.generateJson(
            categories = categories,
            transactions = transactions,
            note = note
        )
        file.writeText(jsonString)

        // Automatically sync persistent auto-backup
        saveAutoBackup(context, categories, transactions)

        return SnapshotItem(
            id = timestamp.toString(),
            filename = filename,
            timestamp = timestamp,
            formattedDate = displayDateFormatter.format(Date(timestamp)),
            transactionCount = transactions.size,
            categoryCount = categories.size,
            note = note
        )
    }

    private fun getBackupsDir(context: Context): File {
        val dir = File(context.filesDir, "backups")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Saves a high-priority persistent backup to internal and external storage.
     * This backup is safeguarded against unexpected app restarts, upgrades, and re-installs.
     */
    fun saveAutoBackup(
        context: Context,
        categories: List<CategoryEntity>,
        transactions: List<TransactionWithCategory>
    ) {
        if (transactions.isEmpty() && categories.isEmpty()) return
        try {
            val jsonString = DataExportImportHelper.generateJson(
                categories = categories,
                transactions = transactions,
                note = "Auto-Persistence Backup"
            )

            // 1. Internal App Backups dir
            val internalFile = File(getBackupsDir(context), "auto_backup_latest.json")
            internalFile.writeText(jsonString)

            // 2. External app files dir (persists across updates and external storage)
            val extDir = context.getExternalFilesDir(null)
            if (extDir != null) {
                val extBackupDir = File(extDir, "backups")
                if (!extBackupDir.exists()) extBackupDir.mkdirs()
                val extFile = File(extBackupDir, "auto_backup_latest.json")
                extFile.writeText(jsonString)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Retrieves the most recent backup JSON content to recover user info
     * if the database is empty after reinstall or update.
     */
    fun getLatestAutoBackupContent(context: Context): String? {
        try {
            // 1. Try internal backup file
            val internalFile = File(getBackupsDir(context), "auto_backup_latest.json")
            if (internalFile.exists() && internalFile.length() > 0) {
                val text = internalFile.readText()
                if (text.isNotBlank()) return text
            }

            // 2. Try external app backup file
            val extDir = context.getExternalFilesDir(null)
            if (extDir != null) {
                val extFile = File(File(extDir, "backups"), "auto_backup_latest.json")
                if (extFile.exists() && extFile.length() > 0) {
                    val text = extFile.readText()
                    if (text.isNotBlank()) return text
                }
            }

            // 3. Try the newest manual or safety snapshot in snapshots dir
            val snapshots = listSnapshots(context)
            if (snapshots.isNotEmpty()) {
                val newest = snapshots.first()
                val content = readSnapshotContent(context, newest.filename)
                if (!content.isNullOrBlank()) return content
            }
        } catch (_: Exception) {
        }
        return null
    }

    fun listSnapshots(context: Context): List<SnapshotItem> {
        val dir = getSnapshotsDir(context)
        val files = dir.listFiles { f -> f.extension == "json" } ?: return emptyList()

        return files.mapNotNull { file ->
            try {
                val content = file.readText()
                val root = JSONObject(content)
                val timestamp = root.optLong("exportedAt", file.lastModified())
                val note = root.optString("note", "Backup Snapshot")
                val transCount = if (root.has("transactions")) root.getJSONArray("transactions").length() else 0
                val catCount = if (root.has("categories")) root.getJSONArray("categories").length() else 0

                SnapshotItem(
                    id = file.nameWithoutExtension,
                    filename = file.name,
                    timestamp = timestamp,
                    formattedDate = displayDateFormatter.format(Date(timestamp)),
                    transactionCount = transCount,
                    categoryCount = catCount,
                    note = note
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.timestamp }
    }

    fun readSnapshotContent(context: Context, filename: String): String? {
        val dir = getSnapshotsDir(context)
        val file = File(dir, filename)
        return if (file.exists()) file.readText() else null
    }

    fun deleteSnapshot(context: Context, filename: String): Boolean {
        val dir = getSnapshotsDir(context)
        val file = File(dir, filename)
        return if (file.exists()) file.delete() else false
    }
}
