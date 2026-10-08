package com.example.coinly.domain.usecase

import com.example.coinly.domain.model.ClaimResult
import com.example.coinly.domain.repository.RewardRepository
import com.example.coinly.domain.repository.SecurityRepository

/**
 * Use case responsible for validating device security, gathering attestation tokens,
 * and executing the reward claim flow.
 * Clean Architecture - Domain Layer.
 */
class ClaimRewardUseCase(
    private val rewardRepository: RewardRepository,
    private val securityRepository: SecurityRepository
) {
    suspend operator fun invoke(taskId: String): ClaimResult {
        // Step 1: Check device security heuristics (root, emulator check)
        if (!securityRepository.isDeviceSecure()) {
            return ClaimResult.FraudDetected("Device security check failed: Root or emulator detected.")
        }

        // Step 2: Delegate claim execution to the repository (which incorporates attestation)
        return rewardRepository.claimReward(taskId)
    }
}
