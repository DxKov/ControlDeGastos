package com.example.controldegastos.feature.transactions.di

import com.example.controldegastos.core.domain.repository.TransactionRepository
import com.example.controldegastos.feature.transactions.data.repository.TransactionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionRepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: com.example.controldegastos.feature.transactions.data.repository.TransactionRepositoryImpl
    ): com.example.controldegastos.core.domain.repository.TransactionRepository
}
