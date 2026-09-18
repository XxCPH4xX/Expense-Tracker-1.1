package com.fahimshahrier.data.sample

import com.fahimshahrier.data.local.entity.CategoryEntity
import com.fahimshahrier.data.local.entity.TransactionEntity
import java.util.Calendar

object SampleDataGenerator {

    val defaultCategories = listOf(
        // Expense categories
        CategoryEntity(
            id = 1,
            name = "Food & Dining",
            iconName = "fastfood",
            colorHex = 0xFFFF7043, // Coral Orange
            type = "EXPENSE",
            budgetLimit = 650.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 2,
            name = "Groceries",
            iconName = "shopping_cart",
            colorHex = 0xFF26A69A, // Teal
            type = "EXPENSE",
            budgetLimit = 400.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 3,
            name = "Housing & Rent",
            iconName = "home",
            colorHex = 0xFF5C6BC0, // Indigo
            type = "EXPENSE",
            budgetLimit = 1200.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 4,
            name = "Transportation",
            iconName = "directions_car",
            colorHex = 0xFF42A5F5, // Blue
            type = "EXPENSE",
            budgetLimit = 250.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 5,
            name = "Shopping",
            iconName = "shopping_bag",
            colorHex = 0xFFEC407A, // Pink
            type = "EXPENSE",
            budgetLimit = 300.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 6,
            name = "Entertainment",
            iconName = "sports_esports",
            colorHex = 0xFFAB47BC, // Purple
            type = "EXPENSE",
            budgetLimit = 200.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 7,
            name = "Health & Fitness",
            iconName = "fitness_center",
            colorHex = 0xFF66BB6A, // Green
            type = "EXPENSE",
            budgetLimit = 150.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 8,
            name = "Utilities & Bills",
            iconName = "receipt_long",
            colorHex = 0xFFFFA726, // Amber
            type = "EXPENSE",
            budgetLimit = 220.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 9,
            name = "Subscriptions",
            iconName = "subscriptions",
            colorHex = 0xFF7E57C2, // Deep Purple
            type = "EXPENSE",
            budgetLimit = 60.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 13,
            name = "Emergency & Ad-hoc",
            iconName = "warning",
            colorHex = 0xFFE53935, // Red
            type = "EXPENSE",
            budgetLimit = 0.0, // Unbudgeted ad-hoc
            isDefault = true
        ),

        // Income categories
        CategoryEntity(
            id = 10,
            name = "Salary",
            iconName = "payments",
            colorHex = 0xFF2E7D32, // Dark Green
            type = "INCOME",
            budgetLimit = 0.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 11,
            name = "Freelance & Projects",
            iconName = "work",
            colorHex = 0xFF00897B, // Emerald
            type = "INCOME",
            budgetLimit = 0.0,
            isDefault = true
        ),
        CategoryEntity(
            id = 12,
            name = "Investments & Dividends",
            iconName = "trending_up",
            colorHex = 0xFF1565C0, // Cobalt
            type = "INCOME",
            budgetLimit = 0.0,
            isDefault = true
        )
    )

    fun createInitialTransactions(): List<TransactionEntity> {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        fun dateOffset(daysAgo: Int, hour: Int = 12): Long {
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, 30)
            return cal.timeInMillis
        }

