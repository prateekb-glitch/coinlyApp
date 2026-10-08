package com.example.coinly.domain.model

/**
 * Domain model representing the user's current coin balance and cash redemption value.
 * Clean Architecture - Domain Layer.
 */
data class CoinBalance(
    val totalCoins: Int,
    val cashValueUsd: Double,
    val redemptionThreshold: Int = 1000
) {
    val canRedeem: Boolean
        get() = totalCoins >= redemptionThreshold
}
