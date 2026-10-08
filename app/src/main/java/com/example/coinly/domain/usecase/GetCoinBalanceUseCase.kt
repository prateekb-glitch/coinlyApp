package com.example.coinly.domain.usecase

import com.example.coinly.domain.model.CoinBalance
import com.example.coinly.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe the user's current coin balance.
 */
class GetCoinBalanceUseCase(
    private val rewardRepository: RewardRepository
) {
    operator fun invoke(): Flow<CoinBalance> {
        return rewardRepository.observeCoinBalance()
    }
}
