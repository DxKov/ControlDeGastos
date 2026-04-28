package com.example.controldegastos.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.controldegastos.core.data.local.GastoDatabase
import com.example.controldegastos.core.data.local.dao.AccountDao
import com.example.controldegastos.core.data.local.dao.CreditCardDao
import com.example.controldegastos.core.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Seeds default categories on first database creation so that transactions
     * with categoryId 1-4 are always valid (Comida, Transporte, Ocio, Servicios).
     */
    private val seedCallback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL(
                """
                INSERT OR IGNORE INTO categories (id, name, icon, color) VALUES
                (1, 'Comida',      'restaurant',    ${0xFFE17055.toInt()}),
                (2, 'Transporte',  'commute',       ${0xFF0984E3.toInt()}),
                (3, 'Ocio',        'sports_esports',${0xFF6C5CE7.toInt()}),
                (4, 'Servicios',   'bolt',          ${0xFFFDCB6E.toInt()}),
                (5, 'Salud',       'local_hospital',${0xFF00CEC9.toInt()}),
                (6, 'Educación',   'school',        ${0xFF00B894.toInt()}),
                (7, 'Compras',     'shopping_bag',  ${0xFFE84393.toInt()}),
                (8, 'Otros',       'more_horiz',    ${0xFF636E72.toInt()})
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GastoDatabase {
        return Room.databaseBuilder(
            context,
            GastoDatabase::class.java,
            "gasto_db"
        )
            .addCallback(seedCallback)
            .build()
    }

    @Provides
    @Singleton
    fun provideAccountDao(db: GastoDatabase): AccountDao = db.accountDao()

    @Provides
    @Singleton
    fun provideCreditCardDao(db: GastoDatabase): CreditCardDao = db.creditCardDao()

    @Provides
    @Singleton
    fun provideTransactionDao(db: GastoDatabase): TransactionDao = db.transactionDao()
}
