package com.example.controldegastos.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GastoDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun transactionDao(): TransactionDao
}
