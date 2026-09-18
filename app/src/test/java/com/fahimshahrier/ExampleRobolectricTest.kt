package com.fahimshahrier

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.fahimshahrier.data.export.DataExportImportHelper
import com.fahimshahrier.data.local.entity.CategoryEntity
import com.fahimshahrier.data.local.entity.TransactionEntity
import com.fahimshahrier.data.local.entity.TransactionWithCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Expense Tracker", appName)
  }

  @Test
  fun `test csv generation and export`() {
    val cat = CategoryEntity(id = 1, name = "Food", iconName = "fastfood", colorHex = 0xFF4CAF50, type = "EXPENSE")
    val trans = TransactionEntity(id = 1, title = "Lunch", amount = 15.5, type = "EXPENSE", categoryId = 1, dateTimestamp = 1700000000000L)
    val list = listOf(TransactionWithCategory(trans, cat))

    val csv = DataExportImportHelper.generateCsv(list)
    assertTrue(csv.contains("Food"))
    assertTrue(csv.contains("Lunch"))
    assertTrue(csv.contains("15.5"))
  }
}
