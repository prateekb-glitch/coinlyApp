package com.example.coinly.trust.core.model

sealed interface TrustState {
    data object Idle : TrustState
    data object Checking : TrustState
    data object Allowed : TrustState
    data class Blocked(val reason: String, val isTerminal: Boolean) : TrustState
    data class ChallengeRequired(val challengeId: String, val taskId: String) : TrustState
    data class Retrying(val attempt: Int, val maxAttempts: Int, val delayMs: Long) : TrustState
}
