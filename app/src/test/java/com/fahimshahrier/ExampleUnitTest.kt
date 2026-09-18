package com.fahimshahrier

import com.fahimshahrier.data.export.DataExportImportHelper
import com.fahimshahrier.data.export.ImportParseResult
import com.fahimshahrier.data.local.entity.CategoryEntity
import com.fahimshahrier.data.local.entity.TransactionEntity
import com.fahimshahrier.data.local.entity.TransactionWithCategory
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

  @Test
  fun testJsonBackupAndRestoreParsing() {
    val categories = listOf(
      CategoryEntity(id = 1, name = "Groceries", iconName = "shopping_cart", colorHex = 0xFF26A69A, type = "EXPENSE", budgetLimit = 400.0)
    )
    val transactions = listOf(
      TransactionWithCategory(
        transaction = TransactionEntity(id = 10, title = "Supermarket", amount = 85.0, type = "EXPENSE", categoryId = 1, dateTimestamp = 1700000000000L, note = "Weekly groceries"),
        category = categories[0]
      )
    )

    val json = DataExportImportHelper.generateJson(categories, transactions)
    val result = DataExportImportHelper.parseImportContent(json)

    assertTrue(result is ImportParseResult.Success)
    val success = result as ImportParseResult.Success

    assertEquals(1, success.categories.size)
    assertEquals("Groceries", success.categories[0].name)
    assertEquals(1, success.transactions.size)
    assertEquals("Supermarket", success.transactions[0].title)
    assertEquals(85.0, success.transactions[0].amount, 0.001)
  }

  @Test
  fun testCsvExportAndRestoreParsing() {
    val categories = listOf(
      CategoryEntity(id = 2, name = "Salary", iconName = "payments", colorHex = 0xFF2E7D32, type = "INCOME", budgetLimit = 0.0)
    )
    val transactions = listOf(
      TransactionWithCategory(
        transaction = TransactionEntity(id = 20, title = "Tech Corp Salary", amount = 4500.0, type = "INCOME", categoryId = 2, dateTimestamp = 1700000000000L, note = "Bi-weekly paycheck"),
        category = categories[0]
      )
    )

    val csv = DataExportImportHelper.generateCsv(transactions)
    val result = DataExportImportHelper.parseImportContent(csv)

    assertTrue(result is ImportParseResult.Success)
    val success = result as ImportParseResult.Success

    assertEquals(1, success.transactions.size)
    assertEquals("Tech Corp Salary", success.transactions[0].title)
    assertEquals(4500.0, success.transactions[0].amount, 0.001)
    assertEquals("INCOME", success.transactions[0].type)
  }

  @Test
  fun testAutoBackupAndRestoreLifecycle() {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    val categories = listOf(
      CategoryEntity(id = 3, name = "Rent", iconName = "home", colorHex = 0xFF1E88E5, type = "EXPENSE", budgetLimit = 1200.0)
    )
    val transactions = listOf(
      TransactionWithCategory(
        transaction = TransactionEntity(id = 30, title = "Monthly Apartment Rent", amount = 1200.0, type = "EXPENSE", categoryId = 3, dateTimestamp = 1700000000000L, note = "Apartment lease"),
        category = categories[0]
      )
    )

    com.fahimshahrier.data.export.SnapshotManager.saveAutoBackup(context, categories, transactions)
    val backupContent = com.fahimshahrier.data.export.SnapshotManager.getLatestAutoBackupContent(context)
    assertNotNull(backupContent)

    val parseResult = DataExportImportHelper.parseImportContent(backupContent!!)
    assertTrue(parseResult is ImportParseResult.Success)
    val success = parseResult as ImportParseResult.Success
    assertEquals(1, success.categories.size)
    assertEquals("Rent", success.categories[0].name)
    assertEquals(1, success.transactions.size)
    assertEquals("Monthly Apartment Rent", success.transactions[0].title)
  }
}
