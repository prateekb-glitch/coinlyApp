package com.example.coinly.data.repository

import com.example.coinly.domain.model.CoinBalance
import com.example.coinly.domain.model.TaskItem
import com.example.coinly.domain.model.TaskCategory
import com.example.coinly.domain.model.ClaimResult
import com.example.coinly.domain.repository.RewardRepository
import com.example.coinly.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Implementation of RewardRepository providing reactive Flows for coin balance and tasks,
 * and secure claim processing with anti-fraud attestation.
 * Clean Architecture - Data Layer.
 */
class RewardRepositoryImpl(
    private val securityRepository: SecurityRepository
) : RewardRepository {

    private val _coinBalance = MutableStateFlow(CoinBalance(totalCoins = 1050, cashValueUsd = 10.50))
    private val _tasks = MutableStateFlow(
        listOf(
            TaskItem("task_1", "Install Finance App", "Install and open partner banking app", 250, TaskCategory.APP_INSTALL, false),
            TaskItem("task_2", "Complete Consumer Survey", "Answer 5 quick marketing questions", 150, TaskCategory.SURVEY, false),
            TaskItem("task_3", "Daily Check-in Bonus", "Claim your daily active reward", 50, TaskCategory.ENGAGEMENT, false),
            TaskItem("task_4", "Play Puzzle Game Lvl 3", "Reach level 3 in partner puzzle game", 500, TaskCategory.APP_INSTALL, false)
        )
    )

    override fun observeCoinBalance(): Flow<CoinBalance> = _coinBalance.asStateFlow()

    override fun observeTasks(): Flow<List<TaskItem>> = _tasks.asStateFlow()

    override suspend fun redeemCoins(): ClaimResult {
        // Simulate network delay & secure payout handshake
        delay(600.milliseconds)

        val attestation = securityRepository.generateAttestation("redeem_cash")
        if (attestation.playIntegrityToken.isBlank() || attestation.hmacSignature.isBlank()) {
            return ClaimResult.FraudDetected("Attestation validation failed during redemption.")
        }

        val currentBalance = _coinBalance.value
        if (!currentBalance.canRedeem) {
            return ClaimResult.Error(IllegalStateException("Insufficient coins. Minimum threshold is ${currentBalance.redemptionThreshold} coins."))
        }

        val newTotalCoins = currentBalance.totalCoins - currentBalance.redemptionThreshold
        val newBalance = CoinBalance(
            totalCoins = newTotalCoins,
            cashValueUsd = newTotalCoins / 100.0
        )
        _coinBalance.value = newBalance

        return ClaimResult.Success(
            newBalance = newBalance,
            message = "Successfully redeemed $10.00 cash payout!"
        )
    }

    override suspend fun claimReward(taskId: String): ClaimResult {
        // Simulate network latency & secure verification handshake
        delay(1000)

        // Generate security attestation (Play Integrity + reCAPTCHA + HMAC signature)
        val attestation = securityRepository.generateAttestation("claim_reward_$taskId")
        
        // Verify attestation token integrity (simulating backend verification check)
        if (attestation.playIntegrityToken.isBlank() || attestation.hmacSignature.isBlank()) {
            return ClaimResult.FraudDetected("Attestation validation failed.")
        }

        val taskList = _tasks.value
        val task = taskList.find { it.id == taskId } ?: return ClaimResult.Error(IllegalArgumentException("Task not found"))

        if (task.isCompleted) {
            return ClaimResult.Error(IllegalStateException("Task already claimed"))
        }

        // Update task completion and add coins
        val updatedTasks = taskList.map { if (it.id == taskId) it.copy(isCompleted = true) else it }
        _tasks.value = updatedTasks

        val currentBalance = _coinBalance.value
        val newTotalCoins = currentBalance.totalCoins + task.rewardCoins
        val newBalance = CoinBalance(
            totalCoins = newTotalCoins,
            cashValueUsd = newTotalCoins / 100.0
        )
        _coinBalance.value = newBalance

        return ClaimResult.Success(
            newBalance = newBalance,
            message = "Successfully claimed ${task.rewardCoins} coins!"
        )
    }
}
