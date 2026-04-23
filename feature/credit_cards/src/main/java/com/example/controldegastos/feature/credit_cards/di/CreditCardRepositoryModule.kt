package com.example.controldegastos.feature.credit_cards.di

import com.example.controldegastos.core.domain.repository.CreditCardRepository
import com.example.controldegastos.feature.credit_cards.data.repository.CreditCardRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CreditCardRepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindCreditCardRepository(
        creditCardRepositoryImpl: com.example.controldegastos.feature.credit_cards.data.repository.CreditCardRepositoryImpl
    ): com.example.controldegastos.core.domain.repository.CreditCardRepository
}
