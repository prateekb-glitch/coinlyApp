package com.example.coinly.domain.repository

import com.example.coinly.domain.model.CoinBalance
import com.example.coinly.domain.model.TaskItem
import com.example.coinly.domain.model.ClaimResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining data operations for coin balances, tasks, and reward claims.
 * Clean Architecture - Domain Layer.
 */
interface RewardRepository {
    fun observeCoinBalance(): Flow<CoinBalance>
    fun observeTasks(): Flow<List<TaskItem>>
    suspend fun claimReward(taskId: String): ClaimResult
    suspend fun redeemCoins(): ClaimResult
}
