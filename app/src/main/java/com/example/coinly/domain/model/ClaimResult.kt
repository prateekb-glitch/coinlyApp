package com.example.coinly.domain.model

/**
 * Domain model representing the outcome of a reward claim transaction.
 */
sealed interface ClaimResult {
    data class Success(val newBalance: CoinBalance, val message: String) : ClaimResult
    data class FraudDetected(val reason: String) : ClaimResult
    data class Error(val throwable: Throwable) : ClaimResult
}
