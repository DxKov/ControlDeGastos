package com.example.controldegastos.core.data.local.dao

import androidx.room.*
import com.example.controldegastos.core.data.local.entity.BillingCycleEntity
import com.example.controldegastos.core.data.local.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards")
    fun observeAllCards(): Flow<List<CreditCardEntity>>
    
    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getCardById(id: Long): CreditCardEntity?
    
    @Query("SELECT * FROM billing_cycles WHERE cardId = :cardId AND status = 'OPEN' LIMIT 1")
    fun observeCurrentCycle(cardId: Long): Flow<BillingCycleEntity?>
    
    @Query("SELECT * FROM billing_cycles WHERE cardId = :cardId AND status = 'OPEN' LIMIT 1")
    suspend fun getCurrentCycle(cardId: Long): BillingCycleEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: BillingCycleEntity)
    
    @Update
    suspend fun updateCycle(cycle: BillingCycleEntity)
    
    @Delete
    suspend fun deleteCard(card: CreditCardEntity)
}
