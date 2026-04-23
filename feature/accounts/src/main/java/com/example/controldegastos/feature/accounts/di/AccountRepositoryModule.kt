package com.example.controldegastos.feature.accounts.di

import com.example.controldegastos.feature.accounts.data.repository.AccountRepositoryImpl
import com.example.controldegastos.feature.accounts.domain.repository.AccountRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountRepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: com.example.controldegastos.feature.accounts.data.repository.AccountRepositoryImpl
    ): com.example.controldegastos.feature.accounts.domain.repository.AccountRepository
}