        return listOf(
            // Salary
            TransactionEntity(
                id = 1,
                title = "Monthly Salary Deposit",
                amount = 4500.0,
                type = "INCOME",
                categoryId = 10,
                dateTimestamp = dateOffset(21, 9),
                note = "Direct payroll deposit",
                paymentMethod = "Bank Transfer"
            ),
            // Freelance
            TransactionEntity(
                id = 2,
                title = "UI/UX Mobile Design Contract",
                amount = 950.0,
                type = "INCOME",
                categoryId = 11,
                dateTimestamp = dateOffset(14, 15),
                note = "Milestone payment from client",
                paymentMethod = "Bank Transfer"
            ),
            // Dividends
            TransactionEntity(
                id = 3,
                title = "Tech Index ETF Dividend",
                amount = 185.50,
                type = "INCOME",
                categoryId = 12,
                dateTimestamp = dateOffset(7, 10),
                note = "Quarterly dividend payout",
                paymentMethod = "Bank Transfer"
            ),
            // Rent
            TransactionEntity(
                id = 4,
                title = "Apartment Rent & Parking",
                amount = 1150.0,
                type = "EXPENSE",
                categoryId = 3,
                dateTimestamp = dateOffset(21, 10),
                note = "Monthly lease payment",
                paymentMethod = "Bank Transfer",
                isRecurring = true
            ),
            // Groceries
            TransactionEntity(
                id = 5,
                title = "Whole Foods Organic Market",
                amount = 128.40,
                type = "EXPENSE",
                categoryId = 2,
                dateTimestamp = dateOffset(18, 17),
                note = "Weekly meal prep groceries",
                paymentMethod = "Card"
            ),
            TransactionEntity(
                id = 6,
                title = "Trader Joe's Pantry Run",
                amount = 84.75,
                type = "EXPENSE",
                categoryId = 2,
                dateTimestamp = dateOffset(8, 18),
                note = "Snacks, produce and staples",
                paymentMethod = "Card"
            ),
            // Food & Dining
            TransactionEntity(
                id = 7,
                title = "Artisan Bistro Dinner",
                amount = 76.20,
                type = "EXPENSE",
                categoryId = 1,
                dateTimestamp = dateOffset(15, 20),
                note = "Dinner with colleagues",
                paymentMethod = "Card"
            ),
            TransactionEntity(
                id = 8,
                title = "Blue Bottle Specialty Coffee",
                amount = 14.50,
                type = "EXPENSE",
                categoryId = 1,
                dateTimestamp = dateOffset(3, 8),
                note = "Espresso & avocado toast",
                paymentMethod = "bKash"
            ),
            TransactionEntity(
                id = 9,
                title = "Sushi Omakase Lunch",
                amount = 58.00,
                type = "EXPENSE",
                categoryId = 1,
                dateTimestamp = dateOffset(1, 13),
                note = "Weekend lunch with family",
                paymentMethod = "Nagad"
            ),
            // Transport
            TransactionEntity(
                id = 10,
                title = "Shell Gas Station Fuel",
                amount = 52.00,
                type = "EXPENSE",
                categoryId = 4,
                dateTimestamp = dateOffset(12, 11),
                note = "Full tank premium fuel",
                paymentMethod = "Card"
            ),
            TransactionEntity(
                id = 11,
                title = "Metro Transit Pass Recharge",
                amount = 45.00,
                type = "EXPENSE",
                categoryId = 4,
                dateTimestamp = dateOffset(4, 9),
                note = "Monthly commuter transit pass",
                paymentMethod = "Card"
            ),
            // Shopping
            TransactionEntity(
                id = 12,
                title = "Noise-Cancelling Headphones",
                amount = 179.99,
                type = "EXPENSE",
                categoryId = 5,
                dateTimestamp = dateOffset(10, 16),
                note = "Ergonomic work setup upgrade",
                paymentMethod = "Card"
            ),
            // Entertainment
            TransactionEntity(
                id = 13,
                title = "IMAX Cinema Tickets",
                amount = 36.50,
                type = "EXPENSE",
                categoryId = 6,
                dateTimestamp = dateOffset(6, 19),
                note = "Sci-fi premiere with friends",
                paymentMethod = "Card"
            ),
            // Utilities
            TransactionEntity(
                id = 14,
                title = "Gigabit Fiber Internet",
                amount = 70.00,
                type = "EXPENSE",
                categoryId = 8,
                dateTimestamp = dateOffset(16, 14),
                note = "High speed home connection",
                paymentMethod = "Bank Transfer",
                isRecurring = true
            ),
            TransactionEntity(
                id = 15,
                title = "Electricity & Power Grid",
                amount = 95.30,
                type = "EXPENSE",
                categoryId = 8,
                dateTimestamp = dateOffset(5, 11),
                note = "Summer AC cooling bill",
                paymentMethod = "Card"
            ),
            // Subscriptions
            TransactionEntity(
                id = 16,
                title = "Streaming & Music Bundle",
                amount = 24.99,
                type = "EXPENSE",
                categoryId = 9,
                dateTimestamp = dateOffset(9, 12),
                note = "Spotify & Netflix recurring",
                paymentMethod = "Card",
                isRecurring = true
            ),
            // Health
            TransactionEntity(
                id = 17,
                title = "Climbing Gym Monthly Pass",
                amount = 85.00,
                type = "EXPENSE",
                categoryId = 7,
                dateTimestamp = dateOffset(2, 10),
                note = "Indoor boulder membership",
                paymentMethod = "Card"
            ),
            // Unbudgeted Emergency
            TransactionEntity(
                id = 18,
                title = "Emergency Dental Root Canal Treatment",
                amount = 350.0,
                type = "EXPENSE",
                categoryId = 13,
                dateTimestamp = dateOffset(5, 15),
                note = "Unplanned emergency wisdom tooth extraction",
                paymentMethod = "Card"
            ),
            // Additional Dining Expense creating Dining overage
            TransactionEntity(
                id = 19,
                title = "Family Celebration Banquet",
                amount = 550.0,
                type = "EXPENSE",
                categoryId = 1,
                dateTimestamp = dateOffset(4, 20),
                note = "Anniversary celebration dinner",
                paymentMethod = "Card"
            )
        )
    }
}
