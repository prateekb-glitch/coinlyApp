package com.example.coinly.domain.usecase

import com.example.coinly.domain.model.ClaimResult
import com.example.coinly.domain.repository.RewardRepository
import com.example.coinly.domain.repository.SecurityRepository

/**
 * Use case responsible for validating device security and executing coin cash redemption.
 * Clean Architecture - Domain Layer.
 */
class RedeemCoinsUseCase(
    private val rewardRepository: RewardRepository,
    private val securityRepository: SecurityRepository
) {
    suspend operator fun invoke(): ClaimResult {
        // Step 1: Check device security heuristics (root, emulator check) before cash payout
        if (!securityRepository.isDeviceSecure()) {
            return ClaimResult.FraudDetected("Device security check failed: Root or emulator detected during payout.")
        }

        // Step 2: Execute redemption
        return rewardRepository.redeemCoins()
    }
}
