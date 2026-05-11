package com.example.controldegastos.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.controldegastos.core.data.local.dao.AccountDao
import com.example.controldegastos.core.data.local.dao.CreditCardDao
import com.example.controldegastos.core.data.local.dao.TransactionDao
import com.example.controldegastos.core.data.local.entity.AccountEntity
import com.example.controldegastos.core.data.local.entity.BillingCycleEntity
import com.example.controldegastos.core.data.local.entity.CategoryEntity
import com.example.controldegastos.core.data.local.entity.CreditCardEntity
import com.example.controldegastos.core.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CreditCardEntity::class,
        BillingCycleEntity::class,
        TransactionEntity::class,
        CategoryEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GastoDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        /**
         * Migration v4 → v5: Adds the installmentMonths column to transactions.
         * Nullable INT so existing rows are unaffected (default NULL = pago normal).
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN installmentMonths INTEGER"
                )
            }
        }

        /**
         * Migration v5 → v6: Adds Category 9 for credit card payments.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO categories (id, name, icon, color) 
                    VALUES (9, 'Pago de Tarjeta', 'credit_score', ${0xFF00B894.toInt()})
                    """.trimIndent()
                )
            }
        }
    }
}

