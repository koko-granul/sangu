package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FinanceDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtPaymentEntity
import com.example.data.local.entity.FundMovementEntity
import com.example.data.local.entity.FundPotEntity
import com.example.data.local.entity.LedgerEntryEntity
import com.example.data.local.entity.TransactionEntity
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        LedgerEntryEntity::class,
        FundPotEntity::class,
        FundMovementEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        AttachmentEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE fund_pots ADD COLUMN imagePath TEXT DEFAULT NULL")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_finance.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial data asynchronously
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database.financeDao())
                    }
                }
            }

            private suspend fun seedInitialData(dao: FinanceDao) {
                // 1. Seed Single Default Clean Account (Dompet Utama Rp0)
                dao.insertAccount(
                    AccountEntity(
                        name = "Dompet Utama",
                        type = "CASH",
                        colorHex = "#10B981",
                        iconName = "payments"
                    )
                )

                // 2. Seed Default Categories
                val defaultCategories = listOf(
                    // Expense
                    CategoryEntity(name = "Makanan & Minuman", type = "EXPENSE", iconName = "restaurant", colorHex = "#EF4444"),
                    CategoryEntity(name = "Transportasi", type = "EXPENSE", iconName = "directions_car", colorHex = "#F59E0B"),
                    CategoryEntity(name = "Belanja Harian", type = "EXPENSE", iconName = "shopping_bag", colorHex = "#3B82F6"),
                    CategoryEntity(name = "Tagihan & Utilitas", type = "EXPENSE", iconName = "receipt_long", colorHex = "#8B5CF6"),
                    CategoryEntity(name = "Kesehatan", type = "EXPENSE", iconName = "medical_services", colorHex = "#EC4899"),
                    CategoryEntity(name = "Hiburan", type = "EXPENSE", iconName = "sports_esports", colorHex = "#14B8A6"),
                    CategoryEntity(name = "Pendidikan", type = "EXPENSE", iconName = "school", colorHex = "#6366F1"),
                    CategoryEntity(name = "Lainnya", type = "EXPENSE", iconName = "category", colorHex = "#6B7280"),
                    // Income
                    CategoryEntity(name = "Gaji Pokok", type = "INCOME", iconName = "payments", colorHex = "#10B981"),
                    CategoryEntity(name = "Bisnis / Penjualan", type = "INCOME", iconName = "storefront", colorHex = "#059669"),
                    CategoryEntity(name = "Bonus & Hadiah", type = "INCOME", iconName = "card_giftcard", colorHex = "#10B981"),
                    CategoryEntity(name = "Investasi", type = "INCOME", iconName = "trending_up", colorHex = "#047857"),
                    CategoryEntity(name = "Lainnya (Pemasukan)", type = "INCOME", iconName = "savings", colorHex = "#34D399")
                )
                dao.insertCategories(defaultCategories)

            }
        }
    }
}
